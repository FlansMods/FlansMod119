package com.flansmod.teams.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IControllableBlockRef
{
	@Nonnull BlockPos getPos();
	default boolean isBlockEntity() { return getBlockEntityType() != null; }
	@Nullable BlockEntityType<?> getBlockEntityType();
}
