package com.flansmod.teams.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public record TeamInfo(@Nonnull ResourceLocation teamID)
{
	private static final ResourceLocation invalidLoc = new ResourceLocation("flansmod", "null");
	public static final TeamInfo invalid = new TeamInfo(invalidLoc);
}
