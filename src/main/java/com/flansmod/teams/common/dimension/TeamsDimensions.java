package com.flansmod.teams.common.dimension;

import com.flansmod.teams.common.TeamsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class TeamsDimensions
{
	private static final ResourceLocation TEAMS_DIMENSION_TYPE_ID = new ResourceLocation(TeamsMod.MODID, "teams");
	private static final ResourceLocation TEAMS_LOBBY_ID = new ResourceLocation(TeamsMod.MODID, "lobby");
	private static final ResourceLocation TEAMS_CONSTRUCT_ID = new ResourceLocation(TeamsMod.MODID, "construct");
	private static final ResourceLocation TEAMS_INSTANCE_A_ID = new ResourceLocation(TeamsMod.MODID, "instance_a");
	private static final ResourceLocation TEAMS_INSTANCE_B_ID = new ResourceLocation(TeamsMod.MODID, "instance_b");

	public static final ResourceKey<DimensionType> TEAMS_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, TEAMS_DIMENSION_TYPE_ID);

	public static final ResourceKey<Level> TEAMS_LOBBY_LEVEL = ResourceKey.create(Registries.DIMENSION, TEAMS_LOBBY_ID);
	public static final ResourceKey<Level> TEAMS_CONSTRUCT_LEVEL = ResourceKey.create(Registries.DIMENSION, TEAMS_CONSTRUCT_ID);
	public static final ResourceKey<Level> TEAMS_INSTANCE_A_LEVEL = ResourceKey.create(Registries.DIMENSION, TEAMS_INSTANCE_A_ID);
	public static final ResourceKey<Level> TEAMS_INSTANCE_B_LEVEL = ResourceKey.create(Registries.DIMENSION, TEAMS_INSTANCE_B_ID);

	public static final ResourceKey<LevelStem> TEAMS_LOBBY_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, TEAMS_LOBBY_ID);
	public static final ResourceKey<LevelStem> TEAMS_CONSTRUCT_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, TEAMS_CONSTRUCT_ID);
	public static final ResourceKey<LevelStem> TEAMS_INSTANCE_A_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, TEAMS_INSTANCE_A_ID);
	public static final ResourceKey<LevelStem> TEAMS_INSTANCE_B_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, TEAMS_INSTANCE_B_ID);

}
