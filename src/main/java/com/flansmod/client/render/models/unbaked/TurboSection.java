package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.ETurboRenderMaterial;
import com.flansmod.client.render.models.ITurboDeserializer;
import com.flansmod.client.render.models.baked.BakedTurboGeometry;
import com.flansmod.client.render.models.baked.BakedTurboSection;
import com.google.common.collect.*;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class TurboSection
{
	private static final Logger LOGGER = LogUtils.getLogger();

	private final List<TurboElement> elements;
	private final BlockModel.GuiLight guiLight;
	public final boolean hasAmbientOcclusion;
	private final ItemTransforms transforms;
	private final List<ItemOverride> overrides;
	public final Vector3f offset;
	//public final Map<String, Either<Material, String>> textureMap;
	public final Map<String, ResourceLocation> textures;
	public final ETurboRenderMaterial material;

	@Nullable
	public TurboSection parent;
	@Nullable
	protected ResourceLocation parentLocation;

	public TurboSection(@Nullable ResourceLocation parentLocation,
						List<TurboElement> elements,
						//Map<String, Either<Material, String>> textureMap,
						Map<String, ResourceLocation> textures,
						boolean hasAmbientOcclusion,
						@Nullable BlockModel.GuiLight guiLight,
						ItemTransforms transforms,
						List<ItemOverride> overrides,
						Vector3f offset,
						ETurboRenderMaterial material)
	{
		this.elements = elements;
		this.hasAmbientOcclusion = hasAmbientOcclusion;
		this.guiLight = guiLight;
		//this.textureMap = textureMap;
		this.textures = textures;
		this.parentLocation = parentLocation;
		this.transforms = transforms;
		this.overrides = overrides;
		this.offset = offset;
		this.material = material;
	}

	@Nonnull
	public BakedTurboSection bake(@Nullable IGeometryBakingContext context,
								  @Nonnull ModelBaker baker,
								  @Nonnull Function<Material, TextureAtlasSprite> spriteGetter,
								  @Nonnull ModelState modelState,
								  @Nullable ItemOverrides overrides,
								  @Nonnull ResourceLocation modelLocation,
								  @Nonnull Vector2i textureSize)
	{
		ImmutableList.Builder<BakedTurboGeometry> bakedGeometry = new ImmutableList.Builder<>();
		for(TurboElement element : elements)
		{
			BakedTurboGeometry bakedGeom = element.bake(textureSize);
			bakedGeometry.add(bakedGeom);
		}
		return new BakedTurboSection(bakedGeometry.build(), overrides, transforms, material);
	}

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<TurboSection>, ITurboDeserializer
	{
		private static final boolean DEFAULT_AMBIENT_OCCLUSION = true;

		public TurboSection deserialize(@Nonnull JsonElement jElement,
										@Nonnull Type p_111499_,
										@Nonnull JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();
			List<TurboElement> elementList = getElements(context, jObject);
			String parentName = getParentName(jObject);
			Map<String, ResourceLocation> map = this.getTextureMap(jObject);
			boolean flag = this.getAmbientOcclusion(jObject);

			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jObject.has("display"))
			{
				JsonObject jDisplayObject = GsonHelper.getAsJsonObject(jObject, "display");
				itemTransforms = context.deserialize(jDisplayObject, ItemTransforms.class);
			}

			List<ItemOverride> list1 = this.getOverrides(context, jObject);
			BlockModel.GuiLight blockmodel$guilight = null;
			if (jObject.has("gui_light")) {
				blockmodel$guilight = BlockModel.GuiLight.getByName(GsonHelper.getAsString(jObject, "gui_light"));
			}

			Vector3f offset = new Vector3f();
			if(jObject.has("origin"))
			{
				offset = getVector3f(jObject.get("origin"));
			}

			ETurboRenderMaterial material = ETurboRenderMaterial.Solid;
			if(jObject.has("material"))
			{
				material = ETurboRenderMaterial.valueOf(jObject.get("material").getAsString());
			}

			ResourceLocation parentLocation = parentName.isEmpty() ? null : new ResourceLocation(parentName);
			return new TurboSection(
				parentLocation,
				elementList,
				map,
				flag,
				blockmodel$guilight,
				itemTransforms,
				list1,
				offset,
				material);
		}

		protected List<ItemOverride> getOverrides(JsonDeserializationContext p_111495_, JsonObject p_111496_) {
			List<ItemOverride> list = Lists.newArrayList();
			if (p_111496_.has("overrides")) {
				for(JsonElement jsonelement : GsonHelper.getAsJsonArray(p_111496_, "overrides")) {
					list.add(p_111495_.deserialize(jsonelement, ItemOverride.class));
				}
			}

			return list;
		}
		@Nonnull
		private Map<String, ResourceLocation> getTextureMap(@Nonnull JsonObject jObject)
		{
			Map<String, ResourceLocation> map = Maps.newHashMap();
			if (jObject.has("textures"))
			{
				JsonObject jTextureObject = GsonHelper.getAsJsonObject(jObject, "textures");
				for(var kvp : jTextureObject.entrySet())
				{
					map.put(kvp.getKey(), parseTextureLocationOrReference(kvp.getValue().getAsString()));
				}
			}

			return map;
		}

		@Nullable
		private static ResourceLocation parseTextureLocationOrReference(@Nonnull String textureName)
		{
			return ResourceLocation.tryParse(textureName);
		}
		@Nonnull
		private String getParentName(@Nonnull JsonObject jObject)
		{
			return GsonHelper.getAsString(jObject, "parent", "");
		}

		protected boolean getAmbientOcclusion(@Nonnull JsonObject jObject)
		{
			return GsonHelper.getAsBoolean(jObject, "ambientocclusion", true);
		}
		@Nonnull
		protected List<TurboElement> getElements(@Nonnull JsonDeserializationContext context,
												 @Nonnull JsonObject jObject)
		{
			List<TurboElement> list = Lists.newArrayList();
			if (jObject.has("turboelements"))
			{
				for(JsonElement jElement : GsonHelper.getAsJsonArray(jObject, "turboelements"))
				{
					list.add(context.deserialize(jElement, TurboElement.class));
				}
			}

			return list;
		}
	}
}
