package com.flansmod.client.render.vehicles;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.longdistance.LongDistanceEntity;
import com.flansmod.common.entity.longdistance.LongDistanceVehicle;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Debug;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Vector;
import java.util.function.Function;

public class VehicleDebugRenderer
{
	private static final float ArrowLengthSeconds = 1f/20f;
	private record DebugPalette(@Nonnull Vector4f Default,
								@Nonnull Vector4f MotionCurrent,
								@Nonnull Vector4f MotionNext,
								@Nonnull Vector4f TotalForce,
								@Nonnull Vector4f WheelForces,
								@Nonnull Vector4f WheelCurrent,
								@Nonnull Vector4f WheelNext,
								@Nonnull Vector4f CoreForces,
								@Nonnull Vector4f CoreCurrent,
								@Nonnull Vector4f CoreNext)
	{}

	// Client palette is red(forces) -> blue(motions)
	private static final DebugPalette Client = new DebugPalette(
		new Vector4f(1f, 1f, 1f, 1f),			// Default
		new Vector4f(0.25f, 0.25f, 1.0f, 1f),	// MotionCurrent
		new Vector4f(0f, 0f, 1f, 1f),			// MotionNext
		new Vector4f(1f, 0.75f, 0.75f, 1f),		// TotalForce
		new Vector4f(1f, 0f, 0f, 1f),			// WheelForces
		new Vector4f(0.125f, 0.125f, 1f, 0.5f),	// WheelCurrent
		new Vector4f(0.25f, 0.25f, 1f, 0.25f),	// WheelNext
		new Vector4f(1f, 0f, 0f, 1f),			// CoreForces
		new Vector4f(0.125f, 0.125f, 1f, 0.5f),	// CoreCurrent
		new Vector4f(0.25f, 0.25f, 1f, 0.25f));	// CoreNext

	// Client palette is green -> yellow
	private static final DebugPalette Server = new DebugPalette(
		new Vector4f(1f, 1f, 1f, 1f),			// Default
		new Vector4f(1f, 1f, 0.25f, 1f),		// MotionCurrent
		new Vector4f(1f, 1f, 0f, 1f),			// MotionNext
		new Vector4f(0.75f, 1f, 0.75f, 1f),		// TotalForce
		new Vector4f(0f, 1f, 0f, 1f),			// WheelForces
		new Vector4f(1f, 1f, 0.125f, 0.5f),		// WheelCurrent
		new Vector4f(1f, 1f, 0.25f, 0.25f),		// WheelNext
		new Vector4f(0f, 1f, 0f, 1f),			// CoreForces
		new Vector4f(1f, 1f, 0.125f, 0.5f),		// CoreCurrent
		new Vector4f(1f, 1f, 0.25f, 0.25f));	// CoreNext



