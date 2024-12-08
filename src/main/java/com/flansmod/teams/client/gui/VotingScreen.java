package com.flansmod.teams.client.gui;

import com.flansmod.teams.client.TeamsClientManager;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.client.gamemode.IClientGamemode;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.info.MapVotingOption;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class VotingScreen extends AbstractTeamsScreen
{
	public static final ResourceLocation texture = new ResourceLocation(TeamsMod.MODID, "gui/vote.png");
	private static int getGuiHeight() { return 29 + 24 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getMapVoteOptions().size(); }

	public Button[] voteButtons;
	public int myVote = 0;

	public VotingScreen(@Nonnull Component title)
	{
		super(title, 256, getGuiHeight());
	}

	@Override
	protected void init()
	{
		super.init();

		voteButtons = new Button[getNumLines()];
		for(int i = 0; i < getNumLines(); i++)
		{
			final int index = i;
			voteButtons[i] = Button.builder(
					Component.empty(),
					(t) -> {
						myVote = index;
						TeamsModClient.MANAGER.placeVote(index);
					})
				.bounds(xOrigin + 9, yOrigin + 24 + 24 * i, 73, 20)
				.build();
		}
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float f)
	{
		super.render(graphics, mouseX, mouseY, f);

		TeamsClientManager teams = TeamsModClient.MANAGER;

		graphics.blit(texture, xOrigin, yOrigin, 0, 0, 256, 22);
		for(int p = 0; p < getNumLines(); p++)
			graphics.blit(texture, xOrigin, yOrigin + 22 + 24 * p, 0, 23, 256, 24);
		graphics.blit(texture, xOrigin, yOrigin + 22 + 24 * getNumLines(), 0, 73, 256, 7);

		graphics.drawString(font, Component.translatable("teams.vote_next_round"), xOrigin + 8, yOrigin + 8, 0xffffff);
		graphics.drawString(font, (teams.currentState.ticksRemaining / 20) + "", xOrigin + 256 - 20, yOrigin + 8, 0xffffff);

		for(int p = 0; p < teams.getMapVoteOptions().size(); p++)
		{
			MapVotingOption option = teams.getMapVoteOptions().get(p);
			IClientGamemode clientGamemode = teams.getClientGamemode(option.gamemodeID);
			graphics.drawString(font, option.mapLongName, xOrigin + 10, yOrigin + 25 + 24 * p, 0xffffff);
			if(clientGamemode != null)
				graphics.drawString(font, clientGamemode.getSummary(option.mapLongName, option.teamIDs), xOrigin + 10, yOrigin + 35 + 24 * p, 0xffffff);

			graphics.drawCenteredString(font, (myVote == p + 1 ? "\u00a72" : "") + option.numVotes, xOrigin + 196, yOrigin + 31 + 24 * p, 0xffffff);
		}

	}
}
