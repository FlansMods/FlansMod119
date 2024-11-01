package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.ProjectedRange;

import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionTaskResolveDynamic
        implements ICollisionTask<CollisionTaskResolveDynamic.Input, CollisionTaskResolveDynamic.Output>
{
    public record Input(@Nonnull IConstDynamicObject Dynamic,
                        @Nonnull List<DynamicCollisionEvent> DynamicCollisions,
                        @Nonnull List<StaticCollisionEvent> StaticCollisions)
    {

    }
    public record Output(@Nonnull List<IAcceleration> ReactionImpulses)
    {

    }
    @Nonnull
    public static CollisionTaskResolveDynamic of(@Nonnull ColliderHandle handle,
                                                 @Nonnull IConstDynamicObject dynamic,
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

    @Override
    public void run()
    {
        if(Input == null)
        {
            Output = new Output(ImmutableList.of());
            return;
        }

        LinearVelocity linearV = Input.Dynamic.getNextFrameLinearVelocity();
        AngularVelocity angularV = Input.Dynamic.getNextFrameAngularVelocity();

        Vec3 v = linearV.applyOneTick();
        Quaternionf q = angularV.applyOneTick();

        ProjectedRange xMoveReq = null, yMoveReq = null, zMoveReq = null;
        List<StaticCollisionEvent> relevantX = new ArrayList<>(), relevantY = new ArrayList<>(), relevantZ = new ArrayList<>();

        Map<StaticCollisionEvent, Ray> responseVectors = new HashMap<>();
        List<Ray> appliedResponses = new ArrayList<>();
        Transform pendingLocation = Input.Dynamic().getPendingLocation();
        DebugRenderer.renderCube(pendingLocation, 4, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), Input.Dynamic.getPendingBB().HalfExtents());

        TransformStack locationAfterResponse = TransformStack.of(pendingLocation);

        for (StaticCollisionEvent collision : Input.StaticCollisions)
        {
            Vec3 pushOutVec = collision.separationPlane().getNormal().scale(-collision.depth());
            pushOutVec = v.subtract(pushOutVec);

            if(collision.contactSurface().GetNumVertices() > 0)
                responseVectors.put(collision, new Ray(collision.contactSurface().getAveragePos(), pushOutVec));
            else
                responseVectors.put(collision, new Ray(pendingLocation.positionVec3(), pushOutVec));

            //if(!Maths.approx(pushOutVec.x, 0d))
            //{
            //    xMoveReq = ProjectedRange.add(xMoveReq, pushOutVec.x);
            //    relevantX.add(collision);
            //}
            //if(!Maths.approx(pushOutVec.y, 0d))
            //{
            //    yMoveReq = ProjectedRange.add(yMoveReq, pushOutVec.y);
            //    relevantY.add(collision);
            //}
            //if(!Maths.approx(pushOutVec.z, 0d))
            //{
            //    zMoveReq = ProjectedRange.add(zMoveReq, pushOutVec.z);
            //    relevantZ.add(collision);
            //}
        }

        while(!responseVectors.isEmpty())
        {
            // Find the deepest collision to process next
            Map.Entry<StaticCollisionEvent, Ray> deepestCollision = null;
            double deepestCollisionDepth = 0d;
            TransformedBBCollection bbs = new TransformedBBCollection(locationAfterResponse.top(), Input.Dynamic.getCurrentColliders().Colliders());
            for(var kvp : responseVectors.entrySet())
            {
                // We re-test using the response vectors we have chosen so far.
                double updatedDepth = kvp.getKey().separationPlane().getOBBsHeightAbove(bbs);
                // If this has been resolved by an earlier choice, skip it
                if(updatedDepth >= 0.0d)
                {
                    continue;
                }
                if(deepestCollision == null || updatedDepth < deepestCollisionDepth)
                {
                    deepestCollision = kvp;
                    deepestCollisionDepth = updatedDepth;
                }
            }

            // It is valid to not select anything. This happens if resolving A entirely resolves B and requires no further movement
            if(deepestCollision == null)
                break;

            // Static object = infinite mass = 0 inverse mass
            double totalInverseMass = 1d + 0d;

            Vec3 pNormal = deepestCollision.getKey().separationPlane().getNormal();

            Vec3 collisionCenter = deepestCollision.getKey().contactSurface().getAveragePos();
            Vec3 collisionRelativeToA = collisionCenter.subtract(bbs.Location().positionVec3());
            //Vec3 relativeB = collisionCenter.subtract(deepestCollision.getKey())

            LinearVelocity effectiveAngularVelocityAtContact = angularV.atOffset(collisionRelativeToA);
            LinearVelocity totalLinearVelocityA = linearV.add(effectiveAngularVelocityAtContact);
            LinearVelocity staticVelocityB = LinearVelocity.Zero;
            LinearVelocity contactVelocity = staticVelocityB.subtract(totalLinearVelocityA);

            double impusleForce = contactVelocity.Velocity().dot(pNormal);

            Vec3 collisionTangentA = collisionRelativeToA.cross(pNormal);
            Vec3 cA = Input.Dynamic.getInertiaTensor().multiply(collisionTangentA);
            Vec3 inertiaA = cA.cross(collisionRelativeToA);
            // Static impact
            Vec3 inertiaB = Vec3.ZERO;



            // Rotate our temporary transform stack



            locationAfterResponse.add(Transform.fromPosAndQuat(
                    deepestCollision.getValue().Vector(),
                    deepestCollision.getValue().getAngularComponent(pendingLocation).get(new Quaternionf())));

            // And move it to our chosen list
            appliedResponses.add(deepestCollision.getValue());
            responseVectors.remove(deepestCollision.getKey());
        }


        //Vec3 linearSum = Vec3.ZERO;
       // Quaternionf angularSum = new Quaternionf();


        //CompoundAcceleration responseTotal = CompoundAcceleration.of(
        //        LinearAcceleration.fromUtoVinTicks(linearV, LinearVelocity.Zero, 1),
        //        AngularAcceleration.Zero);

        ImmutableList.Builder<IAcceleration> reactionImpulses = ImmutableList.builder();
        for(int i = 0; i < appliedResponses.size(); i++)
        {
            LinearVelocity linearResponseV = LinearVelocity.blocksPerTick(appliedResponses.get(i).Vector());

            // TODO: What if two accelerations resolve each other? I think we already covered it?
            // LinearAcceleration.fromUtoVinTicks(linearV, linearResponseV, 1);
            LinearAcceleration linearResponseA = LinearAcceleration.fromUtoVinTicks(linearV, linearResponseV, 1);
            OffsetAcceleration offsetResponseA = OffsetAcceleration.offset(linearResponseA, appliedResponses.get(i).Origin());

            reactionImpulses.add(offsetResponseA);

           //responseTotal = CompoundAcceleration.of(
           //        responseTotal.linear().add(offsetResponseA.getLinearComponent(pendingLocation)),
           //        responseTotal.angular().compose(offsetResponseA.getAngularComponent(pendingLocation))
           //);

            //Quaternionf rot = appliedResponses.get(i).getAngularComponent(pendingLocation).get(new Quaternionf());
            //angularSum.mul(rot);
        }
        //AxisAngle4f radsPerTick = new AxisAngle4f().set(angularSum);
        //if(Maths.approx(Maths.lengthSqr(radsPerTick.x, radsPerTick.y, radsPerTick.z), 0d))
        //    radsPerTick.set(radsPerTick.angle, 0f, 1f, 0f);

        //LinearVelocity linearTarget = LinearVelocity.blocksPerTick(linearSum);
        //AngularVelocity angularTarget = AngularVelocity.radiansPerTick(radsPerTick);
        //LinearAcceleration linearImpulse = LinearAcceleration.fromUtoVinTicks(linearV, linearTarget, 1);
        //AngularAcceleration angularImpulse = AngularAcceleration.fromUtoVinTicks(angularV, angularTarget, 1);

        Output = new Output(reactionImpulses.build());



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
