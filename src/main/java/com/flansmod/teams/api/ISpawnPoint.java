package com.flansmod.teams.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ISpawnPoint
{
	@Nonnull ResourceKey<Level> getDimension();
	@Nonnull BlockPos getPos();
	@Nullable default AABB getBoundingBoxCheckArea() { return null; }
}
