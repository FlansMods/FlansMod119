package com.flansmod.client.render.models;

import com.flansmod.client.FlansModClient;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class TurboRenderUtility
{
	public static boolean USE_MODELVIEW_MATRIX_RENDER_MODE = false;
	public static boolean USE_BAKED_TURBO_MODELS = false;

	protected static final RenderStateShard.ShaderStateShard GUN_SOLID_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunSolidShader);
	protected static final RenderStateShard.ShaderStateShard GUN_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunCutoutShader);
	protected static final RenderStateShard.ShaderStateShard GUN_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunEmissiveShader);
	protected static final RenderStateShard.ShaderStateShard GUN_TRANSPARENT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunTransparentShader);

	private static class RenderTypeFlanItem extends RenderType
	{

		protected static RenderType.CompositeState.CompositeStateBuilder BaseState(ResourceLocation texture)
		{
			return RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setCullState(CULL)
				.setOverlayState(OVERLAY)
				.setLightmapState(LIGHTMAP)
				.setDepthTestState(LEQUAL_DEPTH_TEST);
		}

		protected static final Function<ResourceLocation, RenderType> GUN_CUTOUT = Util.memoize((texture) -> {
			RenderType.CompositeState compositeState =
				RenderType.CompositeState.builder()
					.setShaderState(GUN_CUTOUT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
					.setTransparencyState(NO_TRANSPARENCY)
					.setCullState(CULL)
					.setOverlayState(OVERLAY)
					.setLightmapState(LIGHTMAP)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.createCompositeState(true);
			return create("flan_gun_item",
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				256,
				true,
				false,
				compositeState);
		});


		protected static final Function<ResourceLocation, RenderType> GUN_EMISSIVE =
			Util.memoize((texture) ->
				create("flan_gun_emissive",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_EMISSIVE_SHADER)
						.setTransparencyState(ADDITIVE_TRANSPARENCY)
						.setWriteMaskState(COLOR_WRITE)
						.createCompositeState(false)));
		protected static final Function<ResourceLocation, RenderType> GUN_TRANSPARENT =
			Util.memoize((texture) ->
				create("flan_gun_transparent",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_TRANSPARENT_SHADER)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.createCompositeState(true)));
		protected static final Function<ResourceLocation, RenderType> GUN_SOLID =
			Util.memoize((texture) ->
				create("flan_gun_solid",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_SOLID_SHADER)
						.setTransparencyState(NO_TRANSPARENCY)
						.createCompositeState(true)));

		public RenderTypeFlanItem(String name, VertexFormat vf, VertexFormat.Mode vfm, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupFunc, Runnable cleanupFunc)
		{
			super(name, vf, vfm, bufferSize, affectsCrumbling, sortOnUpload, setupFunc, cleanupFunc);
		}
	}
	@Nonnull
	public static RenderType flanItemRenderType(@Nullable ResourceLocation texture, @Nonnull ETurboRenderMaterial material)
	{
		if(texture == null)
			texture = MissingTextureAtlasSprite.getLocation();
		return switch (material)
			{
				case Solid -> RenderType.entitySolid(texture); //RenderTypeFlanItem.GUN_SOLID.apply(texture);
				case Cutout -> RenderType.entityCutout(texture); //RenderTypeFlanItem.GUN_CUTOUT.apply(texture);
				case Emissive -> RenderType.eyes(texture);
				case Transparent -> RenderType.entityTranslucent(texture);
			};
	}
}
