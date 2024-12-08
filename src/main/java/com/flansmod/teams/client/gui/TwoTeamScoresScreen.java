package com.flansmod.teams.client.gui;

import com.flansmod.teams.client.TeamsClientManager;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.info.GameplayInfo;
import com.flansmod.teams.common.info.PlayerScoreInfo;
import com.flansmod.teams.common.info.TeamScoreInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TwoTeamScoresScreen extends ScoresScreen
{
	public static final ResourceLocation texture = new ResourceLocation(TeamsMod.MODID, "gui/teamsScores.png");

	private static int getGuiHeight() { return 34 + 9 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getMaxPlayerCount(); }

	public final boolean withRanks;

	public TwoTeamScoresScreen(@Nonnull Component title, boolean isRoundOver, boolean ranks)
	{
		super(title, 256, getGuiHeight(), isRoundOver);
		withRanks = ranks;
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float f)
	{
		super.render(graphics, mouseX, mouseY, f);

		TeamsClientManager teams = TeamsModClient.MANAGER;
		GameplayInfo state = teams.currentState;

		int numLines = getNumLines();

		// Header
		graphics.blit(texture, xOrigin, yOrigin, 100, 0, 312, 65, 512, 256);
		// One blit per line of score info
		for(int p = 0; p < numLines; p++)
			graphics.blit(texture, xOrigin, yOrigin + 65 + 16 * p, 100, 65, 312, 16, 512, 256);
		// Footer
		graphics.blit(texture, xOrigin, yOrigin + 65 + (numLines) * 16, 100, 170, 312, 10, 512, 256);

		// Map Name 		Gamemode
		graphics.drawString(font, teams.getCurrentMap().mapName(), xOrigin + 6, yOrigin + 6, 0xffffff);
		String gamemodeString = teams.getCurrentGamemode().gamemodeID();
		graphics.drawCenteredString(font, gamemodeString, xOrigin + 312 - 6 - font.width(gamemodeString), yOrigin + 6, 0xffffff);

		if(isRoundOver)
		{
			if(teams.isGameTied())
			{
				graphics.drawString(font, Component.translatable("teams.scores.tie"), xOrigin + 10, yOrigin + 20, 0xffffff);
			}
			else
			{
				TeamScoreInfo winner = teams.getWinningTeam();
				if(winner != null)
				{
					graphics.drawString(font, Component.translatable("teams.scores.win"), xOrigin + 10, yOrigin + 20, 0xffffff);
				}
			}

			graphics.drawString(font, Component.translatable("teams.time_remaining", getTimeRemaining()), xOrigin + 312 - 22, yOrigin + 20, 0xffffff);
		}
		else
		{
			graphics.drawString(font, Component.translatable("teams.time_remaining", getTimeRemaining()), xOrigin + 10, yOrigin + 20, 0xffffff);
			String scoreLimit = Component.translatable("teams.score_limit", ""+teams.getScoreLimit()).toString();
			graphics.drawString(font, scoreLimit, xOrigin + 302 - font.width(scoreLimit), yOrigin + 20, 0xffffff);
		}

		for(int teamIndex = 0; teamIndex < 2; teamIndex++)
		{
			TeamScoreInfo teamInfo = teams.getTeamScores().get(teamIndex);
			graphics.drawString(font, "\u00a7" + teamInfo.teamTextColour + teamInfo.teamID.getName(), xOrigin + 10 + 151 * teamIndex, yOrigin + 39, 0xffffff);
			graphics.drawString(font, "\u00a7" + teamInfo.teamTextColour + teamInfo.score, xOrigin + 133 + 151 * teamIndex, yOrigin + 39, 0xffffff);
			for(int playerIndex = 0; playerIndex < teamInfo.players.size(); playerIndex++)
			{
				PlayerScoreInfo playerInfo = teamInfo.players.get(playerIndex);
				if(playerInfo == null)
					continue;
				if(withRanks)
					drawRankIcon(graphics, playerInfo.rank, 0, xOrigin + 10 + 151 * teamIndex, yOrigin + 65 + 16 * playerIndex, false);
				graphics.drawString(font, getUsername(playerInfo.playerID), xOrigin + 30 + 151 * teamIndex, yOrigin + 68 + 16 * playerIndex, 0xffffff);
				graphics.drawCenteredString(font, "" + getScore(state, playerInfo), xOrigin + 111 + 151 * teamIndex, yOrigin + 68 + 16 * playerIndex, 0xffffff);
				graphics.drawCenteredString(font, "" + getKills(state, playerInfo), xOrigin + 127 + 151 * teamIndex, yOrigin + 68 + 16 * playerIndex, 0xffffff);
				//graphics.drawCenteredString(font, "" + (teamInfo.showZombieScore ? playerInfo.zombieScore : playerInfo.kills), xOrigin + 127 + 151 * teamIndex, yOrigin + 68 + 16 * playerIndex, 0xffffff);
				graphics.drawCenteredString(font, "" + getDeaths(state, playerInfo), xOrigin + 143 + 151 * teamIndex, yOrigin + 68 + 16 * playerIndex, 0xffffff);
			}
		}
	}


}
