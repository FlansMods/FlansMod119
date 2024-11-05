package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class DynamicObject implements IConstDynamicObject
{
	public static final int MAX_HISTORY = 20;
	public static final int KILL_VOLUME_NEGATIVE_Y = -256;
	public static final int KILL_VOLUME_POSITIVE_Y = Short.MAX_VALUE;

	public static final Vec3 DEFAULT_MOMENT_OF_INERTIA = new Vec3(1d, 1d, 1d);
	public static final Vec3 DEFAULT_INERTIA_TENSOR = new Vec3(1d, 1d, 1d);

	private record FrameData(@Nonnull Transform Location,
							 @Nonnull LinearVelocity linearVelocity,
							 @Nonnull AngularVelocity angularVelocity)
	{

	}

	@Nonnull
	public final ImmutableList<AABB> Colliders;
	@Nonnull
	public final AABB LocalBounds;

	public final double Mass;
	public final double InverseMass;
	public final Vec3 MomentOfInertia;
	public final Vec3 InertiaTensor;
	public final double LinearDrag;
	public final double AngularDrag;


	@Nonnull
	private final Stack<FrameData> Frames = new Stack<>();
	private FrameData PendingFrame = null;

	// Next Frame Inputs

	// In M/Tick
	@Nonnull
	public LinearVelocity NextFrameLinearMotion;
	@Nonnull
	public AngularVelocity NextFrameAngularMotion;
	@Nonnull
	public List<IAcceleration> Reactions;
	@Nonnull
	public Optional<Transform> NextFrameTeleport;
	@Nonnull
	public final List<DynamicCollisionEvent> DynamicCollisions;
	@Nonnull
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
		Frames.add(new FrameData(Transform.copy(initialLocation), LinearVelocity.Zero, AngularVelocity.Zero));
		NextFrameLinearMotion = LinearVelocity.Zero;
		NextFrameAngularMotion = AngularVelocity.Zero;
		Reactions = List.of();
		NextFrameTeleport = Optional.empty();
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
	public double getLinearDecayPerTick() { return 1.0d - LinearDrag; }
	public double getAngularDecayPerTick() { return 1.0d - AngularDrag; }
	@Override @Nonnull
	public Optional<Transform> getNextFrameTeleport() { return NextFrameTeleport; }
	@Override @Nonnull
	public LinearVelocity getNextFrameLinearVelocity() { return NextFrameLinearMotion; }
	@Override @Nonnull
	public AngularVelocity getNextFrameAngularVelocity() { return NextFrameAngularMotion; }

	@Nonnull
	public AABB getCurrentWorldBounds()
	{
		FrameData frame = getFrameNTicksAgo(0);
		return frame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Nonnull
	public AABB getPendingWorldBounds()
	{
		FrameData frame = PendingFrame;
		return frame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Nonnull
	public AABB getSweepTestAABB()
	{
		FrameData frame = getFrameNTicksAgo(0);
		AABB globalAABB = frame.Location.localToGlobalBounds(getLocalBounds());
		return globalAABB.expandTowards(NextFrameLinearMotion.applyOneTick()).inflate(LocalBounds.getSize());
	}

	@Nonnull
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

	public void setLinearVelocity(@Nonnull LinearVelocity linearVelocity)
	{
		NextFrameLinearMotion = linearVelocity;
	}
	public void addLinearAcceleration(@Nonnull LinearAcceleration linearAcceleration)
	{
		NextFrameLinearMotion = NextFrameLinearMotion.add(linearAcceleration.applyOneTick());
	}
	//public void AddOneTickOfLinearAcceleration(@Nonnull LinearAcceleration linearMotionDelta)
	//{
	//	NextFrameLinearMotion = Maths.Clamp(
	//		NextFrameLinearMotion.add(linearMotionDelta),
	//		-OBBCollisionSystem.MAX_LINEAR_BLOCKS_PER_TICK,
	//		OBBCollisionSystem.MAX_LINEAR_BLOCKS_PER_TICK);
	//}

	public void setAngularVelocity(@Nonnull AngularVelocity angularVelocity)
	{
		NextFrameAngularMotion = angularVelocity;
	}
	public void addAngularAcceleration(@Nonnull AngularAcceleration angularAcceleration)
	{
		NextFrameAngularMotion = NextFrameAngularMotion.compose(angularAcceleration.applyOneTick());
	}
	//public void AddAngularAccelerationPerTick(@Nonnull AxisAngle4f angularMotionDelta)
	//{
	//	Quaternionf nextFrameQ = new Quaternionf().set(NextFrameAngularMotion);
	//	Quaternionf deltaQ = new Quaternionf().set(angularMotionDelta);
	//	nextFrameQ.mul(deltaQ);
	//	// TODO: If AngularMotion.angle > Pi, does quaternion composition not have the chance of going backwards?
	//	nextFrameQ.get(NextFrameAngularMotion);
	//}
	public void teleportTo(@Nonnull Transform location)
	{
		NextFrameTeleport = Optional.of(location);
	}

	@Nonnull
	private FrameData getFrameNTicksAgo(int n)
	{
		return Frames.get(Frames.size() - n - 1);
	}
	@Nonnull
	public TransformedBBCollection getCurrentColliders()
	{
		return new TransformedBBCollection(getFrameNTicksAgo(0).Location, Colliders);
	}
	@Nonnull
	public TransformedBB getCurrentBB()
	{
		return TransformedBB.Of(getFrameNTicksAgo(0).Location, LocalBounds);
	}
	@Nonnull
	public Transform getCurrentLocation()
	{
		return getFrameNTicksAgo(0).Location;
	}
	@Nonnull
	public TransformedBBCollection getPendingColliders()
	{
		if(PendingFrame != null)
			return new TransformedBBCollection(PendingFrame.Location, Colliders);
		return getCurrentColliders();
	}
	@Nonnull
	public TransformedBB getPendingBB()
	{
		if(PendingFrame != null)
			return TransformedBB.Of(PendingFrame.Location, LocalBounds);
		return getCurrentBB();
	}
	@Nonnull
	public Transform getPendingLocation()
	{
		if(PendingFrame != null)
			return PendingFrame.Location;
		return getCurrentLocation();
	}
	public void preTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		extrapolateNextFrame();
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

	// An alternative to commitFrame that basically undoes any changes to this dynamic
	public void resetVelocity()
	{
		NextFrameLinearMotion = getFrameNTicksAgo(0).linearVelocity;
		NextFrameAngularMotion = getFrameNTicksAgo(0).angularVelocity;
	}
	public void commitFrame()
	{
		if (Frames.size() >= MAX_HISTORY)
			Frames.remove(0);

		// If we are teleporting, double push so the "last" frame is the same
		if(NextFrameTeleport.isPresent())
		{
			Frames.push(PendingFrame);
			Frames.push(PendingFrame);
		}
		else //if(!VehicleEntity.PAUSE_PHYSICS)
		{
			Frames.push(PendingFrame);
		}

		NextFrameLinearMotion = PendingFrame.linearVelocity;
		NextFrameAngularMotion = PendingFrame.angularVelocity;
		NextFrameTeleport = Optional.empty();
	}
	public void extrapolateNextFrame(boolean withReactionForce)
	{
		if(NextFrameTeleport.isPresent())
		{
			PendingFrame = new FrameData(NextFrameTeleport.get(), NextFrameLinearMotion, NextFrameAngularMotion);
		}
		else
		{
			if(withReactionForce)
			{
				LinearVelocity reactionaryLinearV = NextFrameLinearMotion.scale(getLinearDecayPerTick());
				AngularVelocity reactionaryAngularV = NextFrameAngularMotion.scale(getAngularDecayPerTick());
				for(IAcceleration acceleration : Reactions)
				{
					reactionaryLinearV = reactionaryLinearV.add(acceleration.getLinearComponent(getPendingLocation()).applyOneTick());
					reactionaryAngularV = reactionaryAngularV.compose(acceleration.getAngularComponent(getPendingLocation()).applyOneTick());
				}
				extrapolateNextFrame(reactionaryLinearV, reactionaryAngularV);
			}
			else
			{
				extrapolateNextFrame(
						NextFrameLinearMotion.scale(getLinearDecayPerTick()),
						NextFrameAngularMotion.scale(getAngularDecayPerTick()));
			}
		}
	}
	public void extrapolateNextFrame(@Nonnull CompoundVelocity motion)
	{
		extrapolateNextFrame(motion.linear(), motion.angular());
	}
	public void extrapolateNextFrame(@Nonnull LinearVelocity linearMotion, @Nonnull AngularVelocity angularMotion)
	{
		Vec3 deltaPos = linearMotion.applyOneTick();
		Quaternionf deltaRot = angularMotion.applyOneTick();

		FrameData currentFrame = getFrameNTicksAgo(0);
		Transform newLoc = Transform.fromPosAndQuat(
				currentFrame.Location.positionVec3().add(deltaPos),
				currentFrame.Location.Orientation.mul(deltaRot, new Quaternionf()));
		extrapolateNextFrame(newLoc, linearMotion, angularMotion);
	}
	public void extrapolateNextFrame(@Nonnull Transform location, @Nonnull CompoundVelocity motion)
	{
		PendingFrame = new FrameData(location, motion.linear(), motion.angular());
	}
	public void extrapolateNextFrame(@Nonnull Transform location, @Nonnull LinearVelocity linearMotion, @Nonnull AngularVelocity angularMotion)
	{
		PendingFrame = new FrameData(location, linearMotion, angularMotion);
	}
	public void extrapolateNextFrame() { extrapolateNextFrame(false); }
	public void extrapolateNextFrameWithReaction() { extrapolateNextFrame(true); }
}