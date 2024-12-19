package com.flansmod.teams.server.dimension;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.IPlayerBuilderSettings;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PlayerBuilderSettings implements IPlayerBuilderSettings
{
	private final Map<String, BlockPos> constructPositions = new HashMap<>();

	@Override @Nullable
	public BlockPos getLastPositionInConstruct(@Nonnull String levelName)
	{
		return constructPositions.get(levelName);
	}
	@Override @Nonnull
	public OpResult setLastPositionInConstruct(@Nonnull String levelName, @Nonnull BlockPos pos)
	{
		constructPositions.put(levelName, pos);
		return OpResult.SUCCESS;
	}
}
