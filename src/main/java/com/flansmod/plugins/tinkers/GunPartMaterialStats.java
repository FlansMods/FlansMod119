package com.flansmod.plugins.tinkers;

import com.flansmod.common.FlansMod;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nonnull;
import java.util.List;

public record GunPartMaterialStats(int durability, float impactDamage, float rateOfFire)
	implements IRepairableMaterialStats
{
	public static final MaterialStatsId ID = new MaterialStatsId(new ResourceLocation(FlansMod.MODID, "gun_part"));
	public static final MaterialStatType<GunPartMaterialStats> TYPE =
		new MaterialStatType<>(ID,
			new GunPartMaterialStats(1, 1f, 1f),
			RecordLoadable.create(
				IRepairableMaterialStats.DURABILITY_FIELD,
				FloatLoadable.FROM_ZERO.defaultField("impact_damage", 1f, true, GunPartMaterialStats::impactDamage),
				FloatLoadable.FROM_ZERO.defaultField("rate_of_fire", 1f, true, GunPartMaterialStats::rateOfFire),
				GunPartMaterialStats::new));
	private static final List<Component> DESCRIPTION =
		ImmutableList.of(
			ToolStats.DURABILITY.getDescription(),
			FMToolStats.IMPACT_DAMAGE.getDescription(),
			FMToolStats.RATE_OF_FIRE.getDescription());

	@Override @Nonnull
	public MaterialStatType<?> getType() {
		return TYPE;
	}
	@Override @Nonnull
	public List<Component> getLocalizedInfo()
	{
		List<Component> info = Lists.newArrayList();
		info.add(ToolStats.DURABILITY.formatValue(this.durability));
		info.add(FMToolStats.RATE_OF_FIRE.formatValue(this.rateOfFire));
		info.add(FMToolStats.IMPACT_DAMAGE.formatValue(this.impactDamage));
		return info;
	}
	@Override @Nonnull
	public List<Component> getLocalizedDescriptions() {
		return DESCRIPTION;
	}

	@Override
	public void apply(@Nonnull ModifierStatsBuilder builder, float scale)
	{
		// update for floats cancels out the base stats the first time used, makes the behavior more predictable between this and the stats module
		ToolStats.DURABILITY.update(builder, durability * scale);
		FMToolStats.IMPACT_DAMAGE.update(builder, impactDamage * scale);
		FMToolStats.RATE_OF_FIRE.update(builder, rateOfFire * scale);
	}
}
