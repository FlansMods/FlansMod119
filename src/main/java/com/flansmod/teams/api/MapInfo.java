package com.flansmod.teams.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record MapInfo(@Nonnull String mapName,
					  @Nullable ISettings settings)
{
	public static final MapInfo invalid = new MapInfo("invalid", null);
}
