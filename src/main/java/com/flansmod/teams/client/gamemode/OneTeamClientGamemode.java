package com.flansmod.teams.client.gamemode;

import com.flansmod.teams.client.gui.OneTeamScoresScreen;
import com.flansmod.teams.client.gui.ScoresScreen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public class OneTeamClientGamemode implements IClientGamemode
{
	private final Component name;
	private final Component description;

	public OneTeamClientGamemode(@Nonnull Component gamemodeName, @Nonnull Component gamemodeDesc)
	{
		name = gamemodeName;
		description = gamemodeDesc;
	}


	@Override @Nonnull
	public Component getSummary(@Nonnull String mapName, @Nonnull List<String> teamNames)
	{
		return Component.translatable("teams.one_team_gamemode", mapName, teamNames.get(0));
	}
	@Override @Nonnull
	public ScoresScreen createScoresScreen(boolean isRoundOver)
	{
		return new OneTeamScoresScreen(name, isRoundOver);
	}
	@Override @Nonnull
	public Component getName() { return name; }
	@Override @Nonnull
	public Component getDescription() { return description; }
}
