package com.flansmod.teams.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public record GamemodeInfo(@Nonnull String gamemodeID, @Nonnull IGamemodeFactory factory)
{
	public static final GamemodeInfo invalid = new GamemodeInfo("NULL", IGamemodeFactory.invalid);
}
