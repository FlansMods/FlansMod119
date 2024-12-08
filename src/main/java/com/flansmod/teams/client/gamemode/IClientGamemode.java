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
}
