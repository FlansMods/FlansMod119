package com.flansmod.client.render.models.baked;

import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public record BakedTurboGeometry(@Nonnull List<Vertex> vertices, @Nonnull List<Polygon> polygons)
		implements BakedModel
{
	public static final BakedTurboGeometry invalid = new BakedTurboGeometry(ImmutableList.of(), ImmutableList.of());

	public record Vertex(@Nonnull Vector3f position)
	{
	}
	public record VertexRef(int vIndex, @Nonnull Vector2f uv)
	{
	}
	public record Polygon(@Nonnull List<VertexRef> vertexOrder, @Nonnull Vector3f normal)
	{
	}

	@Override
	public boolean useAmbientOcclusion() { return false; }
	@Override
	public boolean isGui3d() { return false; }
	@Override
	public boolean usesBlockLight() { return false; }
	@Override
	public boolean isCustomRenderer() { return false; }
	@Override
	public TextureAtlasSprite getParticleIcon() { return null; }
	@Override
	public ItemOverrides getOverrides() { return null; }


	@Override @Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		return Collections.emptyList();
	}

	public void render(@Nonnull VertexConsumer vc,
					   @Nonnull TransformStack transformStack,
					   int light,
					   int overlay,
					   float scale)
	{
		Transform topPose = transformStack.top();
		for(BakedTurboGeometry.Polygon polygon : polygons)
		{
			Vector3f normal = polygon.normal();
			Vec3 nPosed = topPose.localToGlobalDirection(new Vec3(normal.x, normal.y, normal.z));

			for(BakedTurboGeometry.VertexRef vertexRef : polygon.vertexOrder())
			{
				BakedTurboGeometry.Vertex v = vertices.get(vertexRef.vIndex());

				Vec3 vPosed = topPose.localToGlobalPosition(new Vec3(
						v.position().x * scale,
						v.position().y * scale,
						v.position().z * scale));

				vc.vertex(
						(float)vPosed.x,
						(float)vPosed.y,
						(float)vPosed.z,
						1.0f, 1.0f, 1.0f, 1.0f,
						vertexRef.uv().x,
						vertexRef.uv().y,
						overlay, // overlayCoords
						light, // uv2
						(float)nPosed.x,
						(float)nPosed.y,
						(float)nPosed.z);
			}
		}
	}
}
