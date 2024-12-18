package com.flansmod.teams.client.gui;

import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.teams.client.TeamsModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import javax.annotation.Nonnull;

public class BuilderAdminHUD
{
	public void render(@Nonnull RenderGuiOverlayEvent event)
	{
		GuiGraphics graphics = event.getGuiGraphics();
		Font font = Minecraft.getInstance().font;
		Player player = MinecraftHelpers.getClient().player;
		if (player == null)
			return;
		if(!TeamsModClient.MANAGER.isBuilder)
			return;

		int i = MinecraftHelpers.getClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.getClient().getWindow().getGuiScaledHeight();



	}
}
