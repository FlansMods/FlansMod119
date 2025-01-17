package com.flansmod.client.render;

import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderContext
{
	@Nonnull
	public final MultiBufferSource Buffers;
	@Nullable
	public final ItemDisplayContext TransformType;
	@Nonnull
	public final TransformStack Transforms;
	public final int Light;
	public final int Overlay;

	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemDisplayContext transformType,
						 @Nonnull TransformStack transforms,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = transforms;
		Light = light;
		Overlay = overlay;
	}
	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemDisplayContext transformType,
						 @Nonnull PoseStack poses,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = TransformStack.of(poses);
		Light = light;
		Overlay = overlay;
	}
	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemDisplayContext transformType,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = TransformStack.empty();
		Light = light;
		Overlay = overlay;
	}
}
