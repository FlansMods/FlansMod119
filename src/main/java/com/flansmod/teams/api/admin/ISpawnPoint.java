package com.flansmod.teams.api.admin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ISpawnPoint
{
	ISpawnPoint zero = new ISpawnPoint()
	{
		@Override @Nonnull
		public BlockPos getPos() { return BlockPos.ZERO; }
	};

	@Nonnull BlockPos getPos();
	@Nullable default AABB getBoundingBoxCheckArea() { return null; }
}
