package com.flansmod.teams.server;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.*;
import com.flansmod.teams.api.runtime.*;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.TeamsModConfig;
import com.flansmod.teams.common.info.TeamScoreInfo;
import com.flansmod.teams.common.network.toclient.*;
import com.flansmod.teams.server.dimension.ConstructManager;
import com.flansmod.teams.server.dimension.DimensionInstancingManager;
import com.flansmod.teams.common.dimension.TeamsDimensions;
import com.flansmod.teams.common.network.toserver.PlaceVoteMessage;
import com.flansmod.teams.common.network.toserver.SelectTeamMessage;
import com.flansmod.teams.common.network.TeamsModPacketHandler;
import com.flansmod.teams.server.map.MapDetails;
import com.flansmod.teams.server.map.SpawnPointRef;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
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
import java.io.File;
import java.io.IOException;
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

	private final Map<ResourceLocation, IGamemodeFactory> gamemodes = new HashMap<>();
	private final Map<String, MapDetails> maps = new HashMap<>();
	private final Map<String, Settings> settings = new HashMap<>();
	private boolean isRotationEnabled = false;
	private final List<RoundInfo> mapRotation = new ArrayList<>();

	private final Settings defaultMapSettings;
	private final Map<ERoundPhase, Phase> phaseImpl = new HashMap<>();
	private final Map<UUID, PlayerSaveData> playerSaveData = new HashMap<>();

	private final DimensionInstancingManager instanceManager;
	private ISpawnPoint lobbySpawnPoint = new SpawnPointRef(BlockPos.ZERO);

	private RoundInfo currentRound = RoundInfo.invalid;
	private RoundInfo nextRound = RoundInfo.invalid;



	private ERoundPhase currentPhase = ERoundPhase.Inactive;
	private ERoundPhase targetPhase = ERoundPhase.Inactive;
	private long phaseStartedTick = 0L;
	private int ticksInCurrentPhase = 0;
	private RoundInstance currentRoundInstance;
	private ConstructManager constructManager;

	@Nonnull
	public DimensionInstancingManager getInstances() { return instanceManager; }
	@Nonnull
	public ConstructManager getConstructs() { return constructManager; }

	public TeamsManager()
	{
		defaultMapSettings = new Settings();
		settings.put(ISettings.DEFAULT_KEY, defaultMapSettings);
		instanceManager = new DimensionInstancingManager(List.of(
			TeamsDimensions.TEAMS_INSTANCE_A_LEVEL,
			TeamsDimensions.TEAMS_INSTANCE_B_LEVEL),
			TeamsDimensions.TEAMS_LOBBY_LEVEL,
			BlockPos.ZERO);
		constructManager = new ConstructManager(List.of(
			TeamsDimensions.TEAMS_CONSTRUCT_LEVEL),
			TeamsDimensions.TEAMS_LOBBY_LEVEL,
			BlockPos.ZERO);


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
	@Override @Nonnull
	public ResourceKey<Level> getLobbyDimension() { return TeamsDimensions.TEAMS_LOBBY_LEVEL; }
	@Override @Nonnull
	public ISpawnPoint getLobbySpawnPoint() { return lobbySpawnPoint; }
	public void onServerStarted(@Nonnull MinecraftServer server)
	{
		scanForMaps(server);
		loadTeamsFile(server);
		saveTeamsFile(server);

		ServerLevel level = server.getLevel(getLobbyDimension());
		if(level != null)
			lobbySpawnPoint = new SpawnPointRef(level.getSharedSpawnPos());
	}
	private void loadTeamsFile(@Nonnull MinecraftServer server)
	{
		try
		{
			File serverDir = server.getServerDirectory();
			File teamsFile = new File(serverDir.getPath() + "/teams_maps/teams_save.dat");
			if(teamsFile.exists())
			{
				CompoundTag rootTag = NbtIo.readCompressed(teamsFile);
				loadFrom(rootTag);
			}
		}
		catch (IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
		}
	}
	private void saveTeamsFile(@Nonnull MinecraftServer server)
	{
		try
		{
			File serverDir = server.getServerDirectory();
			File teamsFile = new File(serverDir.getPath() + "/teams_maps/teams_save.dat");
			CompoundTag rootTag = new CompoundTag();
			saveTo(rootTag);
			NbtIo.writeCompressed(rootTag, teamsFile);
		}
		catch (IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
		}
	}
	private void scanForMaps(@Nonnull MinecraftServer server)
	{
		try
		{
			File serverDir = server.getServerDirectory();
			File mapsDir = new File(serverDir.getPath() + "/teams_maps/");
			TeamsMod.LOGGER.info("Teams Mod map scan started");
			if(mapsDir.exists() && mapsDir.isDirectory())
			{
				File[] files = mapsDir.listFiles();
				if(files != null)
				{
					for(File mapFolder : files)
					{
						if(mapFolder.isDirectory())
						{
							String mapName = mapFolder.getName();
							File mapData = new File(mapFolder.getPath() + "/map.dat");
							if(mapData.exists())
							{
								MapDetails mapInst = new MapDetails(mapName);
								loadMapData(mapInst);
								maps.put(mapName, mapInst);
								TeamsMod.LOGGER.info("Teams Mod map scan found "+mapName);
							}
						}
					}
				}
			}
			TeamsMod.LOGGER.info("Teams Mod map scan complete, found "+ maps.size()+ " map(s)");
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
		}
	}
	private void loadMapData(@Nonnull MapDetails mapInst) throws IOException
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		File serverDir = server.getServerDirectory();
		File mapData = new File(serverDir.getPath() + "/teams_maps/"+mapInst.getName()+"/map.dat");
		if(mapData.exists())
		{
			CompoundTag rootTag = NbtIo.readCompressed(mapData);
			mapInst.loadFrom(rootTag);
		}
	}
	private void saveMapData(@Nonnull MapDetails mapInst) throws IOException
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		File serverDir = server.getServerDirectory();
		File mapFolder = new File(serverDir.getPath() + "/teams_maps/"+mapInst.getName()+"/");
		mapFolder.mkdirs();

		File mapData = new File(mapFolder.getPath() + "/map.dat");
		CompoundTag rootTag = new CompoundTag();
		mapInst.saveTo(rootTag);
		NbtIo.writeCompressed(rootTag, mapData);
	}

	private void loadFrom(@Nonnull CompoundTag tags)
	{
		if(tags.contains("rotation"))
		{
			CompoundTag rotationTag = tags.getCompound("rotation");
			isRotationEnabled = rotationTag.getBoolean("enabled");
			ListTag roundListTag = rotationTag.getList("rounds", 10);
			for(int i = 0; i < roundListTag.size(); i++)
			{
				CompoundTag roundTag = roundListTag.getCompound(i);
				mapRotation.add(RoundInfo.of(roundTag));
			}
		}
	}
	private void saveTo(@Nonnull CompoundTag tags)
	{
		CompoundTag rotationTag = new CompoundTag();
		rotationTag.putBoolean("enabled", isRotationEnabled);
		ListTag roundListTag = new ListTag();
		for(RoundInfo round : mapRotation)
		{
			CompoundTag roundTag = new CompoundTag();
			round.saveTo(roundTag);
			roundListTag.add(roundTag);
		}
		rotationTag.put("rounds", roundListTag);
		tags.put("rotation", rotationTag);
	}

	private void createInactivePhase()
	{
		phaseImpl.put(ERoundPhase.Inactive, new Phase());
	}
	private void createPreparingPhase()
	{
		phaseImpl.put(ERoundPhase.Preparing, new Phase(){

			private RoundInfo transitionTarget;

			private int timeoutTicks() { return (int)Math.floor(TeamsModConfig.preparingPhaseTimeout.get() * 20d); }
			@Override
			public void enter()
			{
				transitionTarget = getNextRoundInfo();
				announce(Component.translatable("teams.announce.load_map", transitionTarget.mapName()));
				if(TeamsModConfig.useDimensionInstancing.get())
				{
					OpResult prepResult = instanceManager.beginLoad(transitionTarget.mapName());
					if(prepResult.success())
					{
						TeamsMod.LOGGER.info("Started loading instance for map "+transitionTarget.mapName());
					}
					else
					{
						TeamsMod.LOGGER.error("Failed to prepare new round: " + prepResult);
						targetPhase = ERoundPhase.Inactive;
					}
				}
				else
				{
					// TODO: Not sure about using a default dimension here. Do we even support this?
					OpResult prepResult = prepareNewRound(transitionTarget, Level.OVERWORLD);
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
					if(instanceManager.isLoaded(transitionTarget.mapName()))
					{
						TeamsMod.LOGGER.info("Successfully loaded dimension instance in " + (ticksInCurrentPhase / 20) + "s");
						OpResult prepResult = prepareNewRound(transitionTarget, Level.OVERWORLD);
						if(prepResult.success())
						{
							targetPhase = ERoundPhase.Gameplay;
						}
						else
						{
							TeamsMod.LOGGER.error("Failed to prepare new round");
							targetPhase = ERoundPhase.Inactive;
						}
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
				IMapDetails map = createMapInstance(transitionTo, inDimension);
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
				currentRound = transitionTo;
				return OpResult.SUCCESS;
			}
			@Nullable
			public IGamemodeInstance createGamemodeInstance(@Nonnull IRoundInstance roundInstance)
			{
				ResourceLocation gamemodeID = roundInstance.getDef().gamemodeID();
				IGamemodeFactory factory = gamemodes.get(gamemodeID);
				if(factory == null)
					return null;

				if(!factory.isValid(roundInstance.getDef()))
					return null;
				return factory.createInstance(roundInstance);
			}
			@Nonnull
			public MapDetails createMapInstance(@Nonnull RoundInfo roundInfo, @Nonnull ResourceKey<Level> inDimension)
			{
				return new MapDetails(roundInfo.mapName());
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
				for(ResourceLocation teamInfo : roundInfo.teams())
					list.add(new TeamInstance(teamInfo));
				return list;
			}
		});
	}
	private void createGameplayPhase()
	{
		phaseImpl.put(ERoundPhase.Gameplay, new Phase(){
			@Override
			public void enter()
			{
				if(currentRoundInstance !=null)
				{
					currentRoundInstance.begin();
					MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
					if(server != null)
					{
						// Move all players into the lobby dimension
						List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
						for(ServerPlayer player : players)
						{
							// Skip builders
							IPlayerPersistentInfo playerData = getPlayerData(player.getUUID());
							if(playerData != null && playerData.isBuilder())
								continue;

							// Move player into the lobby dimension
							teleportSpawnPlayer(player, getLobbyDimension(), getLobbySpawnPoint());

							// And send them team options
							TeamsModPacketHandler.sendToPlayer(player, createTeamOptionsMsg(true));
						}
					}
				}
				else
				{
					TeamsMod.LOGGER.error("Gameplay phase entered with no current round!");
					targetPhase = ERoundPhase.Inactive;
				}
			}
			@Override
			public void tick()
			{
				IGamemodeInstance gamemode = getCurrentGamemode();
				if(gamemode != null)
				{
					gamemode.tick();
				}
			}
			@Override
			public void exit()
			{
				if(currentRoundInstance !=null)
				{
					currentRoundInstance.end();
				}
			}
			@Override
			public void onPlayerJoined(@Nonnull ServerPlayer player)
			{
				TeamsModPacketHandler.sendToPlayer(player, createTeamOptionsMsg(true));
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
				TeamsModPacketHandler.sendToAll(createScoresMsg(true));
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
				TeamsModPacketHandler.sendToPlayer(player, createScoresMsg(true));
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
				TeamsModPacketHandler.sendToAll(createMapVotingMsg());
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
				TeamsModPacketHandler.sendToPlayer(player, createMapVotingMsg());
			}

			private void selectVotingOptions()
			{

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

		instanceManager.serverTick();
		constructManager.serverTick();

		getPhaseImpl(currentPhase).tick();
		if(targetPhase != currentPhase)
		{
			getPhaseImpl(currentPhase).exit();
			currentPhase = targetPhase;
			ticksInCurrentPhase = 0;
			getPhaseImpl(targetPhase).enter();
			TeamsModPacketHandler.sendToAll(createPhaseUpdateMsg());
		}
	}
	@Override @Nonnull
	public OpResult start()
	{
		if(currentPhase == ERoundPhase.Inactive)
		{
			if(isRotationEnabled)
			{
				if (mapRotation.size() == 0)
					return OpResult.FAILURE_MAP_ROTATION_EMPTY;
			}
			else
			{
				OpResult validateResult = nextRound.validate();
				if(validateResult.failure())
					return validateResult;
			}
			announce(Component.translatable("teams.announce.start"));
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
			announce(Component.translatable("teams.announce.stop"));
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_GENERIC;
	}



	@Override @Nonnull
	public OpResult registerGamemode(@Nonnull ResourceLocation gamemodeID, @Nonnull IGamemodeFactory factory)
	{
		if(gamemodes.containsKey(gamemodeID))
			return OpResult.FAILURE_DUPLICATE_ID;

		gamemodes.put(gamemodeID, factory);
		return OpResult.SUCCESS;
	}
	@Override @Nullable
	public IGamemodeFactory getGamemode(@Nonnull ResourceLocation gamemodeID) { return gamemodes.get(gamemodeID); }
	@Override @Nonnull
	public Collection<ResourceLocation> getAllGamemodes() { return gamemodes.keySet(); }
	@Override @Nonnull
	public Collection<String> getAllMaps() { return maps.keySet(); }
	@Override @Nullable
	public IMapDetails getMapData(@Nonnull String mapName) { return maps.get(mapName); }
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
	public IMapDetails getCurrentMap() { return currentRoundInstance != null ? currentRoundInstance.getMap() : null; }
	@Override @Nullable
	public IGamemodeInstance getCurrentGamemode() { return currentRoundInstance != null ? currentRoundInstance.getGamemode() : null; }
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
		if(!TeamsAPI.isValidMapName(mapName))
			return OpResult.FAILURE_INVALID_MAP_NAME;

		maps.put(mapName, new MapDetails(mapName));
		try
		{
			saveMapData(maps.get(mapName));
		}
		catch (IOException e)
		{
			return OpResult.FAILURE_GENERIC;
		}
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
		saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult disableMapRotation()
	{
		isRotationEnabled = false;
		saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult setMapRotation(@Nonnull List<RoundInfo> rotationEntries)
	{
		mapRotation.clear();
		mapRotation.addAll(rotationEntries);
		saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
		return OpResult.SUCCESS;
	}
	@Override @Nullable
	public RoundInfo tryCreateRoundInfo(@Nonnull String mapName, @Nonnull ResourceLocation gamemodeID, @Nonnull String ... teamNames)
	{
		IMapDetails mapDetails = getMapData(mapName);
		if(mapDetails == null)
			return null;

		IGamemodeFactory gamemodeFactory = getGamemode(gamemodeID);
		if(gamemodeFactory == null)
			return null;

		List<ResourceLocation> teamInfos = new ArrayList<>(teamNames.length);
		for(String teamID : teamNames)
		{
			ResourceLocation resLoc = ResourceLocation.tryParse(teamID);
			if(resLoc == null)
				return null;
			teamInfos.add(resLoc);
		}
		return new RoundInfo(gamemodeID, mapName, teamInfos, TeamsAPI.defaultSettingsName);
	}

	@Override @Nonnull
	public OpResult addMapToRotation(@Nonnull RoundInfo round, int positionHint) {

		if(positionHint >= 0)
		{
			if(mapRotation.size() >= positionHint)
			{
				mapRotation.add(positionHint, round);
				saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
				return OpResult.SUCCESS;
			}
			else return OpResult.FAILURE_INVALID_MAP_INDEX;
		}
		mapRotation.add(round);
		saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult removeMapFromRotation(int inPosition)
	{
		if(mapRotation.size() > inPosition) {
			mapRotation.remove(inPosition);
			saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_INVALID_MAP_INDEX;
	}

	@Override @Nonnull
	public OpResult createNewSettings(@Nonnull String settingsName)
	{
		if(settings.containsKey(settingsName))
			return OpResult.FAILURE_GENERIC;
		if(!TeamsAPI.isValidSettingsName(settingsName))
			return OpResult.FAILURE_INVALID_MAP_NAME;

		Settings newSettings = new Settings(defaultMapSettings);
		settings.put(settingsName, newSettings);
		saveTeamsFile(ServerLifecycleHooks.getCurrentServer());
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
		return instanceManager.printDebug();
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
		TeamsModPacketHandler.sendToPlayer(player, createPhaseUpdateMsg());

		if(currentRoundInstance != null)
		{
			ITeamInstance bestTeam = getBestTeamFor(player);
			bestTeam.add(player);
		}
	}
	public void playerSelectedTeam(@Nonnull ServerPlayer player, @Nonnull ResourceLocation selection)
	{
		if(currentPhase != ERoundPhase.Gameplay)
			return;

		IPlayerGameplayInfo playerData = currentRoundInstance.getPlayerData(player.getUUID());
		if(playerData == null)
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.server_error"));
			return;
		}
		ITeamInstance selectedTeam = currentRoundInstance.getTeam(selection);
		if(selectedTeam == null)
		{
			player.sendSystemMessage(Component.translatable("teams.player_msg.invalid_team"));
			return;
		}

		// Always set the next team
		playerData.setTeamChoice(selection);

		// Then, work out if we do instant-switch
		ITeamInstance previousTeam = currentRoundInstance.getTeamOf(player);
		boolean instantRespawn = previousTeam == null ||
			(getCurrentGamemode() != null && getCurrentGamemode().doInstantRespawn(previousTeam.getTeamID(), selection));

		if(instantRespawn)
		{
			if(previousTeam != null)
				previousTeam.remove(player);

			selectedTeam.add(player);
			forceSpawnPlayer(player);
		}

		if(selectedTeam instanceof TeamInstance teamInst)
			TeamsModPacketHandler.sendToPlayer(player, createPresetLoadoutsMsg(teamInst, true));
	}
	public void playerSelectedClass(@Nonnull ServerPlayer player, int loadoutIndex)
	{
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
	@Nonnull
	public ResourceKey<Level> getSpawnDimensionFor(@Nonnull ServerPlayer player)
	{
		if(isInBuildMode(player.getUUID()))
			return TeamsDimensions.TEAMS_CONSTRUCT_LEVEL;

		ResourceKey<Level> currentDim = instanceManager.dimensionOf(getCurrentMapName());
		if(currentDim != null)
			return currentDim;

		return TeamsDimensions.TEAMS_LOBBY_LEVEL;
	}
	@Nonnull
	public ISpawnPoint getSpawnPointFor(@Nonnull ServerPlayer player)
	{
		if(!isInBuildMode(player.getUUID()) && currentRoundInstance != null)
		{
			IGamemodeInstance gamemode = currentRoundInstance.getGamemode();
			IMapDetails map = currentRoundInstance.getMap();
			if(gamemode != null && map != null)
				return gamemode.getSpawnPoint(map, player);
		}
		return new SpawnPointRef(BlockPos.ZERO);
	}

	private void forceSpawnPlayer(@Nonnull ServerPlayer player)
	{
		ISpawnPoint spawnPoint = getSpawnPointFor(player);
		player.setRespawnPosition(getSpawnDimensionFor(player), spawnPoint.getPos(), 0f, true, false);
		if(player.isAlive())
		{
			player.kill();
		}
	}
	private void teleportSpawnPlayer(@Nonnull ServerPlayer player, @Nonnull ResourceKey<Level> dimension, @Nonnull ISpawnPoint spawn)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(dimension);
			if(level != null)
			{
				player.teleportTo(level, spawn.getPos().getX(), spawn.getPos().getY(), spawn.getPos().getZ(), 0f, 0f);
			}
		}
	}

	public void receiveSelectTeamMessage(@Nonnull SelectTeamMessage msg, @Nonnull ServerPlayer from)
	{
		playerSelectedTeam(from, currentRound.teams().get(msg.getSelection()));
	}
	public void receivePlaceVoteMessage(@Nonnull PlaceVoteMessage msg, @Nonnull ServerPlayer from)
	{
		IPlayerGameplayInfo playerInfo = currentRoundInstance.getPlayerData(from.getUUID());
		if(playerInfo != null)
		{
			playerInfo.setVote(msg.voteIndex);
		}
	}
	@Nonnull
	private TeamOptionsMessage createTeamOptionsMsg(boolean andOpenGUI)
	{
		TeamOptionsMessage msg = new TeamOptionsMessage();
		msg.andOpenGUI = andOpenGUI;
		IRoundInstance round = getCurrentRound();
		if(round != null)
		{
			for(ITeamInstance team : round.getTeams())
				msg.teamOptions.add(team.getTeamID());
		}
		return msg;
	}
	@Nonnull
	private PresetLoadoutOptionsMessage createPresetLoadoutsMsg(@Nonnull TeamInstance team, boolean andOpenGUI)
	{
		PresetLoadoutOptionsMessage msg = new PresetLoadoutOptionsMessage();
		msg.andOpenGUI = andOpenGUI;
		msg.loadoutOptions.addAll(team.presetLoadouts);
		return msg;
	}
	@Nonnull
	private PhaseUpdateMessage createPhaseUpdateMsg()
	{
		return new PhaseUpdateMessage(currentPhase, phaseStartedTick, getPhaseImpl(currentPhase).getLength());
	}
	@Nonnull
	private MapVotingOptionsMessage createMapVotingMsg()
	{
		return new MapVotingOptionsMessage();
	}
	@Nonnull
	private DisplayScoresMessage createScoresMsg(boolean andOpenGUI)
	{
		DisplayScoresMessage msg = new DisplayScoresMessage();
		msg.teamScores.add(new TeamScoreInfo());
		return msg;
	}
	public void announce(@Nonnull Component msg)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			server.getPlayerList().broadcastSystemMessage(msg, true);
		}
	}
}
