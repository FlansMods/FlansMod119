package com.flansmod.teams.client.gui;

import com.flansmod.teams.common.TeamsMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class AbstractTeamsScreen extends Screen
{
	//private static final ResourceLocation loudoutBoxes = new ResourceLocation("flansmod", "gui/LandingPage.png");
	//private static final ResourceLocation loadoutEditor = new ResourceLocation("flansmod", "gui/LoadoutEditor.png");

	private static final ResourceLocation ranksTexture = new ResourceLocation(TeamsMod.MODID, "gui/ranks.png");
	protected int xOrigin;
	protected int yOrigin;
	protected int imageWidth;
	protected int imageHeight;

	protected AbstractTeamsScreen(@Nonnull Component title, int guiWidth, int guiHeight)
	{
		super(title);
		imageWidth = guiWidth;
		imageHeight = guiHeight;

		xOrigin = width / 2 - guiWidth / 2;
		yOrigin = height / 2 - guiHeight / 2;
	}

	protected void drawRankIcon(@Nonnull GuiGraphics graphics, int rank, int prestige, int x, int y, boolean doubleSize)
	{
		if(doubleSize)
			graphics.blit(ranksTexture, xOrigin + x, yOrigin + y, rank * 32, prestige * 32, 32, 32, 1024, 512);
		else
			graphics.blit(ranksTexture, xOrigin + x, yOrigin + y, rank * 16, prestige * 16, 16, 16, 512, 256);
	}
}
