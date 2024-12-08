package com.flansmod.teams.server;

import com.flansmod.teams.api.admin.ISpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class SpawnPoint implements ISpawnPoint
{
	public final ResourceKey<Level> dimension;
	public final BlockPos blockPos;

	public SpawnPoint(@Nonnull ResourceKey<Level> dim, @Nonnull BlockPos pos)
	{
		dimension = dim;
		blockPos = pos;
	}

	@Override @Nonnull
	public ResourceKey<Level> getDimension() { return dimension; }
	@Override @Nonnull
	public BlockPos getPos() { return blockPos; }
}
