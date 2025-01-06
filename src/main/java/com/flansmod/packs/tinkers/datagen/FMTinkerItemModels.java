package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.common.registration.CastItemObject;

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
	}

	public void castModels(@Nonnull CastItemObject cast)
	{
		ResourceLocation idGold = ForgeRegistries.ITEMS.getKey(cast.get());
		ResourceLocation textureLocationGold = new ResourceLocation(idGold.getNamespace(), "item/" + idGold.getPath());
		singleTexture(idGold.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationGold);
		ResourceLocation idSand = ForgeRegistries.ITEMS.getKey(cast.getSand());
		ResourceLocation textureLocationSand = new ResourceLocation(idSand.getNamespace(), "item/" + idSand.getPath());
		singleTexture(idSand.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationSand);
		ResourceLocation idSandRed = ForgeRegistries.ITEMS.getKey(cast.getRedSand());
		ResourceLocation textureLocationSandRed = new ResourceLocation(idSandRed.getNamespace(), "item/" + idSandRed.getPath());
		singleTexture(idSandRed.getPath(), new ResourceLocation("item/generated"), "layer0", textureLocationSandRed);
	}
}
