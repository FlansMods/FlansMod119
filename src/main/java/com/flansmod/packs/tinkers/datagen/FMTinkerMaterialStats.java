package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.materialstats.GunHandleMaterialStats;
import com.flansmod.packs.tinkers.materialstats.GunInternalMaterialStats;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;

import javax.annotation.Nonnull;

public class FMTinkerMaterialStats extends MaterialStatsDataProvider
{
	public FMTinkerMaterialStats(PackOutput packOutput, AbstractMaterialDataProvider materials)
	{
		super(packOutput, materials);
	}

	@Override @Nonnull
	public String getName() { return "Flan's Mod: Reloaded x Tinker's Construct Material Stats"; }

	@Override
	protected void addMaterialStats()
	{
		super.addMaterialStats();
		addGunInternals();
		addGunHandles();
	}

	private void addGunInternals()
	{
		// Tier 1
		addMaterialStats(MaterialIds.copper,
			new GunInternalMaterialStats(210, 3.0f, 0.5f));

		// Tier 2
		addMaterialStats(MaterialIds.iron,
			new GunInternalMaterialStats(250, 6.0f, 1.0f));
		addMaterialStats(MaterialIds.osmium,
			new GunInternalMaterialStats(500, 4.5f, 1.25f));
		addMaterialStats(MaterialIds.tungsten,
			new GunInternalMaterialStats(350, 6.5f, 0.75f));
		addMaterialStats(MaterialIds.platinum,
			new GunInternalMaterialStats(1200, 4.0f, 0.75f));
		addMaterialStats(MaterialIds.silver,
			new GunInternalMaterialStats(900, 5.0f, 1.0f));
		addMaterialStats(MaterialIds.lead,
			new GunInternalMaterialStats(200, 8.0f, 0.125f));
		addMaterialStats(MaterialIds.aluminum,
			new GunInternalMaterialStats(210, 4.0f, 1.0f));
		addMaterialStats(MaterialIds.gold,
			new GunInternalMaterialStats(10, 12.0f, 4.0f));
		addMaterialStats(MaterialIds.obsidian,
			new GunInternalMaterialStats(3000, 1.0f, 0.125f));

		// Tier 3
		addMaterialStats(MaterialIds.slimesteel,
			new GunInternalMaterialStats(1040, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.amethystBronze,
			new GunInternalMaterialStats(720, 7.0f, 1.5f));
		addMaterialStats(MaterialIds.nahuatl,
			new GunInternalMaterialStats(350, 4.5f, 2.5f));
		addMaterialStats(MaterialIds.pigIron,
			new GunInternalMaterialStats(580, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.roseGold,
			new GunInternalMaterialStats(175, 9.0f, 1.0f));
		addMaterialStats(MaterialIds.cobalt,
			new GunInternalMaterialStats(800, 7.0f, 2.25f));
		addMaterialStats(MaterialIds.steel,
			new GunInternalMaterialStats(775, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.bronze,
			new GunInternalMaterialStats(760, 6.5f, 1.75f));
		addMaterialStats(MaterialIds.constantan,
			new GunInternalMaterialStats(675, 5.5f, 2.5f));
		addMaterialStats(MaterialIds.invar,
			new GunInternalMaterialStats(630, 5.5f, 2.5f));
		addMaterialStats(MaterialIds.necronium,
			new GunInternalMaterialStats(357, 4.0f, 3.0f));
		addMaterialStats(MaterialIds.electrum,
			new GunInternalMaterialStats(225, 10.0f, 1.5f));

		// Tier 4
		addMaterialStats(MaterialIds.manyullyn,
			new GunInternalMaterialStats(1250, 6.5f, 3.5f));
		addMaterialStats(MaterialIds.hepatizon,
			new GunInternalMaterialStats(975, 11.0f, 2.25f));
	}

	private void addGunHandles()
	{
		// Tier 1
		addMaterialStats(MaterialIds.copper,
			new GunHandleMaterialStats(210, 3.0f, 0.5f));
		addMaterialStats(MaterialIds.wood,
			new GunHandleMaterialStats(210, 3.0f, 0.5f));
		addMaterialStats(MaterialIds.bone,
			new GunHandleMaterialStats(210, 3.0f, 0.5f));

		// Tier 2
		addMaterialStats(MaterialIds.iron,
			new GunHandleMaterialStats(250, 6.0f, 1.0f));
		addMaterialStats(MaterialIds.osmium,
			new GunHandleMaterialStats(500, 4.5f, 1.25f));
		addMaterialStats(MaterialIds.tungsten,
			new GunHandleMaterialStats(350, 6.5f, 0.75f));
		addMaterialStats(MaterialIds.platinum,
			new GunHandleMaterialStats(1200, 4.0f, 0.75f));
		addMaterialStats(MaterialIds.silver,
			new GunHandleMaterialStats(900, 5.0f, 1.0f));
		addMaterialStats(MaterialIds.lead,
			new GunHandleMaterialStats(200, 8.0f, 0.125f));
		addMaterialStats(MaterialIds.aluminum,
			new GunHandleMaterialStats(210, 4.0f, 1.0f));
		addMaterialStats(MaterialIds.gold,
			new GunHandleMaterialStats(10, 12.0f, 4.0f));
		addMaterialStats(MaterialIds.obsidian,
			new GunHandleMaterialStats(3000, 1.0f, 0.125f));

		addMaterialStats(MaterialIds.slimewood,
			new GunHandleMaterialStats(3000, 1.0f, 0.125f));
		addMaterialStats(MaterialIds.necroticBone,
			new GunHandleMaterialStats(3000, 1.0f, 0.125f));

		// Tier 3
		addMaterialStats(MaterialIds.slimesteel,
			new GunHandleMaterialStats(1040, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.amethystBronze,
			new GunHandleMaterialStats(720, 7.0f, 1.5f));
		addMaterialStats(MaterialIds.nahuatl,
			new GunHandleMaterialStats(350, 4.5f, 2.5f));
		addMaterialStats(MaterialIds.pigIron,
			new GunHandleMaterialStats(580, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.roseGold,
			new GunHandleMaterialStats(175, 9.0f, 1.0f));
		addMaterialStats(MaterialIds.cobalt,
			new GunHandleMaterialStats(800, 7.0f, 2.25f));
		addMaterialStats(MaterialIds.steel,
			new GunHandleMaterialStats(775, 6.0f, 2.0f));
		addMaterialStats(MaterialIds.bronze,
			new GunHandleMaterialStats(760, 6.5f, 1.75f));
		addMaterialStats(MaterialIds.constantan,
			new GunHandleMaterialStats(675, 5.5f, 2.5f));
		addMaterialStats(MaterialIds.invar,
			new GunHandleMaterialStats(630, 5.5f, 2.5f));
		addMaterialStats(MaterialIds.necronium,
			new GunHandleMaterialStats(357, 4.0f, 3.0f));
		addMaterialStats(MaterialIds.electrum,
			new GunHandleMaterialStats(225, 10.0f, 1.5f));

		addMaterialStats(MaterialIds.blazingBone,
			new GunHandleMaterialStats(100, 1.0f, 0.125f));
		addMaterialStats(MaterialIds.platedSlimewood,
			new GunHandleMaterialStats(2000, 1.0f, 0.125f));

		// Tier 4
		addMaterialStats(MaterialIds.manyullyn,
			new GunHandleMaterialStats(1250, 6.5f, 3.5f));
		addMaterialStats(MaterialIds.hepatizon,
			new GunHandleMaterialStats(975, 11.0f, 2.25f));
	}
}
