package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FlansTinkersMod;
import com.flansmod.packs.tinkers.GunPartMaterialStats;
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
			.addPart("barrel", GunPartMaterialStats.ID)
			.addPart("stock", GunPartMaterialStats.ID)
			.addPart("upper_receiver", GunPartMaterialStats.ID)
			.addPart("lower_receiver", GunPartMaterialStats.ID);


	}
}
