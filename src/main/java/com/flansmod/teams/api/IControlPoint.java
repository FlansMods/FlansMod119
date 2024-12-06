package com.flansmod.teams.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IControlPoint
{
	@Nonnull ResourceKey<Level> getDimension();
	@Nonnull BlockPos getPos();
	double getRadius();
	@Nonnull Collection<IControllableBlockRef> getBlocks();
	int getStartingTeamIndex();
	boolean canBeControlledBy(int teamIndex);
}
