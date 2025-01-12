package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.LanguageProvider;
import slimeknights.tconstruct.common.registration.CastItemObject;

import javax.annotation.Nonnull;

public class FMTinkerLang extends LanguageProvider
{
	public FMTinkerLang(@Nonnull PackOutput output)
	{
		super(output, FlansTinkersMod.MODID, "en_us");
	}

	@Override
	protected void addTranslations()
	{
		addCast(FMTinkerResources.UPPER_RECEIVER_CAST, "Upper Receiver");
		addCast(FMTinkerResources.LOWER_RECEIVER_CAST, "Lower Receiver");
		addCast(FMTinkerResources.GRIP_CAST, "Grip");
		addCast(FMTinkerResources.STOCK_CAST, "Stock");
		addCast(FMTinkerResources.BARREL_CAST, "Barrel");

		addItem(FMTinkerResources.UPPER_RECEIVER, "Upper Receiver");
		add("pattern.flanstinkers.upper_receiver", "Upper Receiver");
		addItem(FMTinkerResources.LOWER_RECEIVER, "Lower Receiver");
		add("pattern.flanstinkers.lower_receiver", "Lower Receiver");
		addItem(FMTinkerResources.GRIP, "Grip");
		add("pattern.flanstinkers.grip", "Grip");
		addItem(FMTinkerResources.STOCK, "Stock");
		add("pattern.flanstinkers.stock", "Stock");
		addItem(FMTinkerResources.BARREL, "Barrel");
		add("pattern.flanstinkers.barrel", "Barrel");


		addItem(FMTinkerResources.RIFLE, "Rifle");
		add("item.flanstinkers.rifle.description", "A medium power firearm");

		add("tool_stat.flansmod.rate_of_fire", "Rate of Fire: ");
		add("tool_stat.flansmod.impact_damage", "Impact Damage: ");
		add("tool_stat.flansmod.bullet_spread", "Bullet Spread: ");

	}

	public void addCast(@Nonnull CastItemObject cast, @Nonnull String name)
	{
		add(cast.get().getDescriptionId(), name + " Gold Cast");
		add(cast.getSand().getDescriptionId(), name + " Sand Cast");
		add(cast.getRedSand().getDescriptionId(), name + " Red Sand Cast");
	}
}
