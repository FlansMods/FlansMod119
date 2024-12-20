package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public interface ICollisionSystem
{
	void preTick();
	void physicsTick();
	long getGameTick();

	@Nonnull default ColliderHandle registerDynamic(@Nonnull List<AABB> localColliders, @Nonnull Transform initialTransform, double mass)  { return registerDynamic(localColliders, initialTransform, mass, new Vec3(mass, mass, mass));}
	@Nonnull ColliderHandle registerDynamic(@Nonnull List<AABB> localColliders, @Nonnull Transform initialTransform, double mass, @Nonnull Vec3 momentOfInertia);
	void unregisterDynamic(@Nonnull ColliderHandle handle);
	void updateColliders(@Nonnull ColliderHandle handle, @Nonnull List<AABB> localColliders);
	boolean isHandleInvalidated(@Nonnull ColliderHandle handle);

	@Nonnull AngularVelocity getAngularVelocity(@Nonnull ColliderHandle handle);
	@Nonnull LinearVelocity getLinearVelocity(@Nonnull ColliderHandle handle);
	void setLinearVelocity(@Nonnull ColliderHandle handle, @Nonnull LinearVelocity linearVelocity);
	void setAngularVelocity(@Nonnull ColliderHandle handle, @Nonnull AngularVelocity angularVelocity);
	void teleport(@Nonnull ColliderHandle handle, @Nonnull Transform to);


	void applyForce(@Nonnull ColliderHandle handle, @Nonnull LinearForce force);
	void applyTorque(@Nonnull ColliderHandle handle, @Nonnull Torque torque);
	//void addLinearAcceleration(@Nonnull ColliderHandle handle, @Nonnull LinearAcceleration linearAcceleration);
	//void addAngularAcceleration(@Nonnull ColliderHandle handle, @Nonnull AngularAcceleration angularAcceleration);

	@Nonnull Transform processEvents(@Nonnull ColliderHandle handle, @Nonnull Consumer<StaticCollisionEvent> staticFunc, @Nonnull Consumer<DynamicCollisionEvent> dynamicFunc);
    void copyDynamicState(@Nonnull ColliderHandle handle, @Nonnull IDynamicObjectUpdateReceiver receiver);

}
