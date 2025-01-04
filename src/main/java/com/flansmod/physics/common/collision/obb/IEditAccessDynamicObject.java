package com.flansmod.physics.common.collision.obb;

import com.flansmod.physics.common.units.AngularAcceleration;
import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;

import javax.annotation.Nonnull;

public interface IEditAccessDynamicObject extends IConstDynamicObject
{
	void setLinearVelocity(@Nonnull LinearVelocity linearVelocity);
	void addLinearAcceleration(@Nonnull LinearAcceleration linearAcceleration);
	void setAngularVelocity(@Nonnull AngularVelocity angularVelocity);
	void addAngularAcceleration(@Nonnull AngularAcceleration angularAcceleration);
	void teleportTo(@Nonnull Transform location);
	void setPendingLocation(@Nonnull Transform location);
}
