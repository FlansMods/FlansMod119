package com.flansmod.client.render.models;

import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public interface ITurboDeserializer
{
	@Nonnull
	default Direction getFacing(@Nonnull String str)
	{
		Direction direction = Direction.byName(str);
		if (direction == null) {
			throw new JsonParseException("Unknown facing: " + str);
		} else {
			return direction;
		}
	}


	@Nonnull
	default Vector3f getVector3f(@Nonnull JsonArray jArray)
	{
		if (jArray.size() != 3)
			throw new JsonParseException("Expected 3 values, found: " + jArray.size());

		float[] afloat = new float[3];
		for(int i = 0; i < afloat.length; ++i)
			afloat[i] = GsonHelper.convertToFloat(jArray.get(i), "getVector3f[" + i + "]");

		return new Vector3f(afloat[0], afloat[1], afloat[2]);
	}
	@Nonnull
	default Vector3f getOrDefaultVector3f(@Nonnull JsonArray jArray, @Nonnull Vector3f defaultValue)
	{
		try
		{
			return getVector3f(jArray);
		}
		catch(JsonParseException jsonParseException)
		{
			return defaultValue;
		}
	}
	@Nonnull
	default Vector3f getVector3f(@Nonnull JsonElement jObject)
	{
		return getVector3f(jObject.getAsJsonArray());
	}
	@Nonnull
	default Vector3f getOrDefaultVector3f(@Nonnull JsonElement jObject, @Nonnull Vector3f defaultValue)
	{
		if(jObject.isJsonArray())
			return getOrDefaultVector3f(jObject.getAsJsonArray(), defaultValue);
		else
			return defaultValue;
	}
	@Nonnull
	default Vector3f getVector3f(@Nonnull JsonObject jObject, @Nonnull String key)
	{
		return getVector3f(GsonHelper.getAsJsonArray(jObject, key));
	}
	@Nonnull
	default Vector3f getOrDefaultVector3f(@Nonnull JsonObject jObject, @Nonnull String key, @Nonnull Vector3f defaultValue)
	{
		if(GsonHelper.isArrayNode(jObject, key))
			return getOrDefaultVector3f(GsonHelper.getAsJsonArray(jObject, key), defaultValue);
		else
			return defaultValue;
	}

	@Nonnull
	default Vector2i getVector2i(@Nonnull JsonArray jArray)
	{
		if (jArray.size() != 2)
			throw new JsonParseException("Expected 2 values, found: " + jArray.size());

		int[] aint = new int[2];
		for(int i = 0; i < aint.length; ++i)
			aint[i] = GsonHelper.convertToInt(jArray.get(i), "getVector2i[" + i + "]");

		return new Vector2i(aint[0], aint[1]);
	}
	@Nonnull
	default Vector2f getVector2f(@Nonnull JsonArray jArray)
	{
		if (jArray.size() != 2)
			throw new JsonParseException("Expected 2 values, found: " + jArray.size());

		float[] afloat = new float[2];
		for(int i = 0; i < afloat.length; ++i)
			afloat[i] = GsonHelper.convertToFloat(jArray.get(i), "getVector2f[" + i + "]");

		return new Vector2f(afloat[0], afloat[1]);
	}
	@Nonnull
	default Vector2f getOrDefaultVector2f(@Nonnull JsonArray jArray, @Nonnull Vector2f defaultValue)
	{
		try
		{
			return getVector2f(jArray);
		}
		catch(JsonParseException jsonParseException)
		{
			return defaultValue;
		}
	}
	@Nonnull
	default Vector2f getVector2f(@Nonnull JsonElement jObject)
	{
		return getVector2f(jObject.getAsJsonArray());
	}
	@Nonnull
	default Vector2f getOrDefaultVector2f(@Nonnull JsonElement jObject, @Nonnull Vector2f defaultValue)
	{
		if(jObject.isJsonArray())
			return getOrDefaultVector2f(jObject.getAsJsonArray(), defaultValue);
		else
			return defaultValue;
	}
	@Nonnull
	default Vector2f getVector2f(@Nonnull JsonObject jObject, @Nonnull String key)
	{
		return getVector2f(GsonHelper.getAsJsonArray(jObject, key));
	}
	@Nonnull
	default Vector2f getOrDefaultVector2f(@Nonnull JsonObject jObject, @Nonnull String key, @Nonnull Vector2f defaultValue)
	{
		if(GsonHelper.isArrayNode(jObject, key))
			return getOrDefaultVector2f(GsonHelper.getAsJsonArray(jObject, key), defaultValue);
		else
			return defaultValue;
	}

}
