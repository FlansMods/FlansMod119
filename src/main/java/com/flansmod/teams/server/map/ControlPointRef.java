package com.flansmod.teams.server.map;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.admin.IControllableBlockRef;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;

public record ControlPointRef( @Nonnull BlockPos pos,
							  double radius,
							  int startingTeamIndex) implements IControlPointRef
{
	public static ControlPointRef of(@Nonnull CompoundTag tags)
	{
		BlockPos pos = new BlockPos(tags.getInt("x"), tags.getInt("y"), tags.getInt("z"));
		double radius = tags.getDouble("radius");
		int startingTeamIndex = tags.getInt("team");
		return new ControlPointRef(pos, radius, startingTeamIndex);
	}

	@Override @Nonnull
	public BlockPos getPos() { return pos; }
	@Override
	public double getRadius() { return radius; }
	@Override @Nonnull
	public Collection<IControllableBlockRef> getBlocks()
	{
		return null;
	}
	@Override
	public int getStartingTeamIndex() { return startingTeamIndex; }
	@Override
	public boolean canBeControlledBy(int teamIndex)
	{
		return false;
	}
}
