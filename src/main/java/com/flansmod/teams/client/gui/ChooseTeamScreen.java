package com.flansmod.teams.client.gui;

import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.TeamsMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class ChooseTeamScreen extends AbstractTeamsScreen
{
	private static final ResourceLocation texture = new ResourceLocation(TeamsMod.MODID, "textures/gui/teams.png");
	private Button[] selectButtons;
	private static int getGuiHeight() { return 29 + 24 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getNumTeamOptions(); }

	public ChooseTeamScreen(@Nonnull Component title)
	{
		super(title, 256, getGuiHeight());
	}

	@Override
	protected void init()
	{
		super.init();

		xOrigin = width / 2 - 256/2;
		yOrigin = height / 2 - getGuiHeight()/2;

		List<ResourceLocation> options = TeamsModClient.MANAGER.getTeamOptions();
		selectButtons = new Button[getNumLines()];
		for(int i = 0; i < getNumLines(); i++)
		{
			final int index = i;
			selectButtons[i] = Button.builder(
				Component.translatable(options.get(i).toLanguageKey("team")),
				(t) -> {
					TeamsModClient.MANAGER.sendTeamChoice(index);
				})
				.bounds(xOrigin + 10, yOrigin + 24 + 24 * i, 236, 20)
				.build();
			addRenderableWidget(selectButtons[i]);
		}

	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int x, int y, float f)
	{

		xOrigin = width / 2 - 256/2;
		yOrigin = height / 2 - getGuiHeight()/2;


		graphics.drawString(font, Component.translatable("teams.select_team.title"), xOrigin + 8, yOrigin + 8, 0x404040, true);
		graphics.drawString(font, Component.translatable("teams.select_team.title"), xOrigin + 7, yOrigin + 7, 0xffffff, true);

		graphics.blit(texture, xOrigin, yOrigin, 0, 0, imageWidth, 22);
		graphics.blit(texture, xOrigin, yOrigin + getGuiHeight() - 7, 0, 73, imageWidth, 7);

		List<ResourceLocation> options = TeamsModClient.MANAGER.getTeamOptions();
		for(int i = 0; i < options.size(); i++)
		{
			graphics.blit(texture, xOrigin, yOrigin + 22 + 24 * i, 0, 23, imageWidth, 24);




		}
		super.render(graphics, x, y, f);
	}
}
