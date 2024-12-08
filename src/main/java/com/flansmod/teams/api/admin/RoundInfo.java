package com.flansmod.teams.api.admin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public record RoundInfo(@Nonnull GamemodeInfo gamemode,
						@Nonnull MapInfo map,
						@Nonnull List<TeamInfo> teams,
						@Nullable ISettings settings)
{
	public static final RoundInfo invalid = new RoundInfo(
		GamemodeInfo.invalid,
		MapInfo.invalid,
		List.of(),
		null);
}
