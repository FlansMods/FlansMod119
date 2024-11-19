package com.flansmod.client.render.models.baked;

import com.flansmod.client.render.models.ETurboRenderMaterial;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public record BakedTurboSection(@Nonnull List<BakedTurboGeometry> geometries,
								@Nonnull ItemOverrides overrides,
								@Nonnull ItemTransforms transforms,
								@Nonnull ETurboRenderMaterial material) implements BakedModel
{
	public static final BakedTurboSection invalid = new BakedTurboSection(ImmutableList.of(), ItemOverrides.EMPTY, ItemTransforms.NO_TRANSFORMS, ETurboRenderMaterial.Cutout);

	@Override
	public boolean useAmbientOcclusion() { return false; }
	@Override
	public boolean isGui3d() { return true; }
	@Override
	public boolean usesBlockLight() { return false; }
	@Override
	public boolean isCustomRenderer() { return true; }
	@Override @Nonnull
	public TextureAtlasSprite getParticleIcon() { return UnitTextureAtlasSprite.INSTANCE; }
	@Override @Nonnull
	public ItemOverrides getOverrides() { return overrides; }


	@Override @Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource)
	{
		return Collections.emptyList();
	}

	public void render(@Nonnull VertexConsumer vc,
					   @Nonnull TransformStack transformStack,
					   int light,
					   int overlay,
					   float scale)
	{
		for(BakedTurboGeometry geometry : geometries)
		{
			geometry.render(vc, transformStack, light, overlay, scale);
		}
	}
}
