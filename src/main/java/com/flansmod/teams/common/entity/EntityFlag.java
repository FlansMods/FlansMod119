package com.flansmod.teams.common.entity;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.runtime.IControllableEntityInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFlag extends Entity implements IControllableEntityInstance
{
	private static final EntityDataAccessor<Integer> DATA_TEAM_INDEX = SynchedEntityData.defineId(EntityFlag.class, EntityDataSerializers.INT);
	public IControlPointRef owner;

	public EntityFlag(@Nonnull EntityType<?> entityType, @Nonnull Level level)
	{
		super(entityType, level);
	}

	public int getTeamIndex() { return entityData.get(DATA_TEAM_INDEX); }
	public void setTeamIndex(int set) { entityData.set(DATA_TEAM_INDEX, set); }

	@Override @Nonnull
	public Entity getAsEntity() { return this; }
	@Override @Nonnull
	public IControlPointRef getOwner() { return owner; }
	@Override
	public boolean isSpawnPoint() { return false; }
	@Override @Nullable
	public ISpawnPoint getSpawnPoint() { return null; }

	@Override
	public void onRoundStart() {}
	@Override
	public void onRoundEnd() {}

	@Override
	protected void defineSynchedData()
	{
		entityData.define(DATA_TEAM_INDEX, 0);
	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}
}
