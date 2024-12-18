package com.flansmod.teams.client.gamemode;

import com.flansmod.teams.client.gui.ScoresScreen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public interface IClientGamemode
{
	@Nonnull ScoresScreen createScoresScreen(boolean isRoundOver);
	@Nonnull Component getName();
	@Nonnull Component getDescription();

	@Nonnull Component getSummary(@Nonnull String mapName, @Nonnull List<String> teamNames);
	@Nonnull List<String> getScoreTypes();

	default int getScore(@Nonnull String scoreType, @Nonnull List<Integer> scores)
	{
		int typeIndex = getScoreTypes().indexOf(scoreType);
		if(typeIndex >= 0 && typeIndex < scores.size())
			return scores.get(typeIndex);
		return 0;
	}
}
