package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FlansTinkersMod;
import com.flansmod.packs.tinkers.materialstats.GunHandleMaterialStats;
import com.flansmod.packs.tinkers.materialstats.GunInternalMaterialStats;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.client.data.material.AbstractPartSpriteProvider;

import javax.annotation.Nonnull;

public class FMTinkerPartTextures extends AbstractPartSpriteProvider
{
	public FMTinkerPartTextures()
	{
		super(FlansTinkersMod.MODID);
	}

	@Override @Nonnull
	public String getName() { return "Flan's Mod: Reloaded x Tinker's Construct Part Textures"; }

	@Override
	protected void addAllSpites()
	{
		buildTool("rifle")
			.addPart("barrel", GunInternalMaterialStats.ID)
			.addPart("stock", GunHandleMaterialStats.ID)
			.addPart("grip", GunHandleMaterialStats.ID)
			.addPart("upper_receiver", GunInternalMaterialStats.ID)
			.addPart("lower_receiver", GunInternalMaterialStats.ID);

		addPart("upper_receiver", GunInternalMaterialStats.ID);
		addPart("lower_receiver", GunInternalMaterialStats.ID);
		addPart("barrel", GunInternalMaterialStats.ID);
		addPart("stock", GunHandleMaterialStats.ID);
		addPart("grip", GunHandleMaterialStats.ID);

		addTexture(new ResourceLocation(FlansTinkersMod.MODID, "skins/rifle"), GunHandleMaterialStats.ID, GunInternalMaterialStats.ID);
	}
}
