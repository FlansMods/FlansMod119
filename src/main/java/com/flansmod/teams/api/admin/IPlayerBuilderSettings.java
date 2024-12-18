package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.OpResult;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPlayerBuilderSettings
{
	@Nullable BlockPos getLastPositionInConstruct(@Nonnull String levelName);
	@Nonnull OpResult setLastPositionInConstruct(@Nonnull String levelName, @Nonnull BlockPos pos);
}
