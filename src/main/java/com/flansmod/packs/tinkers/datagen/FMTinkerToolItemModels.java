package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.library.data.AbstractToolItemModelProvider;

import javax.annotation.Nonnull;
import java.io.IOException;

public class FMTinkerToolItemModels extends AbstractToolItemModelProvider
{
	public FMTinkerToolItemModels(@Nonnull PackOutput packOutput, @Nonnull ExistingFileHelper existingFileHelper)
	{
		super(packOutput, existingFileHelper, FlansTinkersMod.MODID);
	}

	@Override @Nonnull
	public String getName() { return "Flan's Mod: Reloaded x Tinker's Construct Tool Item Models"; }
	@Override
	protected void addModels() throws IOException
	{
		JsonObject toolBlocking = readJson(new ResourceLocation(FlansTinkersMod.MODID, "base/gun_blocking"));
		tool(FMTinkerResources.RIFLE, toolBlocking);
	}
}
