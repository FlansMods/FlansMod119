package com.flansmod.teams.common.block;

import com.flansmod.teams.common.TeamsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSpawnPoint extends BaseEntityBlock
{
	public BlockSpawnPoint(@Nonnull Properties props)
	{
		super(props);
	}


	@Override @Nullable
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return new BlockEntitySpawnPoint(pos, state);
	}
	@Override @Nullable
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		if(context.getPlayer() != null)
		{
			// These can only be placed inside the construct
			if(!TeamsMod.MANAGER.getConstructs().managesDimension(context.getPlayer().level().dimension()))
			{
				return null;
			}
		}
		return this.defaultBlockState();
	}
	@Override
	public void onPlace(@Nonnull BlockState oldState, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean flag)
	{
		super.onPlace(oldState, level, pos, newState, flag);
	}
	@Override
	public void onRemove(@Nonnull BlockState oldState, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean flag)
	{
		//TeamsMod.MANAGER.getConstructs().controlPointRemoved();
		super.onRemove(oldState, level, pos, newState, flag);
	}
}
