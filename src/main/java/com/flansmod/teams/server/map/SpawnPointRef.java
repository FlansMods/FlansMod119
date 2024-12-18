package com.flansmod.teams.server.map;

import com.flansmod.teams.api.admin.ISpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public record SpawnPointRef(@Nonnull BlockPos pos) implements ISpawnPoint
{
	@Nonnull
	public static SpawnPointRef of(@Nonnull CompoundTag tag)
	{
		return new SpawnPointRef(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
	}

	@Override @Nonnull
	public BlockPos getPos() { return pos; }
}
