package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.data.model.MaterialModelBuilder;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.tools.part.MaterialItem;

import javax.annotation.Nonnull;


public class FMTinkerItemModels extends ItemModelProvider
{
	public FMTinkerItemModels(@Nonnull PackOutput output, @Nonnull ExistingFileHelper existingFileHelper)
	{
		super(output, FlansTinkersMod.MODID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		castModels(FMTinkerResources.UPPER_RECEIVER_CAST);
		castModels(FMTinkerResources.LOWER_RECEIVER_CAST);
		castModels(FMTinkerResources.GRIP_CAST);
		castModels(FMTinkerResources.STOCK_CAST);
		castModels(FMTinkerResources.BARREL_CAST);


		part(FMTinkerResources.UPPER_RECEIVER);
		part(FMTinkerResources.LOWER_RECEIVER);
		part(FMTinkerResources.GRIP);
		part(FMTinkerResources.STOCK);
		part(FMTinkerResources.BARREL);
	}

	/* Parts */
	private ResourceLocation id(ItemLike item) {
		return BuiltInRegistries.ITEM.getKey(item.asItem());
	}

	/** Creates a part model with the given texture */
	private MaterialModelBuilder<ItemModelBuilder> part(ResourceLocation part, String texture) {
		return withExistingParent(part.getPath(), "forge:item/default")
			.texture("texture", new ResourceLocation(FlansTinkersMod.MODID, "item/tool/" + texture))
			.customLoader(MaterialModelBuilder::new);
	}

	/** Creates a part model in the parts folder */
	private MaterialModelBuilder<ItemModelBuilder> part(Item item, String texture) {
		return part(id(item), texture);
	}

	/** Creates a part model with the given texture */
	private MaterialModelBuilder<ItemModelBuilder> part(ItemObject<? extends MaterialItem> part, String texture) {
		return part(part.getId(), texture);
	}

	/** Creates a part model in the parts folder */
	private void part(ItemObject<? extends MaterialItem> part) {
		part(part, "parts/" + part.getId().getPath());
	}


	public void castModels(@Nonnull CastItemObject cast)
	{
		ResourceLocation idGold = ForgeRegistries.ITEMS.getKey(cast.get());
		if(idGold != null)
		{
			ResourceLocation textureLocationGold = new ResourceLocation(idGold.getNamespace(), "item/" + idGold.getPath());
			singleTexture(idGold.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationGold);
		}
		ResourceLocation idSand = ForgeRegistries.ITEMS.getKey(cast.getSand());
		if(idSand != null)
		{
			ResourceLocation textureLocationSand = new ResourceLocation(idSand.getNamespace(), "item/" + idSand.getPath());
			singleTexture(idSand.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationSand);
		}
		ResourceLocation idSandRed = ForgeRegistries.ITEMS.getKey(cast.getRedSand());
		if(idSandRed != null)
		{
			ResourceLocation textureLocationSandRed = new ResourceLocation(idSandRed.getNamespace(), "item/" + idSandRed.getPath());
			singleTexture(idSandRed.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationSandRed);
		}
	}
}
