package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.baked.BakedTurboGeometry;
import com.flansmod.physics.common.util.Maths;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Map;

public class TurboElementRaw extends TurboElement
{
	public final Vector3f[] vertices;
	public final Map<Direction, TurboFace> faces;

	public TurboElementRaw(@Nonnull Vector3f euler,
						   @Nonnull Vector3f rotOrigin,
						   boolean shade,
						   @Nonnull Vector3f[] verts,
						   @Nonnull Map<Direction, TurboFace> faceMap)
	{
		super(euler, rotOrigin, shade);
		vertices = verts;
		faces = faceMap;
	}

	@Nonnull
	public TurboFace getFace(@Nonnull Direction direction) { return faces.get(direction); }

	// Up/Down is +y/-y
	private static final int[] UP_VERTS 	= new int[] { 7,3,2,6 };
	private static final int[] DOWN_VERTS 	= new int[] { 5,4,0,1 };
	// North(-z)/South(+z)
	private static final int[] NORTH_VERTS 	= new int[] { 0,2,3,1 };
	private static final int[] SOUTH_VERTS 	= new int[] { 5,7,6,4 };
	// East(+x)/West(-x)
	private static final int[] EAST_VERTS 	= new int[] { 1,3,7,5 };
	private static final int[] WEST_VERTS 	= new int[] { 4,6,2,0 };

	@Nonnull
	public BakedTurboGeometry.Polygon bakeQuad(@Nonnull Direction dir)
	{
		TurboFace face = faces.get(dir);
		int[] faceVerts = switch (dir) {
			case DOWN -> DOWN_VERTS;
			case UP -> UP_VERTS;
			case NORTH -> NORTH_VERTS;
			case SOUTH -> SOUTH_VERTS;
			case WEST -> WEST_VERTS;
			case EAST -> EAST_VERTS;
		};
		return new BakedTurboGeometry.Polygon(ImmutableList.of(
				new BakedTurboGeometry.VertexRef(faceVerts[0], new Vector2f(face.uvData.getU(0), face.uvData.getV(0))),
				new BakedTurboGeometry.VertexRef(faceVerts[1], new Vector2f(face.uvData.getU(1), face.uvData.getV(1))),
				new BakedTurboGeometry.VertexRef(faceVerts[2], new Vector2f(face.uvData.getU(2), face.uvData.getV(2))),
				new BakedTurboGeometry.VertexRef(faceVerts[3], new Vector2f(face.uvData.getU(3), face.uvData.getV(3)))
		), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
	}

	@Nonnull
	public BakedTurboGeometry bake(@Nonnull Vector2i textureSize)
	{
		ImmutableList.Builder<BakedTurboGeometry.Vertex> vertexBuilder = new ImmutableList.Builder<>();
		ImmutableList.Builder<BakedTurboGeometry.Polygon> polygonBuilder = new ImmutableList.Builder<>();
		Quaternionf rotation = new Quaternionf()
				.rotateY(eulerRotations.y * Maths.DegToRadF)
				.rotateX(eulerRotations.x * Maths.DegToRadF)
				.rotateZ(eulerRotations.z * Maths.DegToRadF);

		for (int z = 0; z < 2; z++)
		{
			for (int y = 0; y < 2; y++)
			{
				for (int x = 0; x < 2; x++)
				{
					int index = x + y * 2 + z * 4;
					Vector3f vertex = new Vector3f(vertices[index]);
					vertex.rotate(rotation);
					vertex.add(rotationOrigin);
					vertexBuilder.add(new BakedTurboGeometry.Vertex(vertex));
				}
			}
		}

		polygonBuilder.add(bakeQuad(Direction.NORTH));
		polygonBuilder.add(bakeQuad(Direction.SOUTH));
		polygonBuilder.add(bakeQuad(Direction.WEST));
		polygonBuilder.add(bakeQuad(Direction.EAST));
		polygonBuilder.add(bakeQuad(Direction.UP));
		polygonBuilder.add(bakeQuad(Direction.DOWN));

		return new BakedTurboGeometry(vertexBuilder.build(), polygonBuilder.build());
	}
}
