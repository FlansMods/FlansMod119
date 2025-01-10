package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.tinkering.AbstractStationSlotLayoutProvider;

import javax.annotation.Nonnull;

public class FMTinkerToolSlotLayouts extends AbstractStationSlotLayoutProvider
{
	public FMTinkerToolSlotLayouts(@Nonnull  PackOutput packOutput)
	{
		super(packOutput);
	}
	@Override @Nonnull
	public String getName() { return "Flan's Mod: Reloaded x Tinker's Construct Station Slot Layouts"; }
	@Override
	protected void addLayouts()
	{
		// Rifle
		defineModifiable(FMTinkerResources.RIFLE)
			.sortIndex(SORT_RANGED)
			.addInputItem(FMTinkerResources.UPPER_RECEIVER, 27, 65)
			.addInputItem(FMTinkerResources.LOWER_RECEIVER, 27, 25)
			.addInputItem(FMTinkerResources.STOCK, 44, 45)
			.addInputItem(FMTinkerResources.BARREL, 10, 45)
			.build();
	}

}
