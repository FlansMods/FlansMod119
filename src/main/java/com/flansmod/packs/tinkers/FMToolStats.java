package com.flansmod.packs.tinkers;

import com.flansmod.common.FlansMod;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nonnull;

public class FMToolStats
{
	@Nonnull
	private static ToolStatId name(@Nonnull String name) { return new ToolStatId(FlansMod.MODID, name); }

	public static final FloatToolStat IMPACT_DAMAGE = ToolStats.register(new FloatToolStat(name("impact_damage"), 0xFF78A0CD, 1, 0.1f, 2048f, TinkerTags.Items.HARVEST));
	public static final FloatToolStat RATE_OF_FIRE = ToolStats.register(new FloatToolStat(name("rate_of_fire"), 0xFF78A0CD, 1, 0.1f, 2048f, TinkerTags.Items.HARVEST));
	public static final FloatToolStat RECOIL = ToolStats.register(new FloatToolStat(name("recoil"), 0xFF78A0CD, 1, 0.1f, 2048f, TinkerTags.Items.HARVEST));
	public static final FloatToolStat ACCURACY = ToolStats.register(new FloatToolStat(name("accuracy"), 0xFF78A0CD, 1, 0.1f, 2048f, TinkerTags.Items.HARVEST));
}
