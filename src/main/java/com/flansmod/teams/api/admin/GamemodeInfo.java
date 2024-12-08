package com.flansmod.teams.api.admin;

import javax.annotation.Nonnull;

public record GamemodeInfo(@Nonnull String gamemodeID)
{
	public static final GamemodeInfo invalid = new GamemodeInfo("NULL");
}
