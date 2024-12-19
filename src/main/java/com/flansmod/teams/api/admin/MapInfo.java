package com.flansmod.teams.api.admin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record MapInfo(@Nonnull String mapName,
					  @Nullable ISettings settings)
{
	public static final MapInfo invalid = new MapInfo("invalid", null);

	@Override
	public String toString() { return mapName; }
}
