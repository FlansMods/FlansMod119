package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.ITurboDeserializer;
import com.flansmod.client.render.models.baked.BakedTurboGeometry;
import com.flansmod.physics.common.util.Maths;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class TurboBox extends TurboElement
{
	public final Vector3f origin;
	public final Vector3f dimensions;
	public final Vector2f uvOrigin;

	public TurboBox(@Nonnull Vector3f euler,
					@Nonnull Vector3f rotationOrig,
					boolean shade,
					@Nonnull Vector3f orig,
					@Nonnull Vector3f dims,
					@Nonnull Vector2f uv)
	{
		super(euler, rotationOrig, shade);
		origin = orig;
		dimensions = dims;
		uvOrigin = uv;
	}

	@Nonnull
	public Vector3f getVertex(int vIndex)
	{
		return new Vector3f(
				((vIndex & 0x1) == 0) ? origin.x : (origin.x + dimensions.x),
				((vIndex & 0x2) == 0) ? origin.y : (origin.y + dimensions.y),
				((vIndex & 0x4) == 0) ? origin.z : (origin.z + dimensions.z));
	}
	@Nonnull
	public BakedTurboGeometry.Polygon bakeQuad(@Nonnull Vector2i textureSize, @Nonnull Direction dir)
	{
		int x = Maths.ceil(dimensions.x);
		int y = Maths.ceil(dimensions.y);
		int z = Maths.ceil(dimensions.z);
		float uMin = uvOrigin.x;
		float vMin = uvOrigin.y;
		float tX = 1.0f / textureSize.x;
		float tY = 1.0f / textureSize.y;

		return switch (dir)
		{
			case NORTH -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(0, new Vector2f(tX * (uMin + x * 2 + z * 2), tY * (vMin + y + z))),
					new BakedTurboGeometry.VertexRef(2, new Vector2f(tX * (uMin + x * 2 + z * 2), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(3, new Vector2f(tX * (uMin + x + z * 2), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(1, new Vector2f(tX * (uMin + x + z * 2), tY * (vMin + y + z)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
			case SOUTH -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(5, new Vector2f(tX * (uMin + x + z), tY * (vMin + y + z))),
					new BakedTurboGeometry.VertexRef(7, new Vector2f(tX * (uMin + x + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(6, new Vector2f(tX * (uMin + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(4, new Vector2f(tX * (uMin + z), tY * (vMin + y + z)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
			case WEST -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(4, new Vector2f(tX * (uMin + z), tY * (vMin + y + z))),
					new BakedTurboGeometry.VertexRef(6, new Vector2f(tX * (uMin + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(2, new Vector2f(tX * (uMin), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(0, new Vector2f(tX * (uMin), tY * (vMin + y + z)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
			case EAST -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(1, new Vector2f(tX * (uMin + x + 2 * z), tY * (vMin + y + z))),
					new BakedTurboGeometry.VertexRef(3, new Vector2f(tX * (uMin + x + 2 * z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(7, new Vector2f(tX * (uMin + x + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(5, new Vector2f(tX * (uMin + x + z), tY * (vMin + y + z)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
			case UP -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(7, new Vector2f(tX * (uMin + x + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(3, new Vector2f(tX * (uMin + x + z), tY * (vMin))),
					new BakedTurboGeometry.VertexRef(2, new Vector2f(tX * (uMin + z), tY * (vMin))),
					new BakedTurboGeometry.VertexRef(6, new Vector2f(tX * (uMin + z), tY * (vMin + z)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
			case DOWN -> new BakedTurboGeometry.Polygon(ImmutableList.of(
					new BakedTurboGeometry.VertexRef(5, new Vector2f(tX * (uMin + 2 * x + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(4, new Vector2f(tX * (uMin + x + z), tY * (vMin + z))),
					new BakedTurboGeometry.VertexRef(0, new Vector2f(tX * (uMin + x + z), tY * (vMin))),
					new BakedTurboGeometry.VertexRef(1, new Vector2f(tX * (uMin + 2 * x + z), tY * (vMin)))
			), new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ()));
		};
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
					Vector3f vertex = new Vector3f(getVertex(index));
					vertex.rotate(rotation);
					vertex.add(rotationOrigin);
					vertexBuilder.add(new BakedTurboGeometry.Vertex(vertex));

				}
			}
		}

		polygonBuilder.add(bakeQuad(textureSize, Direction.NORTH));
		polygonBuilder.add(bakeQuad(textureSize, Direction.SOUTH));
		polygonBuilder.add(bakeQuad(textureSize, Direction.WEST));
		polygonBuilder.add(bakeQuad(textureSize, Direction.EAST));
		polygonBuilder.add(bakeQuad(textureSize, Direction.UP));
		polygonBuilder.add(bakeQuad(textureSize, Direction.DOWN));

		return new BakedTurboGeometry(vertexBuilder.build(), polygonBuilder.build());
	}

	public static class Deserializer implements JsonDeserializer<TurboBox>, ITurboDeserializer
	{
		@Nonnull
		public TurboBox deserialize(@Nonnull JsonElement jElement,
									@Nonnull Type type,
									@Nonnull JsonDeserializationContext context)
				throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();

			if(jObject.get("box") instanceof JsonObject jBox)
			{
				Vector3f eulerRotations = getOrDefaultVector3f(jObject, "eulerRotations", new Vector3f());
				Vector3f rotationOrigin = getOrDefaultVector3f(jObject, "rotationOrigin", new Vector3f());
				if (jObject.has("shade") && !GsonHelper.isBooleanValue(jObject, "shade"))
					throw new JsonParseException("Expected shade to be a Boolean");
				boolean shade = GsonHelper.getAsBoolean(jObject, "shade", true);
				Vector3f origin = getOrDefaultVector3f(jBox, "origin", new Vector3f());
				Vector3f dimensions = getOrDefaultVector3f(jBox, "dimensions", new Vector3f());
				Vector2f uvCoords = getOrDefaultVector2f(jBox, "uv", new Vector2f());
				return new TurboBox(eulerRotations, rotationOrigin, shade, origin, dimensions, uvCoords);
			}
			else
				throw new JsonParseException("This is not a TurboBox");
		}
	}
}
