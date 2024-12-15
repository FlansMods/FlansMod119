package com.flansmod.teams.api.admin;

import com.flansmod.teams.common.TeamsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public record TeamInfo(@Nonnull ResourceLocation teamID)
{
	private static final ResourceLocation invalidLoc = new ResourceLocation(TeamsMod.MODID, "null");
	public static final TeamInfo invalid = new TeamInfo(invalidLoc);

	private static final ResourceLocation spectatorLoc = new ResourceLocation(TeamsMod.MODID, "spectator");
	public static final TeamInfo spectator = new TeamInfo(spectatorLoc);

	@Nonnull
	public Component getName() { return Component.translatable("teams.name."+teamID.getNamespace()+"."+teamID.getPath()); }
}
