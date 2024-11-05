package com.flansmod.physics.common.units;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record AngularVelocity(@Nonnull Vec3 Axis, double Magnitude) implements IVelocity
{
	public static final AngularVelocity Zero = new AngularVelocity(new Vec3(0d, 1d, 0d), 0.0d);

	@Nonnull
	public static AngularVelocity fromQuatPerTick(@Nonnull Quaternionf quaternionPerTick)
	{
		if(quaternionPerTick.equals(Transform.IDENTITY.Orientation, Maths.EpsilonF))
		{
			return AngularVelocity.Zero;
		}

		// From AxisAngle4f.set, but it has isInfinite but does not check isNaN
		float acos = Math.safeAcos(quaternionPerTick.w);
		float invSqrt = Math.invsqrt(1.0f - quaternionPerTick.w*quaternionPerTick.w);
		if(Float.isInfinite(invSqrt) || Float.isNaN(invSqrt))
			return AngularVelocity.Zero;

		return new AngularVelocity(new Vec3(
				quaternionPerTick.x * invSqrt,
				quaternionPerTick.y * invSqrt,
				quaternionPerTick.z * invSqrt),
				acos + acos).checkNaN();
	}
	@Nonnull
	public static AngularVelocity fromQuatPerSecond(@Nonnull Quaternionf quaternionPerSecond)
	{
		return fromQuatPerTick(quaternionPerSecond).scale(Units.AngularSpeed.RadiansPerSecond_To_RadiansPerTick).checkNaN();
	}

	@Nonnull
	public static AngularVelocity radiansPerSecond(@Nonnull Vec3 axis, double radiansPerSecond)
	{
		return new AngularVelocity(axis, Units.AngularSpeed.RadiansPerSecond_To_RadiansPerTick(radiansPerSecond)).checkNaN();
	}
	@Nonnull
	public static AngularVelocity radiansPerSecond(@Nonnull Vec3 axisAngle)
	{
		double mag = axisAngle.length();
		if(mag < Maths.Epsilon)
			return AngularVelocity.Zero;

		Vec3 axis = axisAngle.scale(1d / mag);
		return new AngularVelocity(axis,  Units.AngularSpeed.RadiansPerSecond_To_RadiansPerTick(mag)).checkNaN();
	}
	@Nonnull
	public static AngularVelocity radiansPerSecond(@Nonnull AxisAngle4f radiansPerSecond)
	{
		return new AngularVelocity(new Vec3(radiansPerSecond.x, radiansPerSecond.y, radiansPerSecond.z),
				Units.AngularSpeed.RadiansPerSecond_To_RadiansPerTick(radiansPerSecond.angle)).checkNaN();
	}

	@Nonnull
	public static AngularVelocity radiansPerTick(@Nonnull Vec3 axis, double radiansPerTick)
	{
		return new AngularVelocity(axis, radiansPerTick).checkNaN();
	}
	@Nonnull
	public static AngularVelocity radiansPerTick(@Nonnull Vec3 axisAngle)
	{
		double mag = axisAngle.length();
		if(mag < Maths.Epsilon)
			return AngularVelocity.Zero;

		Vec3 axis = axisAngle.scale(1d / mag);
		return new AngularVelocity(axis, mag).checkNaN();
	}
	@Nonnull
	public static AngularVelocity radiansPerTick(@Nonnull AxisAngle4f radiansPerTick)
	{
		return new AngularVelocity(new Vec3(radiansPerTick.x, radiansPerTick.y, radiansPerTick.z), radiansPerTick.angle).checkNaN();
	}

	@Nonnull
	public static AngularVelocity degreesPerSecond(@Nonnull Vec3 axis, double degressPerSecond)
	{
		return new AngularVelocity(axis, Units.AngularSpeed.DegreesPerSecond_To_RadiansPerTick(degressPerSecond)).checkNaN();
	}
	@Nonnull
	public static AngularVelocity degreesPerSecond(@Nonnull Vec3 axisAngle)
	{
		double mag = axisAngle.length();
		if(mag < Maths.Epsilon)
			return AngularVelocity.Zero;

		Vec3 axis = axisAngle.scale(1d / mag);
		return new AngularVelocity(axis, Units.AngularSpeed.DegreesPerSecond_To_RadiansPerTick(mag)).checkNaN();
	}

	@Nonnull
	public Vec3 asVec3()
	{
		return Axis.scale(Magnitude);
	}

	@Nonnull
	public AngularVelocity compose(@Nonnull AngularVelocity other)
	{
		Vec3 scaledA = asVec3();
		Vec3 scaledB = other.asVec3();
		return radiansPerTick(scaledA.add(scaledB));

		//Quaternionf angularA = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		//Quaternionf angularB = new Quaternionf().setAngleAxis(other.Magnitude, other.Axis.x, other.Axis.y, other.Axis.z);
		//Quaternionf composed = angularA.mul(angularB);
		//return fromQuatPerTick(composed);
	}
	@Nonnull
	public AngularVelocity lerp(@Nonnull AngularVelocity other, float t)
	{
		Quaternionf angularA = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		Quaternionf angularB = new Quaternionf().setAngleAxis(other.Magnitude, other.Axis.x, other.Axis.y, other.Axis.z);
		Quaternionf slerp = angularA.slerp(angularB, t);
		return fromQuatPerTick(slerp);
	}
	@Nonnull
	public static AngularVelocity interpolate(@Nonnull AngularVelocity a, @Nonnull AngularVelocity b, float t) { return a.lerp(b, t); }
	@Nonnull
	public AngularVelocity scale(double scale)
	{
		return new AngularVelocity(Axis, Magnitude * scale).checkNaN();
	}

	@Nonnull
	public LinearVelocity atOffset(@Nonnull Vec3 v)
	{
		if(Maths.approx(v, Vec3.ZERO))
			return LinearVelocity.Zero;

		Vec3 dir = Axis.cross(v.normalize());
		double mag = Magnitude * v.length();
		return new LinearVelocity(dir.scale(mag));
	}

	//
	//@Nonnull
	//public AngularVelocity add(@Nonnull AngularVelocity other) { return new AngularVelocity(Velocity.add(other.Velocity)); }

	@Nonnull
	public Units.AngularSpeed getDefaultUnits() { return Units.AngularSpeed.RadiansPerTick; }
	public double convertToUnits(@Nonnull Units.AngularSpeed toUnits) { return Units.AngularSpeed.Convert(Magnitude, Units.AngularSpeed.RadiansPerTick, toUnits); }

	@Nonnull
	public Quaternionf applyOverTicks(double ticks)
	{
		Quaternionf result = new Quaternionf().setAngleAxis(Magnitude * ticks, Axis.x, Axis.y, Axis.z);
		if(!Maths.approx(result.lengthSquared(), 1f))
			return Transform.IDENTITY.Orientation;
		return result;
	}
	@Nonnull
	public Quaternionf applyOneTick()
	{
		Quaternionf result = new Quaternionf().setAngleAxis(Magnitude, Axis.x, Axis.y, Axis.z);
		if(!Maths.approx(result.lengthSquared(), 1f))
			return Transform.IDENTITY.Orientation;
		return result;
	}

	@Override @Nonnull
	public AngularVelocity inverse() { return new AngularVelocity(Axis, -Magnitude); }
	@Override
	public boolean isApproxZero() { return Maths.approx(Magnitude, 0d); }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearVelocity getLinearComponent(@Nonnull Transform actingOn) { return LinearVelocity.Zero; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularVelocity getAngularComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public String toString() { return "AngularVelocity ["+Units.Angle.Radians_To_Degrees(Magnitude)+"] degrees/tick around ["+Axis+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.angular_velocity", Units.Angle.Radians_To_Degrees(Magnitude), Axis.x, Axis.y, Axis.z); }
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof AngularVelocity otherAngularV)
			return otherAngularV.Axis.equals(Axis) && Maths.approx(Magnitude, otherAngularV.Magnitude);
		return false;
	}
	public boolean isApprox(@Nonnull AngularVelocity other) { return Maths.approx(other.Axis, Axis) && Maths.approx(other.Magnitude, Magnitude); }
	public boolean isApprox(@Nonnull AngularVelocity other, double epsilon) { return Maths.approx(other.Axis, Axis, epsilon) && Maths.approx(other.Magnitude, Magnitude, epsilon); }
	@Nonnull
	private AngularVelocity checkNaN()
	{
		if(isNaN())
			FlansPhysicsMod.LOGGER.error("AngularVelocity is NaN");
		return this;
	}
	public boolean isNaN() { return Double.isNaN(Magnitude) || Double.isNaN(Axis.x) || Double.isNaN(Axis.y) || Double.isNaN(Axis.z); }
	public void toBuf(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeVector3f(Axis.toVector3f());
		buf.writeFloat((float)Magnitude);
	}
	@Nonnull
	public static AngularVelocity fromBuf(@Nonnull FriendlyByteBuf buf)
	{
		Vec3 axis = new Vec3(buf.readVector3f());
		double mag = buf.readFloat();
		return new AngularVelocity(axis, mag);
	}
}
