package com.flansmod.teams.client.gui;

import com.flansmod.teams.api.TeamInfo;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.TeamsMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class ChooseTeamScreen extends AbstractTeamsScreen
{
	private static final ResourceLocation texture = new ResourceLocation(TeamsMod.MODID, "gui/teams.png");
	private Button[] selectButtons;
	private static int getGuiHeight() { return 34 + 24 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getNumTeamOptions(); }

	public ChooseTeamScreen(@Nonnull Component title)
	{
		super(title, 256, getGuiHeight());
	}

	@Override
	protected void init()
	{
		super.init();

		selectButtons = new Button[getNumLines()];
		for(int i = 0; i < getNumLines(); i++)
		{
			final int index = i;
			selectButtons[i] = Button.builder(
				Component.empty(),
				(t) -> {
					TeamsModClient.MANAGER.sendTeamChoice(index);
				})
				.bounds(xOrigin + 9, yOrigin + 24 + 24 * i, 73, 20)
				.build();
		}

	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int x, int y, float f)
	{
		super.render(graphics, x, y, f);

		graphics.blit(texture, xOrigin, yOrigin, 0, 0, imageWidth, 22);
		graphics.blit(texture, xOrigin, yOrigin + imageWidth - 6, 0, 73, imageWidth, 7);

		List<TeamInfo> options = TeamsModClient.MANAGER.getTeamOptions();
		for(int i = 0; i < options.size(); i++)
		{
			graphics.blit(texture, xOrigin, yOrigin + 22 + 24 * i, 0, 23, imageWidth, 24);
		}
	}

}
