package com.flansmod.teams.server;

import com.flansmod.teams.api.*;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.TeamsModConfig;
import com.flansmod.teams.common.network.toclient.DisplayScoresMessage;
import com.flansmod.teams.common.network.toclient.MapVotingOptionsMessage;
import com.flansmod.teams.common.network.toserver.PlaceVoteMessage;
import com.flansmod.teams.common.network.toserver.SelectTeamMessage;
import com.flansmod.teams.common.network.TeamsModPacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TeamsManager implements
		ITeamsAdmin,
		ITeamsRuntime
{
	private static class Phase
	{
		public void enter() {}
		public void tick() {}
		public void exit() {}
		public void onPlayerJoined(@Nonnull ServerPlayer player) {}
	}

	private final Map<String, GamemodeInfo> gamemodes = new HashMap<>();
	private final Map<String, MapInfo> maps = new HashMap<>();
	private final Map<String, Settings> settings = new HashMap<>();
	private final Settings defaultMapSettings;
	private final Map<ERoundPhase, Phase> phaseImpl = new HashMap<>();

	private boolean isRotationEnabled = false;
	private final List<RoundInfo> mapRotation = new ArrayList<>();

	private RoundInfo currentRound = RoundInfo.invalid;
	private RoundInfo nextRound = RoundInfo.invalid;

	private ERoundPhase currentPhase = ERoundPhase.Inactive;
	private ERoundPhase targetPhase = ERoundPhase.Inactive;
	private int ticksInCurrentPhase = 0;
	private RoundInstance currentRoundInstance;

	public TeamsManager()
	{
		defaultMapSettings = new Settings();
		settings.put(ISettings.DEFAULT_KEY, defaultMapSettings);

		createInactivePhase();
		createPreparingPhase();
		createGameplayPhase();
		createDisplayScoresPhase();
		createMapVotingPhase();
		createCleanupPhase();

		phaseImpl.get(currentPhase).enter();

		TeamsModPacketHandler.registerServerHandler(SelectTeamMessage.class, SelectTeamMessage::new, this::receiveSelectTeamMessage);
		TeamsModPacketHandler.registerServerHandler(PlaceVoteMessage.class, PlaceVoteMessage::new, this::receivePlaceVoteMessage);
	}

	private void createInactivePhase()
	{
		phaseImpl.put(ERoundPhase.Inactive, new Phase());
	}
	private void createPreparingPhase()
	{
		final int timeoutTicks = (int)Math.floor(TeamsModConfig.preparingPhaseTimeout.get() * 20d);

		phaseImpl.put(ERoundPhase.Preparing, new Phase(){
			@Override
			public void enter(){
				OpResult prepResult = prepareNewRound();
				if(prepResult.success())
					targetPhase = ERoundPhase.Gameplay;
				else
					targetPhase = ERoundPhase.Inactive;
			}
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= timeoutTicks)
				{
					TeamsMod.LOGGER.error("Preparing phase timed out!");
					targetPhase = ERoundPhase.Inactive;
				}
			}
		});
	}
	private void createGameplayPhase()
	{
		phaseImpl.put(ERoundPhase.Gameplay, new Phase(){
			@Override
			public void enter() {
				currentRoundInstance.begin();
			}
			@Override
			public void tick() {
				getCurrentGamemode().tick();
			}
			@Override
			public void exit() {
				currentRoundInstance.end();
			}
		});
	}
	private void createDisplayScoresPhase()
	{
		final int durationTicks = (int)Math.floor(TeamsModConfig.displayScoresPhaseDuration.get() * 20d);
		final boolean isVotingPhaseEnabled = TeamsModConfig.mapVoteEnabled.get();

		phaseImpl.put(ERoundPhase.DisplayScores, new Phase(){
			@Override
			public void enter()
			{
				TeamsModPacketHandler.sendToAll(createMsg());
			}
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= durationTicks)
					targetPhase = isVotingPhaseEnabled ? ERoundPhase.MapVote : ERoundPhase.Cleanup;
			}
			@Override
			public void onPlayerJoined(@Nonnull ServerPlayer player)
			{
				TeamsModPacketHandler.sendToPlayer(player, createMsg());
			}

			@Nonnull
			private DisplayScoresMessage createMsg()
			{
				return new DisplayScoresMessage();
			}
		});
	}
	private void createMapVotingPhase()
	{
		final int durationTicks = (int)Math.floor(TeamsModConfig.mapVotePhaseDuration.get() * 20d);

		phaseImpl.put(ERoundPhase.MapVote, new Phase(){
			@Override
			public void enter()
			{
				selectVotingOptions();
				TeamsModPacketHandler.sendToAll(createMsg());
			}
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= durationTicks)
					targetPhase = ERoundPhase.Cleanup;
			}
			@Override
			public void onPlayerJoined(@Nonnull ServerPlayer player)
			{
				TeamsModPacketHandler.sendToPlayer(player, createMsg());
			}

			private void selectVotingOptions()
			{

			}
			@Nonnull
			private MapVotingOptionsMessage createMsg()
			{
				return new MapVotingOptionsMessage();
			}
		});
	}
	private void createCleanupPhase()
	{
		final int timeoutTicks = (int)Math.floor(TeamsModConfig.cleanupPhaseTimeout.get() * 20d);

		phaseImpl.put(ERoundPhase.Cleanup, new Phase(){
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= timeoutTicks)
				{
					TeamsMod.LOGGER.error("Cleanup phase timed out!");
					targetPhase = ERoundPhase.Inactive;
				}
			}
		});
	}

	public void serverTick()
	{
		ticksInCurrentPhase++;

		phaseImpl.get(currentPhase).tick();
		if(targetPhase != currentPhase)
		{
			phaseImpl.get(currentPhase).exit();
			currentPhase = targetPhase;
			ticksInCurrentPhase = 0;
			phaseImpl.get(targetPhase).enter();
		}
	}

	@Nonnull
	public OpResult prepareNewRound()
	{
		RoundInfo transitionTo = getNextRoundInfo();
		RoundInstance round = createRoundInstance(transitionTo);
		IMapInstance map = createMapInstance(transitionTo);
		IGamemodeInstance gamemode = createGamemodeInstance(round);
		if(gamemode == null)
			return OpResult.FAILURE_GENERIC;

		OpResult assignTeamResult = round.assignTeams(createTeamInstances(transitionTo));
		if(!assignTeamResult.success())
			return assignTeamResult;

		OpResult assignGamemodeResult =	round.assignGamemode(gamemode);
		if(!assignGamemodeResult.success())
			return assignGamemodeResult;

		OpResult assignMapResult = round.assignMap(map);
		if(!assignMapResult.success())
			return assignMapResult;

		currentRoundInstance = round;
		return OpResult.SUCCESS;
	}
	@Nullable
	public IGamemodeInstance createGamemodeInstance(@Nonnull IRoundInstance roundInstance)
	{
		GamemodeInfo gamemode = roundInstance.getDef().gamemode();
		if(!gamemode.factory().isValid(roundInstance.getDef()))
			return null;
		return gamemode.factory().createInstance(roundInstance);
	}
	@Nonnull
	public MapInstance createMapInstance(@Nonnull RoundInfo roundInfo)
	{
		return new MapInstance(roundInfo.map());
	}
	@Nonnull
	public RoundInstance createRoundInstance(@Nonnull RoundInfo roundInfo)
	{
		return new RoundInstance(roundInfo);
	}
	@Nonnull
	public List<ITeamInstance> createTeamInstances(@Nonnull RoundInfo roundInfo)
	{
		List<ITeamInstance> list =new ArrayList<>(roundInfo.teams().size());
		for(TeamInfo teamInfo : roundInfo.teams())
			list.add(new TeamInstance(teamInfo));
		return list;
	}


	@Override @Nonnull
	public OpResult registerGamemode(@Nonnull GamemodeInfo gamemode)
	{
		if(gamemodes.containsKey(gamemode.gamemodeID()))
			return OpResult.FAILURE_DUPLICATE_ID;

		gamemodes.put(gamemode.gamemodeID(), gamemode);
		return OpResult.SUCCESS;
	}
	@Override @Nullable
	public GamemodeInfo getGamemode(@Nonnull String gamemodeID) { return gamemodes.get(gamemodeID); }
	@Override @Nonnull
	public Collection<GamemodeInfo> getAllGamemodes() { return gamemodes.values(); }
	@Override @Nonnull
	public Collection<MapInfo> getAllMaps() { return maps.values(); }
	@Override @Nullable
	public MapInfo getMapData(@Nonnull String mapName) { return maps.get(mapName); }
	@Override @Nonnull
	public ISettings getDefaultSettings() { return defaultMapSettings; }
	@Override @Nonnull
	public Collection<RoundInfo> getMapRotation() { return mapRotation; }
	@Override @Nonnull
	public RoundInfo getCurrentRoundInfo() { return currentRound; }
	@Override @Nonnull
	public RoundInfo getNextRoundInfo()
	{
		if(isRotationEnabled && !mapRotation.isEmpty())
		{
			int index = mapRotation.indexOf(currentRound);
			return mapRotation.get(index + 1);
		}
		return nextRound;
	}


	@Override @Nonnull
	public ERoundPhase getCurrentPhase() { return currentPhase; }
	@Override
	public int getTicksInCurrentPhase() { return ticksInCurrentPhase; }
	@Override @Nullable
	public IRoundInstance getCurrentRound() { return currentRoundInstance; }
	@Override @Nullable
	public IMapInstance getCurrentMap() { return currentRoundInstance.getMap(); }
	@Override @Nullable
	public IGamemodeInstance getCurrentGamemode() { return currentRoundInstance.getGamemode(); }

	@Override @Nonnull
	public OpResult createMap(@Nonnull String mapName)
	{
		if(maps.containsKey(mapName))
			return OpResult.FAILURE_GENERIC;
		OpResult nameCheck = TeamsAPI.isValidMapName(mapName);
		if(nameCheck.failure())
			return nameCheck;

		MapInfo newMap = new MapInfo(mapName, null);
		maps.put(mapName, newMap);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult deleteMap(@Nonnull String mapName)
	{
		return OpResult.SUCCESS;
	}

	@Override @Nonnull
	public OpResult enableMapRotation()
	{
		isRotationEnabled = true;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult disableMapRotation()
	{
		isRotationEnabled = false;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult setMapRotation(@Nonnull List<RoundInfo> rotationEntries)
	{
		mapRotation.clear();
		mapRotation.addAll(rotationEntries);
		return OpResult.SUCCESS;
	}
	@Override @Nullable
	public RoundInfo tryCreateRoundInfo(@Nonnull String mapName, @Nonnull String gamemodeID, @Nonnull String ... teamNames)
	{
		MapInfo mapInfo = getMapData(mapName);
		if(mapInfo == null)
			return null;

		GamemodeInfo gamemodeInfo = getGamemode(gamemodeID);
		if(gamemodeInfo == null)
			return null;

		List<TeamInfo> teamInfos = new ArrayList<>(teamNames.length);
		for(String teamID : teamNames)
		{
			ResourceLocation resLoc = ResourceLocation.tryParse(teamID);
			if(resLoc == null)
				return null;
			teamInfos.add(new TeamInfo(resLoc));
		}
		return new RoundInfo(gamemodeInfo, mapInfo, teamInfos, null);
	}

	@Override @Nonnull
	public OpResult addMapToRotation(@Nonnull RoundInfo round, int positionHint) {

		if(positionHint >= 0)
		{
			if(mapRotation.size() >= positionHint)
			{
				mapRotation.add(positionHint, round);
				return OpResult.SUCCESS;
			}
			else return OpResult.FAILURE_INVALID_MAP_INDEX;
		}
		mapRotation.add(round);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult removeMapFromRotation(int inPosition)
	{
		if(mapRotation.size() > inPosition) {
			mapRotation.remove(inPosition);
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_INVALID_MAP_INDEX;
	}

	@Override @Nonnull
	public OpResult createNewSettings(@Nonnull String settingsName)
	{
		if(settings.containsKey(settingsName))
			return OpResult.FAILURE_GENERIC;
		OpResult nameCheck = TeamsAPI.isValidMapName(settingsName);
		if(nameCheck.failure())
			return nameCheck;

		Settings newSettings = new Settings(defaultMapSettings);
		settings.put(settingsName, newSettings);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult registerIntParameter(@Nonnull String parameterName, int defaultValue)
	{
		return defaultMapSettings.setIntegerParameter(parameterName, defaultValue);
	}
	@Override @Nonnull
	public OpResult registerBooleanParameter(@Nonnull String parameterName, boolean defaultValue)
	{
		return defaultMapSettings.setBooleanParameter(parameterName, defaultValue);
	}
	@Override @Nonnull
	public OpResult setNextRoundInfo(@Nonnull RoundInfo round)
	{
		if(isRotationEnabled)
			return OpResult.FAILURE_GENERIC;
		nextRound = round;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult goToNextRound()
	{

		return OpResult.SUCCESS;
	}


	public void autoAssignPlayer(@Nonnull Player player)
	{
		if(currentRoundInstance != null)
		{
			ITeamInstance bestTeam = getBestTeamFor(player);
			bestTeam.add(player);
		}
	}
	public void playerSelectedTeam(@Nonnull Player player, @Nonnull TeamInfo selection)
	{
		if(currentPhase != ERoundPhase.Gameplay)
			return;


	}
	public void playerSelectedClass(@Nonnull Player player)
	{
		// TODO:
	}
	public void removePlayer(@Nonnull Player player)
	{
		if(currentRoundInstance != null)
		{
			for(ITeamInstance team : currentRoundInstance.getTeams())
				team.remove(player);
		}
	}
	@Nonnull
	public ITeamInstance getBestTeamFor(@Nonnull Player player)
	{
		IGamemodeInstance gamemode = currentRoundInstance.getGamemode();
		if(gamemode != null)
		{
			ITeamInstance gamemodeRecommendation = gamemode.getBestTeamFor(player);
			if(gamemodeRecommendation != null)
				return gamemodeRecommendation;
		}

		ITeamInstance bestTeam = null;
		int numberOfPlayersOnBestTeam = Integer.MAX_VALUE;
		for(ITeamInstance team : currentRoundInstance.getTeams())
		{
			if(team.getMemberPlayers().size() < numberOfPlayersOnBestTeam)
			{
				numberOfPlayersOnBestTeam = team.getMemberPlayers().size();
				bestTeam = team;
			}
		}

		if(bestTeam != null)
			return bestTeam;

		return currentRoundInstance.getTeams().get(0);
	}

	public void receiveSelectTeamMessage(@Nonnull SelectTeamMessage msg, @Nonnull ServerPlayer from)
	{
		playerSelectedTeam(from, msg.getSelection());
	}
	public void receivePlaceVoteMessage(@Nonnull PlaceVoteMessage msg, @Nonnull ServerPlayer from)
	{

	}
}
