package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.collision.obb.FullSeparationResult;
import com.flansmod.physics.common.collision.obb.ICollisionAccessDynamicObject;
import com.flansmod.physics.common.collision.obb.IConstDynamicObject;
import com.flansmod.physics.common.collision.obb.SeparationResult;
import com.flansmod.physics.common.util.ProjectedRange;
import com.flansmod.physics.common.util.shapes.*;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CollisionTaskSeparateDynamicFromStatic
	implements ICollisionTask<CollisionTaskSeparateDynamicFromStatic.Input, CollisionTaskSeparateDynamicFromStatic.Output>
{
	public record Input(@Nonnull ICollisionAccessDynamicObject ObjectA,
						@Nonnull ImmutableList<VoxelShape> StaticShapes,
						@Nonnull ImmutableList<ISeparationAxis> ExistingSeparators)
	{

	}
	public record Output(@Nonnull ImmutableList<StaticCollisionEvent> EventsA,
						 @Nullable ImmutableList<ISeparationAxis> NewSeparatorList)
	{

	}


	@Nonnull
	public static CollisionTaskSeparateDynamicFromStatic of(@Nonnull ColliderHandle handleA,
															@Nonnull ICollisionAccessDynamicObject objectA,
															@Nonnull ImmutableList<VoxelShape> staticShapes,
															@Nonnull ImmutableList<ISeparationAxis> existingSeparators)
	{
		CollisionTaskSeparateDynamicFromStatic task = new CollisionTaskSeparateDynamicFromStatic(handleA);
		task.prepare(new CollisionTaskSeparateDynamicFromStatic.Input(objectA, staticShapes, existingSeparators));
		return task;
	}

	@Nonnull
	public final ColliderHandle Handle;
	@Nullable
	private Input Input;
	@Nullable
	private List<ISeparationAxis> NewSeparators = null;
	@Nullable
	private Output Output;
	private boolean Cancelled = false;


	public CollisionTaskSeparateDynamicFromStatic(@Nonnull ColliderHandle handle)
	{
		Handle = handle;
	}
	@Override
	public void prepare(@Nonnull Input input)
	{
		Input = input;
	}
	@Override
	public boolean canRun()
	{
		return Input != null && Input.StaticShapes.size() > 0;
	}
	@Override
	public void run()
	{
		if(Input != null)
		{
			// No colliders?! No collision
			if(Input.StaticShapes.size() == 0)
			{
				Output = new Output(ImmutableList.of(), ImmutableList.of());
				return;
			}



			SeparateStaticAABBs();


			// Bounds test
			//TransformedBB boundsA = Input.ObjectA.GetPendingBB();
			//Pair<Integer, SeparationTestResult> bestSeparator = FindClosestSeparation(boundsA);
			//int voxelIndex = bestSeparator.getFirst();
			//SeparationTestResult bestSeparationResult = bestSeparator.getSecond();

			//if(voxelIndex == -1 || bestSeparationResult == null)
			//{
			//	// Weird, we should find some sort of separation, even if its bad.
			//	Output = new Output(ImmutableList.of(), ImmutableList.of());
			//	return;
			//}
//
			//ImmutableList<ISeparationAxis> newCollidersImmutable = NewSeparators != null ? ImmutableList.copyOf(NewSeparators) : ImmutableList.of();
//
			//// Non-colliding is nice, off we go
			//if(bestSeparationResult.IsNonColliding())
			//{
			//	Output = new Output(ImmutableList.of(), newCollidersImmutable);
			//	return;
			//}
//
			//// So we collided, right? Actually we might be non-colliding if we dig deeper than the bounds
			//// TODO: Fine-grain
//
			//VoxelShape shape = Input.StaticShapes.get(voxelIndex);
			//TransformedBB voxelBB = TransformedBB.Of(shape.bounds());
			////Vec3 pointOnVoxel = bestSeparationResult.GetCollidingPoint(voxelBB);
//
			//Vec3 sepNormal = bestSeparationResult.Separator().GetNormal();
			//Vec3 voxelCenter = voxelBB.GetCenter();
			//double d = bestSeparationResult.Separator().GetDistance();
			//double cDotV = voxelCenter.dot(sepNormal) - d;
			//Vec3 intersectionPoint = voxelCenter.subtract(sepNormal.scale(cDotV));


			//bestSeparationResult.
			//Vec3 pointOnVoxel = voxelBB.GetCenter().add(bestSeparationResult.Separator().GetNormal().scale(bestSeparationResult.Distance()));

			// Collision!
			//Output = new Output(ImmutableList.of(
			//	new StaticCollisionEvent(
			//		intersectionPoint,
			//		bestSeparationResult.Separator().GetNormal().scale(-1d),
			//		bestSeparationResult.GetCollisionDepth())),
			//	newCollidersImmutable);
		}
	}




	private void SeparateStaticAABBs()
	{
		FullSeparationResult[] results = new FullSeparationResult[Input.StaticShapes.size()];
		TransformedBB boundsA = Input.ObjectA.getPendingBB();

		List<ISeparationAxis> newSeparatorList = new ArrayList<>();
		ImmutableList.Builder<StaticCollisionEvent> collisions = ImmutableList.builder();

		int totalSeparated = 0;
		for(ISeparationAxis separator : Input.ExistingSeparators)
		{
			// Check if this separator still bounds our dynamic object
			ProjectedRange projectionA = separator.projectOBBMinMax(boundsA);
			IPlane testPlane = Plane.of(separator, projectionA.max());

			// Now apply to our unseparated entries
			int numSeparated = ApplySeparation(testPlane, results);

			// Only keep this separator if it did something
			if(numSeparated > 0) {
				newSeparatorList.add(separator);
				totalSeparated += numSeparated;
			}
		}

		// Now keep separating until everything is done
		while(totalSeparated < Input.StaticShapes.size())
		{
			// So something is not separated from our dynamic object
			int indexToProcess = -1;
			for(int i = 0; i < results.length; i++)
				if(results[i] == null)
					indexToProcess = i;

			if(indexToProcess == -1)
			{
				FlansPhysicsMod.LOGGER.error("Couldn't complete static separation algorithm???");
				break;
			}

			// Let's process it
			VoxelShape shape = Input.StaticShapes.get(indexToProcess);
			AABB voxelBB = shape.bounds();
			FullSeparationResult separationResult = CollisionTasks.separateGetAllOptions(boundsA, voxelBB);

			// Directly place it in the array, so we don't test it again
			results[indexToProcess] = separationResult;
			totalSeparated++;

			if(separationResult.success())
			{
				// Now check to see if we can quickly split any of the other outstanding pieces
				SeparationResult successfulResult = separationResult.getSuccessfulResult();
				if(successfulResult != null)
				{
					totalSeparated += ApplySeparation(successfulResult.separator(), results);
					newSeparatorList.add(successfulResult.separator());
				}
			}
			else
			{
				//collidingResults.add(separationResult);
			}
		}


		// Now resolve all the failures collectively, picking the most common normal
		Vec3 strongestNormalDir = Vec3.ZERO;
		for(FullSeparationResult fullResult : results)
		{
			if(fullResult.success())
				continue;

			for(SeparationResult option : fullResult.options())
				strongestNormalDir = strongestNormalDir.add(option.separator().getNormal());//.scale(1d / -option.depth()));
		}
		strongestNormalDir = strongestNormalDir.normalize();

		DebugRenderer.renderArrow(Input.ObjectA.getCurrentLocation(), 3, new Vector4f(1f, 1f, 0.5f, 1f), strongestNormalDir);

		for(int i = 0; i < results.length; i++)
		{
			FullSeparationResult fullResult = results[i];

			if(fullResult.success())
				continue;

			SeparationResult bestOption = null;
			double bestDotProduct = -Double.MAX_VALUE;
			for(SeparationResult option : fullResult.options())
			{
				double dot = option.separator().getNormal().dot(strongestNormalDir) * -1d/option.depth();
				if(dot > bestDotProduct)
				{
					bestDotProduct = dot;
					bestOption = option;
				}
			}

			if(bestOption != null)
			{
				VoxelShape shape = Input.StaticShapes.get(i);
				AABB voxelBB = shape.bounds();

				// In a fail case, we have been given our shallowest colliding faces
				// i.e. the ones that would be easiest to push back on
				// The separating axis theorem tells us that this means our cubes collide, BUT not where.

				// Step 1. Identify the incident and reference faces
				Direction referenceSide = bestOption.separator().selectFaceAABBMax(voxelBB);
				Direction incidentSide = bestOption.separator().selectFaceOBBMin(boundsA);

				IPolygon referencePoly = Polygon.of(voxelBB, referenceSide);
				IPolygon incidentPoly = boundsA.GetFace(incidentSide);

				IPolygon collisionPoly = bestOption.separator().collisionClip(incidentPoly, referencePoly);

				collisions.add(new StaticCollisionEvent(collisionPoly, bestOption.separator(), bestOption.depth()));
			}
		}


		NewSeparators = newSeparatorList;
		Output = new Output(collisions.build(),
							ImmutableList.copyOf(NewSeparators)); // <- builder?
	}

	private int ApplySeparation(@Nonnull IPlane separator,
								@Nonnull FullSeparationResult[] results)
	{
		int numSeparated = 0;

		for(int i = 0; i < Input.StaticShapes.size(); i++)
		{
			// Don't need to separate a single shape twice
			if(results[i] != null)
				continue;

			VoxelShape shape = Input.StaticShapes.get(i);
			double heightAbove = separator.getAABBHeightAbove(shape.bounds());
			if(heightAbove >= 0.0f) {
				results[i] = FullSeparationResult.of(SeparationResult.successful(separator));
				numSeparated++;
			}
		}

		return numSeparated;
	}

	@Nonnull
	private Pair<Integer, SeparationResult> FindClosestSeparation(TransformedBB boundsA)
	{
		int shortestIntersectionVoxelIndex = -1;
		SeparationResult shortestIntersection = null;
		double shortestIntersectionDist = Double.MAX_VALUE;

		for(int i = 0; i < Input.StaticShapes.size(); i++)
		{
			VoxelShape shape = Input.StaticShapes.get(i);
			AABB aabb = shape.bounds();
			boolean isSeparated = false;

			for(ISeparationAxis separator : Input.ExistingSeparators)
			{
				Pair<Double, IPlane> sepResult = separator.getSeparationPlaneAtoB(boundsA, aabb);
				double sepDistance = sepResult.getFirst();
				if(sepDistance < shortestIntersectionDist)
				{
					shortestIntersection = SeparationResult.successful(sepResult.getSecond());
					shortestIntersectionDist = sepDistance;
					shortestIntersectionVoxelIndex = i;
				}
				if (sepDistance >= 0.0f)
				{
					isSeparated = true;
					break;
				}
			}
			if(NewSeparators != null)
			{
				for (ISeparationAxis separator : NewSeparators)
				{
					Pair<Double, IPlane> sepResult = separator.getSeparationPlaneAtoB(boundsA, aabb);
					double sepDistance = sepResult.getFirst();
					if (sepDistance < shortestIntersectionDist)
					{
						shortestIntersection = SeparationResult.successful(sepResult.getSecond());
						shortestIntersectionDist = sepDistance;
						shortestIntersectionVoxelIndex = i;
					}
					if (sepDistance >= 0.0f)
					{
						isSeparated = true;
						break;
					}
				}
			}

			if(!isSeparated)
			{
				SeparationResult result = CollisionTasks.separate(boundsA, aabb);
				if(result.depth() < shortestIntersectionDist)
				{
					shortestIntersection = result;
					shortestIntersectionDist = result.depth();
					shortestIntersectionVoxelIndex = i;
				}
				if(NewSeparators == null)
					NewSeparators = new ArrayList<>();
				NewSeparators.add(result.separator());
			}
		}
		return Pair.of(shortestIntersectionVoxelIndex, shortestIntersection);
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}
	@Override
	public void cancel()
	{
		Cancelled = true;
	}
	@Override
	public boolean isComplete()
	{
		return Cancelled || Output != null;
	}
	@Nullable
	@Override
	public Output getResult()
	{
		return Output;
	}
	@Nullable
	public Input Debug_GetInput()
	{
		return Input;
	}

}
