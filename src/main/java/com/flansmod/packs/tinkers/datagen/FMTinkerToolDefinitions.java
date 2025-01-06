package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.tinkering.AbstractToolDefinitionDataProvider;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.definition.module.build.SetStatsModule;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.DefaultMaterialsModule;
import slimeknights.tconstruct.library.tools.definition.module.material.PartStatsModule;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nonnull;

public class FMTinkerToolDefinitions extends AbstractToolDefinitionDataProvider
{
	public FMTinkerToolDefinitions(@Nonnull PackOutput dataGenerator)
	{
		super(dataGenerator, FlansTinkersMod.MODID);
	}
	@Override @Nonnull
	public String getName() { return "Flan's Mod: Reloaded x Tinker's Construct Tool Defintion Data Generator"; }


	@Override
	protected void addToolDefinitions()
	{
		RandomMaterial tier1Material = RandomMaterial.random().tier(1).build();
		DefaultMaterialsModule defaultFourParts = DefaultMaterialsModule.builder().material(tier1Material, tier1Material, tier1Material, tier1Material).build();

		// Rifle
		define(FMTinkerResources.RIFLE_DEFINITION)
			.module(PartStatsModule.parts()
				.part(FMTinkerResources.UPPER_RECEIVER)
				.part(FMTinkerResources.LOWER_RECEIVER)
				.part(FMTinkerResources.STOCK)
				.part(FMTinkerResources.BARREL).build())
			.module(defaultFourParts)
			.module(new SetStatsModule(StatsNBT.builder()
				.set(ToolStats.ATTACK_DAMAGE, 1.0f)
				.set(ToolStats.ATTACK_SPEED, 1.0f)
				.build()))
			//.module(ToolTraitsModule.builder().trait(ModifierIds.)
			.largeToolStartingSlots();


	}

}