	public VehicleDebugRenderer()
	{
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public void ClientTick(@Nonnull TickEvent.ClientTickEvent event)
	{
		if(event.phase != TickEvent.Phase.END)
			return;

		if (FlansMod.DEBUG || Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
		{
			ClientLevel level = Minecraft.getInstance().level;
			if (level != null)
			{

				for (LongDistanceEntity longEntity : FlansModClient.CLIENT_LONG_DISTANCE.GetAllLongEntities(level))
				{
					if (longEntity instanceof LongDistanceVehicle longVehicle)
					{
						// Render a long distance debug view
					}
				}

				DebugRender(level.entitiesForRendering(), Client);
			}

			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null)
			{
				for (ServerLevel loadedLevel : server.getAllLevels())
				{
					DebugRender(loadedLevel.getAllEntities(), Server);
				}
			}
		}
	}

	private void DebugRender(@Nonnull Iterable<Entity> entityList, @Nonnull DebugPalette palette)
	{
		for(Entity entity : entityList)
		{
			// Render a regular entity debug view
			if(entity instanceof VehicleEntity vehicle)
			{
				ForceModel forces = vehicle.Physics().ForcesLastFrame;

				Transform vehiclePos = vehicle.GetWorldToEntity().GetCurrent();

				DebugRenderer.RenderAxes(vehiclePos, 1, palette.Default);
				DebugRenderer.RenderCube(vehiclePos, 1, palette.CoreCurrent, new Vector3f(0.6f, 0.25f, 0.6f));
				Vec3 coreMotionNextFrame = DebugRenderForces(forces.Debug_GetForcesOnCore(), vehicle.getDeltaMovement(), vehicle.GetWorldToEntity().GetCurrent(), palette, true, vehicle.Physics().Def.mass, vehicle.Hierarchy()::GetWorldToPartCurrent);
				Transform vehiclePosNext = Transform.Compose(vehiclePos, Transform.FromPos(coreMotionNextFrame.scale(1f/20f)));
				DebugRenderer.RenderCube(vehiclePosNext, 1, palette.CoreNext,  new Vector3f(0.6f, 0.25f, 0.6f));

				for(int wheelIndex = 0; wheelIndex < vehicle.Physics().AllWheels().size(); wheelIndex++)
				{
					WheelEntity wheel = vehicle.Physics().WheelByIndex(wheelIndex);
					if(wheel != null)
					{
						Transform wheelPos = Transform.FromPos(wheel.position());
						Vector3f debugWheelBoxSize = new Vector3f(0.5f * wheel.Def.radius, wheel.Def.radius, wheel.Def.radius);
						DebugRenderer.RenderAxes(wheel.GetWorldToEntity().GetCurrent(), 1, palette.Default);
						DebugRenderer.RenderCube(wheelPos, 1, palette.WheelCurrent, debugWheelBoxSize);

						Vec3 wheelMotionNextFrame = DebugRenderForces(forces.Debug_GetForcesOnWheel(wheelIndex), wheel.getDeltaMovement(), wheel.GetWorldToEntity().GetCurrent(), palette, false, wheel.Def.mass, vehicle.Hierarchy()::GetWorldToPartCurrent);
						Transform wheelPosNext = Transform.Compose(wheelPos, Transform.FromPos(wheelMotionNextFrame.scale(1f/20f)));
						DebugRenderer.RenderCube(wheelPosNext, 1, palette.WheelNext, debugWheelBoxSize);
					}
				}
			}
		}
	}



	private Vec3 DebugRenderForces(@Nullable ForceModel.ForcesOnPart forces,
								   @Nonnull Vec3 motion,
								   @Nonnull Transform worldTransform,
								   @Nonnull DebugPalette palette,
								   boolean isCore,
								   float mass,
								   @Nonnull Function<String, Transform> lookup)
	{
		float inertia = 1.0f / mass;
		float arrowScale = 1.0f / 20f;
		if(forces != null)
		{
			Vec3 origin = worldTransform.PositionVec3();
			Vec3 accelerationTotal = new Vec3(0d, 0d, 0d);
			Vector4f forceColour = isCore ? palette.CoreForces : palette.WheelForces;
			for(ForceModel.Force global : forces.GlobalForces)
			{
				Vec3 acceleration = global.Vector().scale(inertia);
				DebugRenderer.RenderArrow(origin, 1, forceColour, acceleration.scale(arrowScale));
				accelerationTotal = accelerationTotal.add(acceleration);
			}
			for(ForceModel.OffsetForce global : forces.OffsetGlobalForces)
			{
				Vec3 offsetOrigin = origin.add(global.Offset());
				Vec3 acceleration = global.Vector().scale(inertia);
				DebugRenderer.RenderArrow(offsetOrigin, 1, forceColour, acceleration.scale(arrowScale));
				accelerationTotal = accelerationTotal.add(acceleration);
			}
			for(ForceModel.Force local : forces.LocalForces)
			{
				Vec3 global = worldTransform.LocalToGlobalDirection(local.Vector());
				Vec3 acceleration = global.scale(inertia);
				accelerationTotal = accelerationTotal.add(acceleration);
				DebugRenderer.RenderArrow(origin, 1, forceColour, acceleration.scale(arrowScale));
			}
			for(ForceModel.OffsetForce local : forces.OffsetLocalForces)
			{
				Vec3 offsetOrigin = worldTransform.LocalToGlobalPosition(local.Offset());
				Vec3 global = worldTransform.LocalToGlobalDirection(local.Vector());
				Vec3 acceleration = global.scale(inertia);
				accelerationTotal = accelerationTotal.add(acceleration);
				DebugRenderer.RenderArrow(offsetOrigin, 1, forceColour, acceleration.scale(arrowScale));
			}
			//for(ForceModel.SpringJoint spring : forces.Springs)
			//{
			//	Transform pullTowards = lookup.apply(spring.PullTowardsAP());
			//	if(pullTowards != null)
			//	{
			//		Vec3 delta = pullTowards.PositionVec3().subtract(origin);
			//		Vec3 springForce = delta.scale(spring.SpringStrength());
			//		Vec3 acceleration = springForce.scale(inertia);
//
			//		DebugRenderer.RenderArrow(origin, 1, SpringColour, acceleration.scale(arrowScale));
			//		DebugRenderer.RenderLine(origin, 1, SpringConnectorColour, delta);
			//		accelerationTotal = accelerationTotal.add(acceleration);
			//	}
			//}

			Vec3 motionNext = motion.add(accelerationTotal);
			DebugRenderer.RenderArrow(origin, 1, palette.MotionCurrent, motion.scale(arrowScale));
			DebugRenderer.RenderArrow(origin, 1, palette.MotionNext, motionNext.scale(arrowScale));

			DebugRenderer.RenderArrow(origin.add(motion.scale(arrowScale)), 1, palette.TotalForce, accelerationTotal.scale(arrowScale));
			return motionNext;
		}
		return Vec3.ZERO;
	}
}
