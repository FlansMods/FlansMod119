package com.flansmod.teams.common.entity;

import com.flansmod.teams.api.runtime.IControlPointInstance;
import com.flansmod.teams.api.admin.IControllableBlockRef;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityFlagpole extends Entity implements IControlPointInstance
{
	private static final EntityDataAccessor<Integer> DATA_TEAM_INDEX = SynchedEntityData.defineId(EntityFlagpole.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_CAPTURE_PROGRESS = SynchedEntityData.defineId(EntityFlagpole.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_CONTESTED = SynchedEntityData.defineId(EntityFlagpole.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_FLAG_PRESENT = SynchedEntityData.defineId(EntityFlagpole.class, EntityDataSerializers.BOOLEAN);

	public double radius = 6d;
	public int initialTeamIndex = 0;
	public boolean canBeCaptured = false;
	public List<IControllableBlockRef> controlledBlocks = new ArrayList<>();

	@Override public int getCurrentTeamIndex() { return entityData.get(DATA_TEAM_INDEX); }
	@Override public void setCurrentTeamIndex(int index) { entityData.set(DATA_TEAM_INDEX, index); }
	@Override public int getCaptureProgress() { return entityData.get(DATA_CAPTURE_PROGRESS); }
	@Override public void setCaptureProgress(int ticks) { entityData.set(DATA_CAPTURE_PROGRESS, ticks); }
	@Override public boolean getContested() { return entityData.get(DATA_CONTESTED); }
	@Override public void setContested(boolean set) { entityData.set(DATA_CONTESTED, set); }
	@Override public boolean getFlagPresent() { return entityData.get(DATA_FLAG_PRESENT); }
	@Override public void setFlagPresent(boolean set) { entityData.set(DATA_FLAG_PRESENT, set); }

	public EntityFlagpole(@Nonnull EntityType<?> entityType, @Nonnull Level level)
	{
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData()
	{
		entityData.define(DATA_TEAM_INDEX, 0);
		entityData.define(DATA_CAPTURE_PROGRESS, 0);
		entityData.define(DATA_CONTESTED, false);
		entityData.define(DATA_FLAG_PRESENT, true);
	}


	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		radius = tags.getDouble("radius");
		initialTeamIndex = tags.getInt("initialTeam");
		canBeCaptured = tags.getBoolean("canBeCaptured");
	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		tags.putDouble("radius", radius);
		tags.putInt("initialTeam", initialTeamIndex);
		tags.putBoolean("canBeCaptured", canBeCaptured);
	}

	@Override
	public void onRoundStart()
	{
		setCurrentTeamIndex(initialTeamIndex);
		setCaptureProgress(0);
		setFlagPresent(true);
		setContested(false);
	}
	@Override
	public void onRoundEnd()
	{
	}
	@Override @Nonnull
	public BlockPos getPos() { return blockPosition(); }
	@Override @Nonnull
	public Collection<IControllableBlockRef> getBlocks() { return controlledBlocks; }
	@Override
	public double getRadius() { return radius; }
	@Override
	public int getStartingTeamIndex() { return initialTeamIndex; }
	@Override
	public boolean canBeControlledBy(int teamIndex)
	{
		return canBeCaptured || teamIndex == initialTeamIndex;
	}
}
