package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.ITurboDeserializer;
import com.flansmod.client.render.models.baked.BakedTurboGeometry;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public abstract class TurboElement
{
	public static final TurboElement invalid = new TurboElement(new Vector3f(), new Vector3f(), false) {
		@Nonnull @Override
		public BakedTurboGeometry bake(@Nonnull Vector2i textureSize) { return BakedTurboGeometry.invalid; }
	};
	public final boolean shade;
	public final Vector3f eulerRotations;
	public final Vector3f rotationOrigin;

	public TurboElement(@Nonnull Vector3f eulerRotations,
						@Nonnull Vector3f rotationOrigin,
						boolean shade)
	{
		this.eulerRotations = eulerRotations;
		this.rotationOrigin = rotationOrigin;
		this.shade = shade;
	}

	@Nonnull
	public abstract BakedTurboGeometry bake(@Nonnull Vector2i textureSize);

	public Vector3f GetNormal(Direction direction, boolean applyRotation)
	{
		if(applyRotation)
		{
			Quaternionf rotation = new Quaternionf().rotateZYX(eulerRotations.z, eulerRotations.y, eulerRotations.x);
			return rotation.transform(new Vector3f(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ()));
		}
		return new Vector3f(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ());
	}

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<TurboElement>, ITurboDeserializer
	{
		private static final boolean DEFAULT_SHADE = true;

		@Nonnull
		public TurboElement deserialize(@Nonnull JsonElement jElement,
										@Nonnull Type p_111330_,
										@Nonnull JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();

			Vector3f eulerRotations = getOrDefaultVector3f(jObject, "eulerRotations", new Vector3f());
			Vector3f rotationOrigin = getOrDefaultVector3f(jObject, "rotationOrigin", new Vector3f());
			if (jObject.has("shade") && !GsonHelper.isBooleanValue(jObject, "shade"))
				throw new JsonParseException("Expected shade to be a Boolean");
			boolean shade = GsonHelper.getAsBoolean(jObject, "shade", true);

			if(jObject.get("verts") instanceof JsonArray jVertArray)
			{
				Vector3f[] vertices = new Vector3f[8];
				if(jVertArray.size() == 8)
				{
					for(int i = 0; i < 8; i++)
					{
						vertices[i] = getVector3f(jVertArray.get(i));
					}
				}
				Map<Direction, TurboFace> faceMap = Maps.newEnumMap(Direction.class);
				getFaces(context, jObject, faceMap);
				return new TurboElementRaw(eulerRotations, rotationOrigin, shade, vertices, faceMap);
			}

			if(jObject.get("box") instanceof JsonObject jBox)
			{
				Vector3f origin = getOrDefaultVector3f(jBox, "origin", new Vector3f());
				Vector3f dimensions = getOrDefaultVector3f(jBox, "dimensions", new Vector3f());
				Vector2f uvCoords = getOrDefaultVector2f(jBox, "uv", new Vector2f());
				return new TurboBox(eulerRotations, rotationOrigin, shade, origin, dimensions, uvCoords);
			}

			if(jObject.get("shapebox") instanceof JsonObject jShapeBox)
			{
				Vector3f origin = getOrDefaultVector3f(jShapeBox, "origin", new Vector3f());
				Vector3f dimensions = getOrDefaultVector3f(jShapeBox, "dimensions", new Vector3f());
				Vector2f uvCoords = getOrDefaultVector2f(jShapeBox, "uv", new Vector2f());
				Vector3f[] offsets = new Vector3f[8];
				if(jShapeBox.get("offsets") instanceof JsonArray jOffsetArray)
				{
					if (jOffsetArray.size() == 8)
					{
						for (int i = 0; i < 8; i++)
						{
							offsets[i] = getVector3f(jOffsetArray.get(i));
						}
					}
				}
				return new TurboShapeBox(eulerRotations, rotationOrigin, shade, origin, dimensions, uvCoords, offsets);
			}

			return invalid;
		}

		private void getFaces(@Nonnull JsonDeserializationContext context,
							  @Nonnull JsonObject jObject,
							  @Nonnull Map<Direction, TurboFace> faceMap)
		{
			filterNullFromFaces(context, jObject, faceMap);
			if (faceMap.isEmpty())
				throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
		}

		private void filterNullFromFaces(@Nonnull JsonDeserializationContext context,
									 	@Nonnull JsonObject jObject,
									 	@Nonnull Map<Direction, TurboFace> faceMap)
		{
			JsonObject jsonobject = GsonHelper.getAsJsonObject(jObject, "faces");
			for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet())
			{
				Direction direction = this.getFacing(entry.getKey());
				faceMap.put(direction, context.deserialize(entry.getValue(), TurboFace.class));
			}
		}

	}

}
