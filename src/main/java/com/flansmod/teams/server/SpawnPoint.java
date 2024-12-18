package com.flansmod.teams.server;

import com.flansmod.teams.api.admin.ISpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class SpawnPoint implements ISpawnPoint
{
	public final BlockPos blockPos;

	public SpawnPoint(@Nonnull BlockPos pos)
	{
		blockPos = pos;
	}

	@Override @Nonnull
	public BlockPos getPos() { return blockPos; }
}
