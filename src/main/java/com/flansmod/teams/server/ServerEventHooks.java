package com.flansmod.teams.server;

import com.flansmod.teams.api.runtime.IGamemodeInstance;
import com.flansmod.teams.api.runtime.ITeamsRuntime;
import com.flansmod.teams.api.TeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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
			manager.onPlayerLogin(serverPlayer);
	}
	@SubscribeEvent
	public void onPlayerLogout(@Nonnull PlayerEvent.PlayerLoggedOutEvent event)
	{
		if(event.getEntity() instanceof ServerPlayer serverPlayer)
			manager.removePlayer(serverPlayer);
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
		IGamemodeInstance gamemode = getCurrentGamemode();
		if(gamemode != null)
		{
			if(event.getEntity() instanceof ServerPlayer player)
				gamemode.playerKilled(player, event.getSource());
		}
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
