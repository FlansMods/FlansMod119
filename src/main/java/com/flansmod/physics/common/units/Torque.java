package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.*;

import javax.annotation.Nonnull;

public record Torque(@Nonnull Vec3 Axis, double Magnitude) implements IForce
{
	public static final Torque Zero = new Torque(new Vec3(0d, 1d, 0d), 0.0d);

	@Nonnull
	public static Torque fromKgBlocksSqQuatPerTickSq(@Nonnull Quaternionf quaternionPerTick)
	{
		if(quaternionPerTick.equals(Transform.IDENTITY.Orientation, Maths.EpsilonF))
		{
			return Torque.Zero;
		}
		AxisAngle4f axisAngle = new AxisAngle4f().set(quaternionPerTick);
		return new Torque(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle);
	}
	@Nonnull
	public static Torque fromKgBlocksSqQuatPerSecondSq(@Nonnull Quaternionf quaternionPerSecond)
	{
		return fromKgBlocksSqQuatPerTickSq(quaternionPerSecond).scale(Units.Torque.KgBlocksSqPerSecondSq_To_KgBlocksSqPerTickSq);
	}

	@Nonnull
	public static Torque kgBlocksSqPerSecondSq(@Nonnull Vec3 axis, double kgBlocksSqPerSecondSq)
	{
		return new Torque(axis, Units.Force.KgBlocksPerSecondSq_To_KgBlocksPerTickSq(kgBlocksSqPerSecondSq));
	}
	@Nonnull
	public static Torque kgBlocksSqPerTickSq(@Nonnull Vec3 axis, double kgBlocksSqPerTickSq)
	{
		return new Torque(axis, kgBlocksSqPerTickSq);
	}

	@Nonnull
	public Vec3 asVec3() { return Axis.scale(Magnitude); }
	@Nonnull
	public Torque scale(double scale)
	{
		return new Torque(Axis, Magnitude * scale);
	}
	@Nonnull
	public Torque compose(@Nonnull Torque other)
	{
		Quaternionf torqueA = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		Quaternionf torqueB = new Quaternionf().setAngleAxis(other.Magnitude, other.Axis.x, other.Axis.y, other.Axis.z);
		Quaternionf composed = torqueA.mul(torqueB);
		return fromKgBlocksSqQuatPerTickSq(composed);
	}

	@Nonnull
	public Units.Torque getDefaultUnits() { return Units.Torque.KgBlocksSqPerTickSq; }
	public Torque convertToUnits(@Nonnull Units.Torque toUnits) { return new Torque(Axis, Units.Torque.Convert(Magnitude, Units.Torque.KgBlocksSqPerTickSq, toUnits)); }
	@Nonnull
	public AngularAcceleration sumTorqueActingOnMass(double mass) { return new AngularAcceleration(Axis, Magnitude / mass); }
	@Nonnull
	public AngularAcceleration sumTorqueActingOnInverseMass(double invMass) { return new AngularAcceleration(Axis, Magnitude * invMass); }
	@Nonnull
	public AngularAcceleration actingOnMomentOfInertia(@Nonnull Vec3 momentOfInertia)
	{
		AxisAngle4d axisAngle = new AxisAngle4d();
		Quaterniond quat = new Quaterniond();
		Vector3d euler = new Vector3d();

		quat.setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		quat.getEulerAnglesXYZ(euler);
		euler.div(momentOfInertia.x, momentOfInertia.y, momentOfInertia.z);
		quat.identity();
		quat.rotateXYZ(euler.x, euler.y, euler.z);
		axisAngle.set(quat);

	 	return new AngularAcceleration(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle);
	}
	@Nonnull
	public AngularAcceleration actingOnInertiaTensor(@Nonnull Vec3 inertiaTensor)
	{
		AxisAngle4d axisAngle = new AxisAngle4d();
		Quaterniond quat = new Quaterniond();
		Vector3d euler = new Vector3d();

		quat.setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		quat.getEulerAnglesXYZ(euler);
		euler.mul(inertiaTensor.x, inertiaTensor.y, inertiaTensor.z);
		quat.identity();
		quat.rotateXYZ(euler.x, euler.y, euler.z);
		axisAngle.set(quat);

		return new AngularAcceleration(new Vec3(axisAngle.x, axisAngle.y, axisAngle.z), axisAngle.angle);
	}

	@Override
	public boolean isApproxZero() { return Maths.approx(Magnitude, 0d); }
	@Override @Nonnull
	public Torque inverse() { return new Torque(Axis, -Magnitude); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearForce getLinearComponent(@Nonnull Transform actingOn) { throw new NotImplementedException(); }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public Torque getTorqueComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public String toString() { return "Torque ["+Magnitude+"] around ["+Axis+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysics.torque", Magnitude, Axis.x, Axis.y, Axis.z); }

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof Torque otherForce)
			return otherForce.Axis.equals(Axis) && Maths.approx(Magnitude, otherForce.Magnitude);
		return false;
	}
	public boolean isApprox(@Nonnull Torque other) { return Maths.approx(other.Axis, Axis) && Maths.approx(other.Magnitude, Magnitude); }
	public boolean isApprox(@Nonnull Torque other, double epsilon) { return Maths.approx(other.Axis, Axis, epsilon) && Maths.approx(other.Magnitude, Magnitude, epsilon); }

}