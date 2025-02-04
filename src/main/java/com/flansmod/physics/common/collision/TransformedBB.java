package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.deprecated.ContinuousCollisionUtility;
import com.flansmod.physics.common.deprecated.ContinuousSeparationManifold;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.shapes.VertexIndex;
import com.flansmod.physics.common.util.shapes.IPolygon;
import com.flansmod.physics.common.util.shapes.Polygon;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public record TransformedBB(@Nonnull Transform Loc, @Nonnull Vector3f HalfExtents)
{
	@Nonnull
	public static TransformedBB EntityInSpace(@Nonnull AABB entityBounds,
											  @Nonnull Vec3 entityPos,
											  @Nonnull Transform space)
	{
		return new TransformedBB(space.globalToLocalTransform(Transform.fromPos(entityPos)),
			new Vector3f((float)entityBounds.getXsize()/2f, (float)entityBounds.getYsize()/2f, (float)entityBounds.getZsize()/2f));
	}
	@Nonnull
	public static TransformedBB Of(@Nonnull Transform location, @Nonnull AABB aabb)
	{
		Transform centerLoc = Transform.compose(
			location,
			Transform.fromPos(aabb.getCenter()));
		return new TransformedBB(centerLoc,
			new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f));
	}
	@Nonnull
	public static TransformedBB Of(@Nonnull AABB aabb)
	{
		return new TransformedBB(Transform.fromPos(aabb.getCenter()), new Vector3f((float)aabb.getXsize()/2f, (float)aabb.getYsize()/2f, (float)aabb.getZsize()/2f));
	}



	@Nullable
	public ContinuousSeparationManifold Intersect(@Nonnull AABB bb, @Nonnull Vec3 motion) {
		return ContinuousCollisionUtility.SeparateContinuous(this, motion, bb);
	}
	public boolean Contains(@Nonnull Vec3 point)
	{
		Vec3 local = Loc.globalToLocalPosition(point);
		return Maths.abs(local.x) <= HalfExtents.x
			&& Maths.abs(local.y) <= HalfExtents.y
			&& Maths.abs(local.z) <= HalfExtents.z;
	}
	public boolean ApproxContains(@Nonnull Vec3 point) { return ApproxContains(point, 0.01d); }
	public boolean ApproxContains(@Nonnull Vec3 point, double epsilon)
	{
		Vec3 local = Loc.globalToLocalPosition(point);
		return Maths.abs(local.x) <= HalfExtents.x + epsilon
			&& Maths.abs(local.y) <= HalfExtents.y + epsilon
			&& Maths.abs(local.z) <= HalfExtents.z + epsilon;
	}

	public double XSize() { return HalfExtents.x * 2d; }
	public double YSize() { return HalfExtents.y * 2d; }
	public double ZSize() { return HalfExtents.z * 2d; }

	public double GetMaxRadiusBound() {
		return Maths.max(HalfExtents.x, HalfExtents.y, HalfExtents.z) * Maths.Root2;
	}

	@Nonnull
	public TransformedBB Move(@Nonnull Function<Transform, Transform> func) { return new TransformedBB(func.apply(Loc), HalfExtents); }
	//@Nonnull
	//public CollisionManifold GetSide(@Nonnull Direction dir)
	//{
	//	Vec3 center = Loc.PositionVec3();
	//	Vec3 dirVec = Loc.DirectionVec3(dir);
	//	return switch(dir)
	//	{
	//		case UP, DOWN -> new CollisionManifold(dirVec, dirVec.dot(center) + HalfExtents.y);
	//		case NORTH, SOUTH -> new CollisionManifold(dirVec, dirVec.dot(center) + HalfExtents.z);
	//		case EAST, WEST -> new CollisionManifold(dirVec, dirVec.dot(center) + HalfExtents.x);
	//	};
	//}
	@Nonnull
	public Vec3 GetCenter() { return Loc.positionVec3(); }
	@Nonnull
	public Vec3 GetCorner(@Nonnull VertexIndex corner) {
		return switch(corner)
		{
			case NegX_NegY_NegZ -> Loc.localToGlobalPosition(new Vec3(-HalfExtents.x, -HalfExtents.y, -HalfExtents.z));
			case NegX_NegY_PosZ -> Loc.localToGlobalPosition(new Vec3(-HalfExtents.x, -HalfExtents.y, HalfExtents.z));
			case NegX_PosY_NegZ -> Loc.localToGlobalPosition(new Vec3(-HalfExtents.x, HalfExtents.y, -HalfExtents.z));
			case NegX_PosY_PosZ -> Loc.localToGlobalPosition(new Vec3(-HalfExtents.x, HalfExtents.y, HalfExtents.z));
			case PosX_NegY_NegZ -> Loc.localToGlobalPosition(new Vec3(HalfExtents.x, -HalfExtents.y, -HalfExtents.z));
			case PosX_NegY_PosZ -> Loc.localToGlobalPosition(new Vec3(HalfExtents.x, -HalfExtents.y, HalfExtents.z));
			case PosX_PosY_NegZ -> Loc.localToGlobalPosition(new Vec3(HalfExtents.x, HalfExtents.y, -HalfExtents.z));
			case PosX_PosY_PosZ -> Loc.localToGlobalPosition(new Vec3(HalfExtents.x, HalfExtents.y, HalfExtents.z));
		};
	}
	@Nonnull
	public Vec3 GetAxis(@Nonnull Direction dir)
	{
		return Loc.directionVec(dir);
	}
	@Nonnull
	public IPolygon GetFace(@Nonnull Direction dir)
	{
		return switch (dir)
		{
			case UP -> Polygon.square(GetCorner(VertexIndex.UP[0]), GetCorner(VertexIndex.UP[1]), GetCorner(VertexIndex.UP[2]), GetCorner(VertexIndex.UP[3]));
			case DOWN -> Polygon.square(GetCorner(VertexIndex.DOWN[0]), GetCorner(VertexIndex.DOWN[1]), GetCorner(VertexIndex.DOWN[2]), GetCorner(VertexIndex.DOWN[3]));
			case NORTH -> Polygon.square(GetCorner(VertexIndex.NORTH[0]), GetCorner(VertexIndex.NORTH[1]), GetCorner(VertexIndex.NORTH[2]), GetCorner(VertexIndex.NORTH[3]));
			case EAST -> Polygon.square(GetCorner(VertexIndex.EAST[0]), GetCorner(VertexIndex.EAST[1]), GetCorner(VertexIndex.EAST[2]), GetCorner(VertexIndex.EAST[3]));
			case SOUTH -> Polygon.square(GetCorner(VertexIndex.SOUTH[0]), GetCorner(VertexIndex.SOUTH[1]), GetCorner(VertexIndex.SOUTH[2]), GetCorner(VertexIndex.SOUTH[3]));
			case WEST -> Polygon.square(GetCorner(VertexIndex.WEST[0]), GetCorner(VertexIndex.WEST[1]), GetCorner(VertexIndex.WEST[2]), GetCorner(VertexIndex.WEST[3]));
		};
	}

}
