package com.flansmod.teams.client.gui;

import com.flansmod.teams.client.TeamsClientManager;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.info.GameplayInfo;
import com.flansmod.teams.common.info.PlayerScoreInfo;
import com.flansmod.teams.common.info.TeamScoreInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class OneTeamScoresScreen extends ScoresScreen
{
	private static int getGuiHeight() { return 34 + 9 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getMaxPlayerCount(); }

	public OneTeamScoresScreen(@Nonnull Component title, boolean isOver)
	{
		super(title, 128, getGuiHeight(), isOver);
	}


	@Override
	public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float f)
	{
		super.render(graphics, mouseX, mouseY, f);

		TeamsClientManager teams = TeamsModClient.MANAGER;
		GameplayInfo state = teams.currentState;
		int numLines = getNumLines();

		graphics.blit(texture, xOrigin, yOrigin, 0, 45, 256, 24);
		for(int p = 0; p < numLines; p++)
			graphics.blit(texture, xOrigin, yOrigin + 24 + 9 * p, 0, 71, 256, 9);
		graphics.blit(texture, xOrigin, yOrigin - 10, 0, 87, 256, 10);

		graphics.drawCenteredString(font, teams.getCurrentGamemode().gamemodeID(), xOrigin, yOrigin + 4, 0xffffff);
		graphics.drawString(font, "Name", xOrigin + 8, yOrigin + 14, 0xffffff);
		graphics.drawString(font, "Score", xOrigin + 100, yOrigin + 14, 0xffffff);
		graphics.drawString(font, "Kills", xOrigin + 150, yOrigin + 14, 0xffffff);
		graphics.drawString(font, "Deaths", xOrigin + 200, yOrigin + 14, 0xffffff);
		int line = 0;
		TeamScoreInfo teamInfo = teams.getTeamScores().get(0);
		for(int q = 0; q < teamInfo.players.size(); q++)
		{
			PlayerScoreInfo playerData = teamInfo.players.get(q);
			graphics.drawString(font, getUsername(playerData.playerID), xOrigin + 8, yOrigin + 25 + 9 * line, 0xffffff);
			graphics.drawString(font, "" + getScore(state, playerData), xOrigin + 100, yOrigin + 25 + 9 * line, 0xffffff);
			graphics.drawString(font, "" + getKills(state, playerData), xOrigin + 150, yOrigin + 25 + 9 * line, 0xffffff);
			graphics.drawString(font, "" + getDeaths(state, playerData), xOrigin + 200, yOrigin + 25 + 9 * line, 0xffffff);
			line++;
		}
	}
}
