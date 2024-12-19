package com.flansmod.teams.common.block;

import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.common.TeamsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BlockEntitySpawnPoint extends BlockEntity implements ISpawnPoint
{
	public BlockEntitySpawnPoint(@Nonnull BlockPos pos,
								 @Nonnull BlockState state)
	{
		super(TeamsMod.SPAWN_POINT_BLOCK_ENTITY_TYPE.get(), pos, state);
	}

	@Override @Nonnull
	public BlockPos getPos() { return getBlockPos(); }


}
