package com.flansmod.physics.common.collision.obb;

import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class DynamicObject implements
	IConstDynamicObject,
	ICollisionAccessDynamicObject,
	IEditAccessDynamicObject
{
	public static final int KILL_VOLUME_NEGATIVE_Y = -256;
	public static final int KILL_VOLUME_POSITIVE_Y = Short.MAX_VALUE;

	public static final Vec3 DEFAULT_MOMENT_OF_INERTIA = new Vec3(1d, 1d, 1d);
	public static final Vec3 DEFAULT_INERTIA_TENSOR = new Vec3(1d, 1d, 1d);

	private record FrameData(@Nonnull Transform Location,
							 @Nonnull LinearVelocity linearVelocity,
							 @Nonnull AngularVelocity angularVelocity)
	{

	}

	public final ImmutableList<AABB> Colliders;
	public final AABB LocalBounds;

	public final double Mass;
	public final double InverseMass;
	public final Vec3 MomentOfInertia;
	public final Vec3 InertiaTensor;
	public final double LinearDrag;
	public final double AngularDrag;

	private FrameData CurrentFrame;
	private FrameData PendingFrame;
	private boolean teleportedPendingFrame = false;

	public final List<DynamicCollisionEvent> DynamicCollisions;
	public final List<StaticCollisionEvent> StaticCollisions;

	public static class Builder
	{
		private ImmutableList.Builder<AABB> localColliders = new ImmutableList.Builder<>();
		private double mass = 1.0d;
		private double invMass = 1.0d;
		private Vec3 momentOfInertia = null;
		private Vec3 inertiaTensor = null;
		private double linearDrag = 0.0d;
		private double angularDrag = 0.0d;
		private Transform initialLocation = null;

		public Builder(){}

		@Nonnull
		public Builder withMass(double kg)
		{
			if(Maths.approx(kg, 0d))
				return massless();
			mass = kg;
			invMass = 1d / kg;
			return this;
		}
		@Nonnull
		public Builder withInverseMass(double oneOverKg)
		{
			if(Maths.approx(oneOverKg, 0d))
				return immovable();
			mass = 1d / oneOverKg;
			invMass = oneOverKg;
			return this;
		}
		@Nonnull
		public Builder massless() {
			mass = 0d;
			invMass = Double.MAX_VALUE;
			return this;
		}
		@Nonnull
		public Builder immovable() {
			mass = Double.MAX_VALUE;
			invMass = 0d;
			return this;
		}
		@Nonnull
		public Builder withDrag(double decayPerTick)
		{
			linearDrag = decayPerTick;
			angularDrag = decayPerTick;
			return this;
		}
		@Nonnull
		public Builder withLinearDrag(double decayPerTick)
		{
			linearDrag = decayPerTick;
			return this;
		}
		@Nonnull
		public Builder withAngularDrag(double decayPerTick)
		{
			angularDrag = decayPerTick;
			return this;
		}
		@Nonnull
		public Builder withMomentOfInertia(@Nonnull Vec3 moment)
		{
			if(Maths.approx(moment, Vec3.ZERO))
				return zeroInertia();
			momentOfInertia = moment;
			inertiaTensor = new Vec3(1d / moment.x, 1d / moment.y, 1d / moment.z);
			return this;
		}
		@Nonnull
		public Builder withInertiaTensor(@Nonnull Vec3 tensor)
		{
			if(Maths.approx(tensor, Vec3.ZERO))
				return infiniteInertia();
			momentOfInertia = new Vec3(1d / tensor.x, 1d / tensor.y, 1d / tensor.z);
			inertiaTensor = tensor;
			return this;
		}
		@Nonnull
		public Builder infiniteInertia()
		{
			momentOfInertia = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			inertiaTensor = Vec3.ZERO;
			return this;
		}
		@Nonnull
		public Builder zeroInertia()
		{
			momentOfInertia = Vec3.ZERO;
			inertiaTensor = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			return this;
		}
		@Nonnull
		public Builder inLocation(@Nonnull Transform loc)
		{
			initialLocation = loc;
			return this;
		}
		@Nonnull
		public Builder withCollider(@Nonnull AABB collider)
		{
			localColliders.add(collider);
			return this;
		}
		@Nonnull
		public Builder withColliders(@Nonnull Iterable<AABB> colliders)
		{
			localColliders.addAll(colliders);
			return this;
		}

		@Nonnull
		public DynamicObject build()
		{
			return new DynamicObject(localColliders.build(),
					initialLocation != null ? initialLocation : Transform.IDENTITY,
					mass,
					invMass,
					linearDrag,
					angularDrag,
					momentOfInertia != null ? momentOfInertia : DEFAULT_MOMENT_OF_INERTIA,
					inertiaTensor != null ? inertiaTensor : DEFAULT_INERTIA_TENSOR);
		}
	}


	private DynamicObject(@Nonnull List<AABB> localColliders,
						 @Nonnull Transform initialLocation,
						 double mass,
						 double invMass,
						 double linearDrag,
						 double angularDrag,
						 @Nonnull Vec3 momentOfInertia,
						 @Nonnull Vec3 inertiaTensor)
	{
		ImmutableList.Builder<AABB> builder = ImmutableList.builder();
		Colliders = builder.addAll(localColliders).build();
		LocalBounds = getLocalBounds();
		CurrentFrame = new FrameData(Transform.copy(initialLocation), LinearVelocity.Zero, AngularVelocity.Zero);
		PendingFrame = null;
		DynamicCollisions = new ArrayList<>();
		StaticCollisions = new ArrayList<>();
		Mass = mass;
		InverseMass = invMass;
		MomentOfInertia = momentOfInertia;
		InertiaTensor = inertiaTensor;
		LinearDrag = linearDrag;
		AngularDrag = angularDrag;
	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	public void preTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		extrapolatePendingFrame();
	}
	public boolean isInvalid()
	{
		if(PendingFrame != null)
		{
			if(PendingFrame.Location.hasNaN())
				return true;
			Vec3 pos = PendingFrame.Location.positionVec3();
			if(pos.y < KILL_VOLUME_NEGATIVE_Y || pos.y > KILL_VOLUME_POSITIVE_Y)
			{
				return true;
			}
		}
		return false;
	}



	// -----------------------------------------------------------------------------------------------------------------
	// IConstDynamicObject
	//  - This should be a fairly safe interface to give out at any time on any thread
	@Override
	public double getMass() { return Mass; }
	@Override
	public double getInverseMass() { return InverseMass; }
	@Override @Nonnull
	public Vec3 getMomentOfInertia() { return MomentOfInertia; }
	@Override @Nonnull
	public Vec3 getInertiaTensor() { return InertiaTensor; }
	@Override
	public double getLinearDrag() { return LinearDrag; }
	@Override
	public double getAngularDrag() { return AngularDrag; }
	@Override @Nonnull
	public AABB getCurrentWorldBounds()
	{
		return CurrentFrame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Override @Nonnull
	public AABB getPendingWorldBounds()
	{
		FrameData frame = PendingFrame;
		return frame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Override @Nonnull
	public AABB getSweepTestAABB()
	{
		AABB globalAABB = CurrentFrame.Location.localToGlobalBounds(getLocalBounds());
		return globalAABB.expandTowards(CurrentFrame.linearVelocity.applyOneTick()).inflate(LocalBounds.getSize());
	}
	@Override @Nonnull
	public AABB getLocalBounds()
	{
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for(AABB aabb : Colliders)
		{
			xMax = Maths.max(aabb.maxX, xMax);
			yMax = Maths.max(aabb.maxY, yMax);
			zMax = Maths.max(aabb.maxZ, zMax);
			xMin = Maths.min(aabb.minX, xMin);
			yMin = Maths.min(aabb.minY, yMin);
			zMin = Maths.min(aabb.minZ, zMin);
		}
		return new AABB(xMin, yMin, zMin, xMax, yMax, zMax);
	}
	@Override @Nonnull
	public TransformedBBCollection getCurrentColliders()
	{
		return new TransformedBBCollection(CurrentFrame.Location, Colliders);
	}
	@Override @Nonnull
	public TransformedBB getCurrentBB()
	{
		return TransformedBB.Of(CurrentFrame.Location, LocalBounds);
	}
	@Override @Nonnull
	public Transform getCurrentLocation()
	{
		return CurrentFrame.Location;
	}
	@Override @Nonnull
	public LinearVelocity getLinearVelocity() { return CurrentFrame.linearVelocity; }
	@Override @Nonnull
	public AngularVelocity getAngularVelocity() { return CurrentFrame.angularVelocity; }
	// -----------------------------------------------------------------------------------------------------------------


	// -----------------------------------------------------------------------------------------------------------------
	// IEditAccessDynamicObject
	//  - This interface should only be offered to one thread at a time
	@Override
	public void setLinearVelocity(@Nonnull LinearVelocity linearVelocity)
	{
		CurrentFrame = new FrameData(CurrentFrame.Location, linearVelocity, CurrentFrame.angularVelocity);
	}
	@Override
	public void addLinearAcceleration(@Nonnull LinearAcceleration linearAcceleration)
	{
		CurrentFrame = new FrameData(CurrentFrame.Location, CurrentFrame.linearVelocity.add(linearAcceleration.applyOneTick()), CurrentFrame.angularVelocity);
	}
	@Override
	public void setAngularVelocity(@Nonnull AngularVelocity angularVelocity)
	{
		CurrentFrame = new FrameData(CurrentFrame.Location, CurrentFrame.linearVelocity, angularVelocity);
	}
	@Override
	public void addAngularAcceleration(@Nonnull AngularAcceleration angularAcceleration)
	{
		CurrentFrame = new FrameData(CurrentFrame.Location, CurrentFrame.linearVelocity, CurrentFrame.angularVelocity.compose(angularAcceleration.applyOneTick()));
	}
	@Override
	public void teleportTo(@Nonnull Transform location)
	{
		CurrentFrame = new FrameData(location, CurrentFrame.linearVelocity, CurrentFrame.angularVelocity);
		teleportedPendingFrame = true;
	}
	// -----------------------------------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------------------------------
	// ICollisionAccessDynamicObject
	//  - This interface is intended only for physics thread and collision resolving
	@Override
	public boolean isPendingFrameEvaluated() { return PendingFrame != null; }
	@Override @Nonnull
	public TransformedBBCollection getPendingColliders()
	{
		if(PendingFrame != null)
			return new TransformedBBCollection(PendingFrame.Location, Colliders);
		return getCurrentColliders();
	}
	@Override @Nonnull
	public TransformedBB getPendingBB()
	{
		if(PendingFrame != null)
			return TransformedBB.Of(PendingFrame.Location, LocalBounds);
		return getCurrentBB();
	}
	@Override @Nonnull
	public Transform getPendingLocation()
	{
		if(PendingFrame != null)
			return PendingFrame.Location;
		return getCurrentLocation();
	}
	@Override
	public void extrapolatePendingFrame(double parametricTicks)
	{
		LinearVelocity linearV = CurrentFrame.linearVelocity.scale(getLinearDecayPerTick());
		AngularVelocity angularV = CurrentFrame.angularVelocity.scale(getAngularDecayPerTick());
		Vec3 deltaPos = linearV.applyOverTicks(parametricTicks);
		Quaternionf deltaRot = angularV.applyOverTicks(parametricTicks);
		Transform newLoc = Transform.fromPosAndQuat(
			CurrentFrame.Location.positionVec3().add(deltaPos),
			CurrentFrame.Location.Orientation.mul(deltaRot, new Quaternionf()));
		PendingFrame = new FrameData(newLoc, linearV, angularV);
	}
	@Override
	public void setPendingLocation(@Nonnull Transform location)
	{
		if(PendingFrame == null)
			extrapolatePendingFrame();

		PendingFrame = new FrameData(location, PendingFrame.linearVelocity, PendingFrame.angularVelocity);
	}
	@Override
	public void setPendingLinearVelocity(@Nonnull LinearVelocity linearVelocity)
	{
		if(PendingFrame == null)
			extrapolatePendingFrame();

		PendingFrame = new FrameData(PendingFrame.Location, linearVelocity, PendingFrame.angularVelocity);
	}
	@Override
	public void setPendingAngularVelocity(@Nonnull AngularVelocity angularVelocity)
	{
		if(PendingFrame == null)
			extrapolatePendingFrame();

		PendingFrame = new FrameData(PendingFrame.Location, PendingFrame.linearVelocity, angularVelocity);
	}
	@Override
	public void commitPendingFrame()
	{
 		CurrentFrame = PendingFrame;
		PendingFrame = null;
		teleportedPendingFrame = false;
	}
	@Override
	public void discardPendingFrame()
	{
		PendingFrame = null;
		teleportedPendingFrame = false;
	}
	@Override
	public boolean pendingFrameIsTeleport() { return teleportedPendingFrame; }
	// -----------------------------------------------------------------------------------------------------------------
}