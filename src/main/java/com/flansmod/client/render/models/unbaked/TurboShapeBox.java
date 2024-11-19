package com.flansmod.client.render.models.unbaked;

import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class TurboShapeBox extends TurboBox
{
	public final Vector3f[] offsets;

	public TurboShapeBox(@Nonnull Vector3f euler,
						 @Nonnull Vector3f rotationOrig,
						 boolean shade,
						 @Nonnull Vector3f orig,
						 @Nonnull Vector3f dims,
						 @Nonnull Vector2f uv,
						 @Nonnull Vector3f[] offs)
	{
		super(euler, rotationOrig, shade, orig, dims, uv);
		offsets = offs;
	}

	@Nonnull @Override
	public Vector3f getVertex(int vIndex)
	{
		return new Vector3f(
				(((vIndex & 0x1) == 0) ? origin.x : (origin.x + dimensions.x)) + offsets[vIndex].x,
				(((vIndex & 0x2) == 0) ? origin.y : (origin.y + dimensions.y)) + offsets[vIndex].y,
				(((vIndex & 0x4) == 0) ? origin.z : (origin.z + dimensions.z)) + offsets[vIndex].z);
	}
}
