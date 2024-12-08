package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.admin.ISpawnPoint;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IControllableEntityInstance
{
	@Nonnull Entity getAsEntity();
	@Nonnull IControlPointRef getOwner();
	boolean isSpawnPoint();
	@Nullable ISpawnPoint getSpawnPoint();

	void onRoundStart();
	void onRoundEnd();
}
