package com.flansmod.common.actions.contexts;

import com.flansmod.common.actions.stats.IModifierBaker;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShooterContextVehicleRoot extends ShooterContextVehicle
{
	public ShooterContextVehicleRoot(@Nonnull VehicleEntity vehicle)
	{
		super(vehicle);
	}

	@Override
	public int GetNumValidContexts()
	{
		return 0;
	}
	@Override
	@Nullable
	public Entity Owner()
	{
		if(Vehicle.hasControllingPassenger())
			return Vehicle.getControllingPassenger();
		return null;
	}
	@Nonnull
	@Override
	public Transform GetShootOrigin(float deltaTick)
	{
		return Transform.fromPos(Vehicle.position());
	}

	@Override
	public void BakeModifiers(@Nonnull IModifierBaker baker)
	{

	}
}
