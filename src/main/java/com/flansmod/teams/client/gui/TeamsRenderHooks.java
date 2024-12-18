package com.flansmod.teams.client.gui;

import com.flansmod.teams.client.TeamsModClient;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class TeamsRenderHooks
{
	public final TeamsHUD hud = new TeamsHUD();
	public final BuilderAdminHUD builderHud = new BuilderAdminHUD();

	public TeamsRenderHooks()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void clientTick(@Nonnull TickEvent.ClientTickEvent event)
	{

	}
	@SubscribeEvent
	public void renderTick(@Nonnull TickEvent.RenderTickEvent event)
	{

	}
	@SubscribeEvent
	public void onRenderPlayer(@Nonnull RenderPlayerEvent.Pre event)
	{

	}
	@SubscribeEvent
	public void onRenderNametag(@Nonnull RenderNameTagEvent event)
	{

	}
	@SubscribeEvent
	public void onRenderHUD(@Nonnull RenderGuiOverlayEvent event)
	{
		if (event instanceof RenderGuiOverlayEvent.Pre)
		{
			if("hotbar".equals(event.getOverlay().id().getPath()))
			{
				if(TeamsModClient.MANAGER.isBuilder())
					builderHud.render(event);
				else
					hud.renderTeamHeader(event);
			}
		}
	}
}
