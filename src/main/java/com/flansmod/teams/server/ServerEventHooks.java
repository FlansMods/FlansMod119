package com.flansmod.teams.server;

import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.IPlayerBuilderSettings;
import com.flansmod.teams.api.admin.IPlayerPersistentInfo;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.runtime.IGamemodeInstance;
import com.flansmod.teams.api.runtime.IPlayerGameplayInfo;
import com.flansmod.teams.api.runtime.IRoundInstance;
import com.flansmod.teams.api.runtime.ITeamsRuntime;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.TeamsModConfig;
import com.flansmod.teams.common.dimension.TeamsDimensions;
import com.flansmod.teams.server.dimension.ConstructManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerEventHooks
{
	private final TeamsManager manager;

	private enum EDimensionSet
	{
		Lobby,
		Instance,
		Construct,

		NonTeams,
	}

	public ServerEventHooks(@Nonnull TeamsManager teamsManager)
	{
		manager = teamsManager;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onServerStart(@Nonnull ServerStartedEvent event)
	{
		manager.onServerStarted(event.getServer());
	}
	@SubscribeEvent
	public void onServerTick(@Nonnull TickEvent.ServerTickEvent event)
	{
		manager.serverTick();
	}

	@SubscribeEvent
	public void onPlayerLogin(@Nonnull PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.getEntity() instanceof ServerPlayer serverPlayer)
		{
			manager.onPlayerLogin(serverPlayer);

			EDimensionSet enterIntoDimType = categorizeDimension(serverPlayer.level().dimension());
			switch(enterIntoDimType)
			{
				case Construct, Instance -> {
					TeamsMod.MANAGER.sendPlayerToLobby(serverPlayer);
				}
			}
		}

	}
	@SubscribeEvent
	public void onPlayerLogout(@Nonnull PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(event.getEntity() instanceof ServerPlayer serverPlayer)
			manager.removePlayer(serverPlayer);
	}
	@Nonnull
	private EDimensionSet categorizeDimension(@Nonnull ResourceKey<Level> dimension)
	{
		if(dimension.equals(TeamsDimensions.TEAMS_LOBBY_LEVEL))
			return EDimensionSet.Lobby;
		if(manager.getConstructs().managesDimension(dimension))
			return EDimensionSet.Construct;
		if(manager.getInstances().managesDimension(dimension))
			return EDimensionSet.Instance;

		return EDimensionSet.NonTeams;
	}
	@SubscribeEvent
	public void beforePlayerChangeDimension(@Nonnull EntityTravelToDimensionEvent event)
	{
		if(event.getEntity() instanceof ServerPlayer player)
		{
			IPlayerPersistentInfo playerData = manager.getOrCreatePlayerData(player.getUUID());
			ResourceKey<Level> from = event.getEntity().level().dimension();
			ResourceKey<Level> to = event.getDimension();

			EDimensionSet toSet = categorizeDimension(to);
			EDimensionSet fromSet = categorizeDimension(from);
			if(toSet == EDimensionSet.NonTeams && fromSet == EDimensionSet.NonTeams)
			{
				// We don't care if you are hopping other dimensions outside our control
				return;
			}

			switch(fromSet)
			{
				case Construct -> {
					String mapName = manager.getConstructs().getMapLoadedIn(to);
					if(playerData.isBuilder())
					{
						TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" exited a construct");
						IPlayerBuilderSettings builderSettings = playerData.getBuilderSettings();
						if(builderSettings != null && mapName != null)
						{
							builderSettings.setLastPositionInConstruct(mapName, player.getOnPos());
						}
					}
					else
						TeamsMod.LOGGER.warn("[Dimension event] "+player.getName().getString()+" (NOT A BUILDER) exited a construct");
				}
				case Instance -> {
					TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" exited an instance");
				}
				case Lobby -> {
					TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" exited the lobby");
				}
				case NonTeams -> {
					TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" is leaving a non-TeamsMod dimension");
				}
			}
		}
	}
	@SubscribeEvent
	public void onPlayerChangeDimension(@Nonnull PlayerEvent.PlayerChangedDimensionEvent event)
	{
		if(!(event.getEntity() instanceof ServerPlayer player))
			return;

		IPlayerPersistentInfo playerData = manager.getOrCreatePlayerData(player.getUUID());
		ResourceKey<Level> from = event.getFrom();
		ResourceKey<Level> to = event.getTo();

		EDimensionSet toSet = categorizeDimension(to);
		EDimensionSet fromSet = categorizeDimension(from);
		if(toSet == EDimensionSet.NonTeams && fromSet == EDimensionSet.NonTeams)
		{
			// We don't care if you are hopping other dimensions outside our control
			return;
		}


		switch(toSet)
		{
			// If we are entering a construct, we must be a builder
			case Construct -> {
				String mapName = manager.getConstructs().getMapLoadedIn(to);
				if(playerData.isBuilder())
				{
					TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" entered a construct");
					IPlayerBuilderSettings builderSettings = playerData.getBuilderSettings();
					if(builderSettings != null && mapName != null)
					{
						BlockPos spawnPos = builderSettings.getLastPositionInConstruct(mapName);
						if(spawnPos != null)
							player.teleportTo(spawnPos.getX()+0.5d, spawnPos.getY(), spawnPos.getZ()+0.5d);
					}
				}
				else
					TeamsMod.LOGGER.warn("[Dimension event] "+player.getName().getString()+" (NOT A BUILDER) entered a construct");

				manager.onPlayerEnteredConstruct(player);
			}
			case Lobby -> {
				TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" entered the lobby");
				manager.onPlayerEnteredLobby(player);
			}
			case Instance -> {
				IRoundInstance currentRound = manager.getCurrentRound();
				if(currentRound != null)
				{
					IPlayerGameplayInfo playerGameplayData = currentRound.getPlayerData(player.getUUID());
					if(playerGameplayData != null)
					{
						ResourceLocation teamID = playerGameplayData.getTeamChoice();
						boolean isValidTeam = TeamsAPI.isValidTeamID(teamID);
						if(isValidTeam)
						{
							TeamsMod.LOGGER.info("[Dimension event] "+player.getName().getString()+" entered an instance on team "+teamID);
							manager.onPlayerEnteredCurrentInstance(player);
						}
					}
				}
				else
					TeamsMod.LOGGER.warn("[Dimension event] "+player.getName().getString()+" entered an instance BUT TEAMS IS NOT RUNNING");
			}
		}



	}

	@SubscribeEvent
	public void onPlayerInteract(@Nonnull PlayerInteractEvent.EntityInteract event)
	{

	}

	@SubscribeEvent
	public void onDamageDealt(@Nonnull LivingDamageEvent event)
	{
		IGamemodeInstance gamemode = getCurrentGamemode();
		if(gamemode != null)
		{
			if(event.getEntity() instanceof ServerPlayer player)
				gamemode.playerDamaged(player, event.getSource(), event.getAmount());
		}
	}
	@SubscribeEvent
	public void onEntityKilled(@Nonnull LivingDeathEvent event)
	{
		IRoundInstance round = manager.getCurrentRound();
		IGamemodeInstance gamemode = getCurrentGamemode();
		IMapDetails map = manager.getCurrentMap();
		if(round == null || gamemode == null || map == null)
			return;
		if(!(event.getEntity() instanceof ServerPlayer player))
			return;
		if(!round.isParticipating(player.getUUID()))
			return;

		gamemode.playerKilled(player, event.getSource());
		ISpawnPoint nextSpawnPoint = gamemode.getSpawnPoint(map, player);
		player.setRespawnPosition(gamemode.getDimension().dimension(), nextSpawnPoint.getPos(), 0f, true, false);
	}

	@Nullable
	private IGamemodeInstance getCurrentGamemode()
	{
		if(!TeamsAPI.isInitialized())
			return null;
		ITeamsRuntime runtime = TeamsAPI.getRuntime();
		if(runtime == null)
			return null;

		return runtime.getCurrentGamemode();
	}
}
