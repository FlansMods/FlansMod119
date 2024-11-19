package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.baked.BakedAttachPoint;
import com.flansmod.physics.common.util.Transform;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record AttachPoint(@Nullable String parent, @Nonnull Vector3f offset, @Nonnull Vector3f euler)
{
	public static final AttachPoint Invalid = new AttachPoint(null, new Vector3f(), new Vector3f());
	public static final AttachPoint Core = new AttachPoint(null, new Vector3f(), new Vector3f());

	@Nonnull
	public BakedAttachPoint bake()
	{
		return new BakedAttachPoint(parent, Transform.fromPosAndEuler(offset.mul(1f/16f, new Vector3f()), euler));
	}
}
