package com.flansmod.physics.common.util.shapes;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public record Plane(@Nonnull Vec3 Normal, double Distance) implements IPlane
{
    @Nonnull
    public static Plane of(@Nonnull Vec3 normal, double distance) { return new Plane(normal, distance); }
    @Nonnull
    public static Plane ofNormalAndPointOnPlane(@Nonnull Vec3 normal, @Nonnull Vec3 point) { return new Plane(normal, point.dot(normal)); }
    @Nonnull
    public static Plane of(@Nonnull ISeparationAxis axis, double distance) { return new Plane(axis.getNormal(), distance); }

    @Override
    public double getDistance() { return Distance; }
    @Override @Nonnull
    public Vec3 getNormal() { return Normal; }
    @Override
    public double project(@Nonnull Vec3 point) { return point.dot(Normal); }

    @Override @Nonnull
    public Optional<Vec3> rayPlaneIntersect(@Nonnull Vec3 origin, @Nonnull Vec3 ray)
    {
        // (Origin + t * Ray).V == d
        // Solve for t
        // (R.V)t = d - O.V
        double rDotV = ray.dot(Normal);
        if(Maths.approx(rDotV, 0d))
            return Optional.empty();

        double oDotV = origin.dot(Normal);
        double t = (Distance - oDotV) / rDotV;
        return Optional.of(origin.add(ray.scale(t)));
    }
}
