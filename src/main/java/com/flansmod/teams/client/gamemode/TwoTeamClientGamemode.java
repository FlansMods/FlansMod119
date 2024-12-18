package com.flansmod.teams.client.gamemode;

import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.client.gui.ScoresScreen;
import com.flansmod.teams.client.gui.TwoTeamScoresScreen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class TwoTeamClientGamemode implements IClientGamemode
{
	private final Component name;
	private final Component description;

	public TwoTeamClientGamemode(@Nonnull Component gamemodeName, @Nonnull Component gamemodeDesc)
	{
		name = gamemodeName;
		description = gamemodeDesc;
	}

	@Override @Nonnull
	public Component getSummary(@Nonnull String mapName, @Nonnull List<String> teamNames)
	{
		return Component.translatable("teams.two_team_gamemode", mapName, teamNames.get(0), teamNames.get(1));
	}
	@Override @Nonnull
	public ScoresScreen createScoresScreen(boolean isRoundOver)
	{
		return new TwoTeamScoresScreen(name, isRoundOver, false);
	}
	@Override @Nonnull
	public Component getName() { return name; }
	@Override @Nonnull
	public Component getDescription() { return description; }

	@Override @Nonnull
	public List<String> getScoreTypes()
	{
		return List.of(TeamsAPI.SCORE_TYPE_KILLS, TeamsAPI.SCORE_TYPE_ASSISTS, TeamsAPI.SCORE_TYPE_DEATHS);
	}
}
