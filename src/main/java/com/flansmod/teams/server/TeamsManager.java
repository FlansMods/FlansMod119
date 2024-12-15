package com.flansmod.teams.server;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.*;
import com.flansmod.teams.api.runtime.*;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.TeamsModConfig;
import com.flansmod.teams.common.dimension.DimensionInstancingManager;
import com.flansmod.teams.common.dimension.TeamsDimensions;
import com.flansmod.teams.common.network.toclient.DisplayScoresMessage;
import com.flansmod.teams.common.network.toclient.MapVotingOptionsMessage;
import com.flansmod.teams.common.network.toclient.PhaseUpdateMessage;
import com.flansmod.teams.common.network.toserver.PlaceVoteMessage;
import com.flansmod.teams.common.network.toserver.SelectTeamMessage;
import com.flansmod.teams.common.network.TeamsModPacketHandler;
import com.flansmod.teams.server.map.SingleDimensionMapInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

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
		public long getLength() { return 0L; }
	}

	private final Map<String, GamemodeInfo> gamemodes = new HashMap<>();
	private final Map<String, MapInfo> maps = new HashMap<>();
	private final Map<String, Settings> settings = new HashMap<>();
	private final Settings defaultMapSettings;
	private final Map<ERoundPhase, Phase> phaseImpl = new HashMap<>();
	private final Map<UUID, PlayerSaveData> playerSaveData = new HashMap<>();

	private final Map<String, IGamemodeFactory> gamemodeFactories = new HashMap<>();

	private boolean isRotationEnabled = false;
	private final List<RoundInfo> mapRotation = new ArrayList<>();
	private final DimensionInstancingManager instanceManager;
	private final DimensionInstancingManager constructManager;
	private int reservedInstanceID = DimensionInstancingManager.INVALID_INSTANCE;

	private RoundInfo currentRound = RoundInfo.invalid;
	private RoundInfo nextRound = RoundInfo.invalid;

	private ERoundPhase currentPhase = ERoundPhase.Inactive;
	private ERoundPhase targetPhase = ERoundPhase.Inactive;
	private long phaseStartedTick = 0L;
	private int ticksInCurrentPhase = 0;
	private RoundInstance currentRoundInstance;

	@Nonnull
	public DimensionInstancingManager getInstances() { return instanceManager; }
	@Nonnull
	public DimensionInstancingManager getConstructs() { return constructManager; }

	public TeamsManager()
	{
		defaultMapSettings = new Settings();
		settings.put(ISettings.DEFAULT_KEY, defaultMapSettings);
		instanceManager = new DimensionInstancingManager(List.of(
			TeamsDimensions.TEAMS_INSTANCE_A_LEVEL,
			TeamsDimensions.TEAMS_INSTANCE_B_LEVEL),
			TeamsDimensions.TEAMS_LOBBY_LEVEL,
			BlockPos.ZERO);
		constructManager = new DimensionInstancingManager(List.of(
			TeamsDimensions.TEAMS_CONSTRUCT_LEVEL),
			TeamsDimensions.TEAMS_LOBBY_LEVEL,
			BlockPos.ZERO);

		// TODO: Scan for teams_maps/<map_name>.dat
		// Then load teams_maps/<map_name>/region/...

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
		phaseImpl.put(ERoundPhase.Preparing, new Phase(){
			private int timeoutTicks() { return (int)Math.floor(TeamsModConfig.preparingPhaseTimeout.get() * 20d); }
			@Override
			public void enter()
			{
				RoundInfo transitionTo = getNextRoundInfo();
				if(TeamsModConfig.useDimensionInstancing.get())
				{
					reservedInstanceID = instanceManager.reserveInstance();
					if(reservedInstanceID == DimensionInstancingManager.INVALID_INSTANCE)
					{
						TeamsMod.LOGGER.error("Failed to reserve Dimension instance");
						targetPhase = ERoundPhase.Inactive;
					}
					else
					{
						ResourceKey<Level> dimension = instanceManager.getDimension(reservedInstanceID);
						if(dimension != null)
						{
							OpResult prepResult = prepareNewRound(transitionTo, dimension);
							if(!prepResult.success())
							{
								TeamsMod.LOGGER.error("Failed to prepare new round");
								targetPhase = ERoundPhase.Inactive;
							}

							if(!instanceManager.beginLoadLevel(reservedInstanceID, getCurrentMapName()))
							{
								TeamsMod.LOGGER.error("Failed to begin level load to Dimension instance");
								targetPhase = ERoundPhase.Inactive;
							}
						}
						else
						{
							TeamsMod.LOGGER.error("Failed to acquire Dimension for loading level into");
							targetPhase = ERoundPhase.Inactive;
						}
					}
				}
				else
				{
					// TODO: Not sure about using a default dimension here. Do we even support this?
					OpResult prepResult = prepareNewRound(transitionTo, Level.OVERWORLD);
					if(!prepResult.success())
					{
						TeamsMod.LOGGER.error("Failed to prepare new round");
						targetPhase = ERoundPhase.Inactive;
					}
				}
			}
			@Override
			public void tick()
			{
				if(targetPhase == ERoundPhase.Inactive)
					return;

				if(ticksInCurrentPhase >= timeoutTicks())
				{
					TeamsMod.LOGGER.error("Preparing phase timed out!");
					targetPhase = ERoundPhase.Inactive;
				}

				if(TeamsModConfig.useDimensionInstancing.get())
				{
					if(reservedInstanceID != DimensionInstancingManager.INVALID_INSTANCE)
					{
						if(instanceManager.checkLoadLevelComplete(reservedInstanceID))
						{
							TeamsMod.LOGGER.info("Successfully loaded dimension instance in " + (ticksInCurrentPhase / 20) + "s");
							targetPhase = ERoundPhase.Gameplay;
						}
					}
					else
					{
						TeamsMod.LOGGER.error("Somehow waiting in Preparing phase with no valid dimension reservation");
						targetPhase = ERoundPhase.Inactive;
					}
				}
				else
				{
					// There's nothing to wait for if we aren't instancing
					targetPhase = ERoundPhase.Gameplay;
				}
			}
			@Nonnull
			public OpResult prepareNewRound(@Nonnull RoundInfo transitionTo,
											@Nonnull ResourceKey<Level> inDimension)
			{
				RoundInstance round = createRoundInstance(transitionTo);
				IMapInstance map = createMapInstance(transitionTo, inDimension);
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
				IGamemodeFactory factory = gamemodeFactories.get(gamemode.gamemodeID());
				if(factory == null)
					return null;

				if(!factory.isValid(roundInstance.getDef()))
					return null;
				return factory.createInstance(roundInstance);
			}
			@Nonnull
			public SingleDimensionMapInstance createMapInstance(@Nonnull RoundInfo roundInfo, @Nonnull ResourceKey<Level> inDimension)
			{
				return new SingleDimensionMapInstance(roundInfo.map(), inDimension);
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

		phaseImpl.put(ERoundPhase.DisplayScores, new Phase(){
			private int durationTicks() { return (int)Math.floor(TeamsModConfig.displayScoresPhaseDuration.get() * 20d); }
			private boolean isVotingPhaseEnabled() { return TeamsModConfig.mapVoteEnabled.get(); }

			@Override
			public void enter()
			{
				TeamsModPacketHandler.sendToAll(createMsg());
			}
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= durationTicks())
					targetPhase = isVotingPhaseEnabled() ? ERoundPhase.MapVote : ERoundPhase.Cleanup;
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

		phaseImpl.put(ERoundPhase.MapVote, new Phase(){
			private int durationTicks() { return (int)Math.floor(TeamsModConfig.mapVotePhaseDuration.get() * 20d); }

			@Override
			public void enter()
			{
				selectVotingOptions();
				TeamsModPacketHandler.sendToAll(createMsg());
			}
			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= durationTicks())
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
		phaseImpl.put(ERoundPhase.Cleanup, new Phase(){
			private int timeoutTicks() { return (int)Math.floor(TeamsModConfig.cleanupPhaseTimeout.get() * 20d); }

			@Override
			public void tick()
			{
				if(ticksInCurrentPhase >= timeoutTicks())
				{
					TeamsMod.LOGGER.error("Cleanup phase timed out!");
					targetPhase = ERoundPhase.Inactive;
				}
			}
		});
	}

	@Nonnull
	private Phase getPhaseImpl(@Nonnull ERoundPhase phase) {
		return phaseImpl.get(currentPhase);
	}
	public void serverTick()
	{
		ticksInCurrentPhase++;

		getPhaseImpl(currentPhase).tick();
		if(targetPhase != currentPhase)
		{
			getPhaseImpl(currentPhase).exit();
			currentPhase = targetPhase;
			ticksInCurrentPhase = 0;
			getPhaseImpl(targetPhase).enter();
		}
	}
	@Override @Nonnull
	public OpResult start()
	{
		if(currentPhase == ERoundPhase.Inactive)
		{
			targetPhase = ERoundPhase.Preparing;
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_GENERIC;
	}
	@Override @Nonnull
	public OpResult stop()
	{
		if(currentPhase != ERoundPhase.Inactive)
		{
			targetPhase = ERoundPhase.Inactive;
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_GENERIC;
	}



	@Override @Nonnull
	public OpResult registerGamemode(@Nonnull GamemodeInfo gamemode)
	{
		if(gamemodes.containsKey(gamemode.gamemodeID()))
			return OpResult.FAILURE_DUPLICATE_ID;

		gamemodes.put(gamemode.gamemodeID(), gamemode);
		return OpResult.SUCCESS;
	}
	public void registerGamemode(@Nonnull GamemodeInfo gamemode, @Nonnull IGamemodeFactory factory)
	{
		if(registerGamemode(gamemode).success())
			registerGamemodeFactory(gamemode.gamemodeID(), factory);
	}
	public void registerGamemodeFactory(@Nonnull String gamemodeID, @Nonnull IGamemodeFactory factory)
	{
		gamemodeFactories.put(gamemodeID, factory);
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
	@Override
	public boolean isInBuildMode(@Nonnull UUID playerID) { return getOrCreatePlayerData(playerID).isBuilder(); }
	@Override @Nonnull
	public OpResult setBuildMode(@Nonnull UUID playerID, boolean set)
	{
		getOrCreatePlayerData(playerID).setBuilder(set);
		return OpResult.SUCCESS;
	}

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
	@Override @Nonnull
	public List<String> getDimensionInfo()
	{
		return instanceManager.getInfo();
	}

	public void onPlayerLogin(@Nonnull ServerPlayer player)
	{
		boolean isBuilder = isInBuildMode(player.getUUID());
		boolean shouldStartInLobby = !isBuilder &&
			(currentPhase == ERoundPhase.Inactive
			? TeamsModConfig.startInLobbyDimensionWhenTeamsInactive.get()
			: TeamsModConfig.startInLobbyDimension.get());
		if(shouldStartInLobby)
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerLevel lobbyLevel = server.getLevel(TeamsDimensions.TEAMS_LOBBY_LEVEL);
			if(lobbyLevel != null)
			{
				Entity teleportedPlayer = player.changeDimension(lobbyLevel, new ITeleporter()
				{
					@Override
					public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
					{
						return ITeleporter.super.placeEntity(entity, currentWorld, destWorld, yaw, repositionEntity);
					}
				});
				if(teleportedPlayer != null)
				{
					player.setRespawnPosition(TeamsDimensions.TEAMS_LOBBY_LEVEL, player.getOnPos(), player.getYRot(), true, false);
				}
			}
		}
		sendPhaseUpdateTo(player);

		if(currentRoundInstance != null)
		{
			ITeamInstance bestTeam = getBestTeamFor(player);
			bestTeam.add(player);
		}
	}
	public void playerSelectedTeam(@Nonnull ServerPlayer player, @Nonnull TeamInfo selection)
	{
		if(currentPhase != ERoundPhase.Gameplay)
			return;


	}
	public void playerSelectedClass(@Nonnull ServerPlayer player, int loadoutIndex)
	{
		// TODO:

		if(currentPhase != ERoundPhase.Gameplay)
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.invalid_phase"));
			return;
		}

		ITeamInstance team = currentRoundInstance.getTeamOf(player);
		if(team == null)
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.not_on_team"));
			return;
		}

		if(loadoutIndex >= team.getNumPresetLoadouts())
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.invalid_loadout"));
			return;
		}

		IPlayerGameplayInfo playerData = currentRoundInstance.getPlayerData(player.getUUID());
		if(playerData == null)
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.server_error"));
			return;
		}

		OpResult choiceResult = playerData.setLoadoutChoice(loadoutIndex);
		if(choiceResult.failure())
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.server_error"));
			return;
		}

		// TODO: Ranked handling
		//IPlayerPersistentInfo playerSaveData = getOrCreatePlayerData(player.getUUID());
	}

	@Nonnull
	public PlayerSaveData getOrCreatePlayerData(@Nonnull UUID playerID)
	{
		if(!playerSaveData.containsKey(playerID))
			playerSaveData.put(playerID, new PlayerSaveData(playerID));

		return playerSaveData.get(playerID);
	}
	@Override @Nullable
	public IPlayerPersistentInfo getPlayerData(@Nonnull UUID playerID)
	{
		return playerSaveData.get(playerID);
	}
	public void removePlayer(@Nonnull ServerPlayer player)
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
		playerSelectedTeam(from, currentRound.teams().get(msg.getSelection()));
	}
	public void receivePlaceVoteMessage(@Nonnull PlaceVoteMessage msg, @Nonnull ServerPlayer from)
	{

	}

	public void sendPhaseUpdateTo(@Nonnull ServerPlayer player)
	{
		TeamsModPacketHandler.sendToPlayer(player, createPhaseUpdate());
	}
	@Nonnull
	private PhaseUpdateMessage createPhaseUpdate() {
		return new PhaseUpdateMessage(currentPhase, phaseStartedTick, getPhaseImpl(currentPhase).getLength());
	}
}
