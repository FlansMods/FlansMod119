package com.flansmod.teams.api.admin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public record TeamInfo(@Nonnull ResourceLocation teamID)
{
	private static final ResourceLocation invalidLoc = new ResourceLocation("flansmod", "null");
	public static final TeamInfo invalid = new TeamInfo(invalidLoc);

	@Nonnull
	public Component getName() { return Component.translatable("teams.name."+teamID.getNamespace()+"."+teamID.getPath()); }
}
