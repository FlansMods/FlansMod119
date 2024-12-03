package com.flansmod.teams.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IMap
{
	@Nonnull String getName();
	@Nonnull ResourceKey<Level> getPrimaryDimension(@Nonnull String mapName);
	@Nonnull List<ChunkPos> getChunkLoadTickets();

	boolean hasSettingsOverride();
	@Nullable ISettings getSettingsOverride();
}
