package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.collision.obb.ICollisionAccessDynamicObject;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;

import com.flansmod.physics.common.util.Transform;
import com.mojang.datafixers.util.Pair;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollisionTaskResolveDynamic
        implements ICollisionTask<CollisionTaskResolveDynamic.Input, CollisionTaskResolveDynamic.Output>
{
    public record Input(@Nonnull ICollisionAccessDynamicObject Dynamic,
                        @Nonnull List<DynamicCollisionEvent> DynamicCollisions,
                        @Nonnull List<StaticCollisionEvent> StaticCollisions)
    {

    }
    public record Output(double resolvedPartialTick,
                         @Nonnull Transform ResolvedLocation,
                         @Nonnull CompoundVelocity ResolvedVelocity)
    {

    }
    @Nonnull
    public static CollisionTaskResolveDynamic of(@Nonnull ColliderHandle handle,
                                                 @Nonnull ICollisionAccessDynamicObject dynamic,
                                                 @Nonnull List<DynamicCollisionEvent> dynamicCollisions,
                                                 @Nonnull List<StaticCollisionEvent> staticCollisions)
    {
        var task = new CollisionTaskResolveDynamic(handle);
        task.prepare(new Input(dynamic, dynamicCollisions, staticCollisions));
        return task;
    }

    @Nonnull
    public final ColliderHandle Handle;
    @Nullable
    private Input Input;
    @Nullable
    private Output Output;

    private CollisionTaskResolveDynamic(@Nonnull ColliderHandle handle)
    {
        Handle = handle;
    }

    @Override
    public void prepare(@Nonnull CollisionTaskResolveDynamic.Input input)
    {
        Input = input;
    }

    @Override
    public boolean canRun() { return Input != null; }

    @Nonnull
    private List<Pair<Double, StaticCollisionEvent>> sort(@Nonnull TransformedBBCollection currentBBs,
                                                          @Nonnull List<StaticCollisionEvent> unsorted)
    {
        List<Pair<Double, StaticCollisionEvent>> byIntersectionTime = new ArrayList<>(unsorted.size());
        for(StaticCollisionEvent event : unsorted)
        {
            double aMaxBefore = event.separationPlane().getOBBsHeightAbove(currentBBs);
            double aMaxAfter = 0d;

            double intersectionFractionalTick;
            if(aMaxBefore > aMaxAfter)
            {
                // This motion is already moving us out of the collision,
                // which makes it the lowest priority to resolve
                intersectionFractionalTick = 1.0d;
            }
            else
            {
                double depthPost = event.depth();
                double depthPre = depthPost - aMaxBefore;
                double depthDelta = depthPost - depthPre;
                if(Maths.approx(depthDelta, 0d))
                {
                    // The depth did not change this tick? So just process last.
                    intersectionFractionalTick = 1d;
                }
                else
                {
                    // Otherwise, roughly work out the intersection time
                    intersectionFractionalTick = Maths.clamp((-depthPre) / (depthPost - depthPre), 0d, 1d);
                }
            }
            byIntersectionTime.add(Pair.of(intersectionFractionalTick, event));
        }

        byIntersectionTime.sort(Comparator.comparingDouble(Pair::getFirst));
        return byIntersectionTime;
    }
    private double getFirstIntersectTime(@Nonnull List<Pair<Double, StaticCollisionEvent>> byIntersectionTime)
    {
        if(byIntersectionTime.isEmpty())
            return 1.0d;
        return byIntersectionTime.get(0).getFirst();
    }
    @Nonnull
    private List<Pair<Double, StaticCollisionEvent>> popSimultaneousEvents(@Nonnull List<Pair<Double, StaticCollisionEvent>> byIntersectionTime, double epsilon)
    {
        if(byIntersectionTime.size() == 0)
            return List.of();

        List<Pair<Double, StaticCollisionEvent>> simultaneousGroup = new ArrayList<>(byIntersectionTime.size());
        simultaneousGroup.add(byIntersectionTime.get(0));
        double t = byIntersectionTime.get(0).getFirst();

        for(int i = 1; i < byIntersectionTime.size(); i++)
        {
            double ti = byIntersectionTime.get(i).getFirst();
            if(Maths.approx(t, ti, epsilon))
                simultaneousGroup.add(byIntersectionTime.get(i));
            else
                break;
        }
        return simultaneousGroup;
    }
    private void resolveEventAtTime(@Nonnull StaticCollisionEvent event, double t)
    {

    }

    @Override
    public void run()
    {
        if(Input == null)
        {
            Output = new Output(1d, Transform.IDENTITY, CompoundVelocity.Zero);
            return;
        }

        LinearVelocity linearV = Input.Dynamic.getLinearVelocity();
        AngularVelocity angularV = Input.Dynamic.getAngularVelocity();

        Transform currentLocation = Input.Dynamic().getCurrentLocation();
        TransformedBBCollection currentBBs = new TransformedBBCollection(currentLocation, Input.Dynamic.getCurrentColliders().Colliders());

        Transform pendingLocation;
        //    = extrapolate(currentLocation, linearV, angularV);
        //DebugRenderer.renderCube(pendingLocation, 4, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), Input.Dynamic.getPendingBB().HalfExtents());

        // Sort our list by intersection time
        List<Pair<Double, StaticCollisionEvent>> byIntersectionTime = sort(currentBBs, Input.StaticCollisions);

        // Cap our movement parametrically to the first intersection
        double firstIntersect = getFirstIntersectTime(byIntersectionTime);
        pendingLocation = extrapolate(currentLocation, linearV, angularV, firstIntersect);
        Transform projectedLocation = pendingLocation;

        // Now resolve our velocity by impulse calculation
        CompoundVelocity compoundV = CompoundVelocity.of(linearV, angularV);
        while(!byIntersectionTime.isEmpty())
        {
            List<Pair<Double, StaticCollisionEvent>> nextGroup = popSimultaneousEvents(byIntersectionTime, Maths.Epsilon);
            CompoundVelocity[] impulseResults = new CompoundVelocity[nextGroup.size()];

            for(int i = 0; i < nextGroup.size(); i++)
            {
                StaticCollisionEvent collision = nextGroup.get(i).getSecond();
                impulseResults[i] =
                    CollisionTasks.findResponseByImpulseAgainstStatic(
                        compoundV, pendingLocation.positionVec3(), Input.Dynamic.getInverseMass(), Input.Dynamic.getInertiaTensor(),
                        collision.contactSurface().getAveragePos(), collision.separationPlane().getNormal(),
                        0.6d);

                // EXCEPT: If pushing in this direction would actually increase the intersection depth of another collision!


                projectedLocation =
                    CollisionTasks.resolveByProjectionAgainstStatic(
                        projectedLocation,
                        collision.separationPlane().getNormal(),
                        collision.depth());
            }

            compoundV = compoundV.compose(CompoundVelocity.average(impulseResults));
            break;
        }

        Output = new Output(firstIntersect, projectedLocation, compoundV);


/* Old position process
        List<StaticCollisionEvent> toProcess = new ArrayList<>(Input.StaticCollisions);
        while(!toProcess.isEmpty())
        {
            // Find the deepest collision to process next
            StaticCollisionEvent deepestCollision = null;
            double deepestCollisionDepth = 0d;
//
            TransformedBBCollection bbs = new TransformedBBCollection(pendingLocation, Input.Dynamic.getCurrentColliders().Colliders());
            for(StaticCollisionEvent event : toProcess)
            {
                // We re-test using the response vectors we have chosen so far.
                double updatedDepth = event.separationPlane().getOBBsHeightAbove(bbs);
                // If this has been resolved by an earlier choice, skip it
                if(updatedDepth >= 0.0d)
                {
                    continue;
                }
                if(deepestCollision == null || updatedDepth < deepestCollisionDepth)
                {
                    deepestCollision = event;
                    deepestCollisionDepth = updatedDepth;
                }
            }
//
            // It is valid to not select anything. This happens if resolving A entirely resolves B and requires no further movement
            if(deepestCollision == null)
                break;
//
            Transform projectedLocation =
                    CollisionTasks.resolveByProjectionAgainstStatic(
                            pendingLocation,
                            deepestCollision.separationPlane().getNormal(),
                            deepestCollisionDepth);
            pendingLocation = projectedLocation;
//
            // And move it to our chosen list
            toProcess.remove(deepestCollision);
        }


 */

            /*
        // Here  we want to work out how to weight each collision
        double totalArea = 0d;
        int numCollisionsWithZeroArea = 0;
        int numCollisionsWithNonZeroArea = 0;
        for (StaticCollisionEvent collision : Input.StaticCollisions)
        {
            double collisionSurfaceArea = collision.contactSurface().getArea();

            if(collisionSurfaceArea > 0d)
            {
                totalArea += collisionSurfaceArea;
                numCollisionsWithNonZeroArea++;
            }
            else
                numCollisionsWithZeroArea++;
        }

        double zeroContactSize = 1d;
        if(numCollisionsWithNonZeroArea > 0)
        {
            double averageArea = totalArea / numCollisionsWithNonZeroArea;
            zeroContactSize = averageArea;
            totalArea += numCollisionsWithZeroArea * zeroContactSize;
        }
        else
        {
            totalArea = numCollisionsWithZeroArea;
        }

        CompoundVelocity impulseAppliedVelocity = CompoundVelocity.of(linearV, angularV);
        for (StaticCollisionEvent collision : Input.StaticCollisions)
        {
            // Not "pendingLocation", but an interpolation at this point in "time"
            Vec3 boxCenterAtCollisionT = pendingLocation.positionVec3();



            CompoundVelocity correctedForImpulse =
                    CollisionTasks.resolveByImpulseAgainstStatic(
                            CompoundVelocity.of(linearV, angularV), boxCenterAtCollisionT, Input.Dynamic.getInverseMass(), Input.Dynamic.getInertiaTensor(),
                            collision.contactSurface().getAveragePos(), collision.separationPlane().getNormal(),
                            0.6d);

            double surfaceArea = collision.contactSurface().getArea();
            if(surfaceArea <= 0d)
                surfaceArea = zeroContactSize;

            double contribution = (surfaceArea / totalArea);

            LinearVelocity linearComponent = correctedForImpulse.linear().subtract(linearV).scale(contribution);
            AngularVelocity angularComponent = correctedForImpulse.angular().compose(angularV.inverse()).scale(contribution);


            double angularCap = 1d;
            for(Vec3 collisionSurfaceVertex : collision.contactSurface().getVertices())
            {
                Vec3 relative = collisionSurfaceVertex.subtract(pendingLocation.positionVec3());
                LinearVelocity vAtVertex = angularComponent.atOffset(relative).add(linearComponent);
                Vec3 deltaVertex = vAtVertex.applyOneTick();

                double depthPre = collision.separationPlane().getPointHeightAbove(collisionSurfaceVertex);
                double depthPost = collision.separationPlane().getPointHeightAbove(collisionSurfaceVertex.add(deltaVertex));

                if(depthPost < depthPre && depthPost <= 0d)
                {
                    double newAngularCap = Maths.clamp(depthPre / (depthPre - depthPost), 0d, 1d);
                    if(newAngularCap < angularCap)
                        angularCap = newAngularCap;
                }
            }

            impulseAppliedVelocity = CompoundVelocity.of(
                    impulseAppliedVelocity.linear().add(linearComponent),
                    impulseAppliedVelocity.angular().compose(angularComponent.scale(angularCap)));
        }


		// Update our velocities from the resolution
		//LinearVelocity linearVDelta = impulseAppliedVelocity.linear().subtract(linearV);
		//AngularVelocity angularVDelta = impulseAppliedVelocity.angular().compose(angularV.inverse());
		//pendingLocation = extrapolate(pendingLocation, linearVDelta, angularVDelta);

        Output = new Output(pendingLocation, impulseAppliedVelocity);

             */

        //Vec3 linearSum = Vec3.ZERO;
        // Quaternionf angularSum = new Quaternionf();


        //CompoundAcceleration responseTotal = CompoundAcceleration.of(
        //        LinearAcceleration.fromUtoVinTicks(linearV, LinearVelocity.Zero, 1),
        //        AngularAcceleration.Zero);

        //ImmutableList.Builder<IAcceleration> reactionImpulses = ImmutableList.builder();
        //for(int i = 0; i < appliedResponses.size(); i++)
        //{
        //    LinearVelocity linearResponseV = LinearVelocity.blocksPerTick(appliedResponses.get(i).Vector());
//
        //    // TODO: What if two accelerations resolve each other? I think we already covered it?
        //    // LinearAcceleration.fromUtoVinTicks(linearV, linearResponseV, 1);
        //    LinearAcceleration linearResponseA = LinearAcceleration.fromUtoVinTicks(linearV, linearResponseV, 1);
        //    OffsetAcceleration offsetResponseA = OffsetAcceleration.offset(linearResponseA, appliedResponses.get(i).Origin());
//
        //    reactionImpulses.add(offsetResponseA);

        //responseTotal = CompoundAcceleration.of(
        //        responseTotal.linear().add(offsetResponseA.getLinearComponent(pendingLocation)),
        //        responseTotal.angular().compose(offsetResponseA.getAngularComponent(pendingLocation))
        //);

        //Quaternionf rot = appliedResponses.get(i).getAngularComponent(pendingLocation).get(new Quaternionf());
        //angularSum.mul(rot);
        //}
        //AxisAngle4f radsPerTick = new AxisAngle4f().set(angularSum);
        //if(Maths.approx(Maths.lengthSqr(radsPerTick.x, radsPerTick.y, radsPerTick.z), 0d))
        //    radsPerTick.set(radsPerTick.angle, 0f, 1f, 0f);

        //LinearVelocity linearTarget = LinearVelocity.blocksPerTick(linearSum);
        //AngularVelocity angularTarget = AngularVelocity.radiansPerTick(radsPerTick);
        //LinearAcceleration linearImpulse = LinearAcceleration.fromUtoVinTicks(linearV, linearTarget, 1);
        //AngularAcceleration angularImpulse = AngularAcceleration.fromUtoVinTicks(angularV, angularTarget, 1);

        //Output = new Output(reactionImpulses.build());



        //Input.Dynamic.ExtrapolateNextFrameWithReaction();

        //double xRange = ProjectedRange.isNonZero(xMoveReq) ? ProjectedRange.width(xMoveReq) : Double.MAX_VALUE;
        //double yRange = ProjectedRange.isNonZero(yMoveReq) ? ProjectedRange.width(yMoveReq) : Double.MAX_VALUE;
        //double zRange = ProjectedRange.isNonZero(zMoveReq) ? ProjectedRange.width(zMoveReq) : Double.MAX_VALUE;
//
        //Vec3 forceOrigin = Input.Dynamic.getCurrentLocation().positionVec3();
//
        //// Clamp on the smallest non-zero axis
        //if(xRange <= yRange && xRange <= zRange)
        //{
        //    v = new Vec3(ProjectedRange.clamp(xMoveReq, v.x), v.y, v.z);
        //    if(!relevantX.isEmpty())
        //    {
        //        forceOrigin = Vec3.ZERO;
        //        for (StaticCollisionEvent x : relevantX)
        //            forceOrigin.add(x.contactSurface().GetAveragePos());
        //        forceOrigin = forceOrigin.scale(1d / relevantX.size());
        //    }
        //}
        //else if(yRange <= zRange)
        //{
        //    v = new Vec3(v.x, ProjectedRange.clamp(yMoveReq, v.y), v.z);
        //    if(!relevantY.isEmpty())
        //    {
        //        forceOrigin = Vec3.ZERO;
        //        for (StaticCollisionEvent y : relevantY)
        //            forceOrigin.add(y.contactSurface().GetAveragePos());
        //        forceOrigin = forceOrigin.scale(1d / relevantY.size());
        //    }
        //}
        //else
        //{
        //    v = new Vec3(v.x, v.y, ProjectedRange.clamp(zMoveReq, v.z));
        //    if(!relevantZ.isEmpty())
        //    {
        //        forceOrigin = Vec3.ZERO;
        //        for (StaticCollisionEvent z : relevantZ)
        //            forceOrigin.add(z.contactSurface().GetAveragePos());
        //        forceOrigin = forceOrigin.scale(1d / relevantZ.size());
        //    }
        //}

        // TODO: Check if this resolves all collisions
        // If not, clamp another axis

        // Now, we know what the maximum v is, we need to work out what the reaction force is that results in this v

        //LinearVelocity maxV = LinearVelocity.blocksPerTick(v);
        //LinearAcceleration reactionAcc = LinearAcceleration.reaction(linearV, maxV);

        //dyn.SetLinearVelocity(LinearVelocity.blocksPerTick(v));
        //dyn.SetAngularVelocity(angularV.scale(maxT));

        // TODO: CHECK, we used to set the v/q direct, now we apply reaction
        // dyn.ExtrapolateNextFrame(v, q);


    }

    @Nonnull
    private Transform extrapolate(@Nonnull Transform t, @Nonnull LinearVelocity linearV, @Nonnull AngularVelocity angularV, double parameter)
    {
        if(Maths.approx(parameter, 0d))
            return t;
        return Transform.fromPosAndQuat(
            t.positionVec3().add(linearV.applyOverTicks(parameter)),
            t.Orientation.mul(angularV.applyOverTicks(parameter), new Quaternionf()));
    }

    @Nonnull
    private Transform extrapolate(@Nonnull Transform t, @Nonnull LinearVelocity linearV, @Nonnull AngularVelocity angularV)
    {
        return Transform.fromPosAndQuat(
                t.positionVec3().add(linearV.applyOneTick()),
                t.Orientation.mul(angularV.applyOneTick(), new Quaternionf()));
    }

    @Override
    public boolean isComplete() { return Output != null; }
    @Override
    public boolean canCancel() { return false; }
    @Override
    public void cancel() { }
    @Override @Nullable
    public Output getResult()
    {
        return Output;
    }
}
