package com.flansmod.teams.common;

import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;

public class TeamsModConfig
{
	public static final ForgeConfigSpec generalConfig;

	public static ForgeConfigSpec.DoubleValue preparingPhaseTimeout;
	public static ForgeConfigSpec.BooleanValue startInLobbyDimension;
	public static ForgeConfigSpec.BooleanValue startInLobbyDimensionWhenTeamsInactive;
	public static ForgeConfigSpec.BooleanValue useDimensionInstancing;


	public static ForgeConfigSpec.DoubleValue gameplayPhaseDurationMultiplier;


	public static ForgeConfigSpec.DoubleValue displayScoresPhaseDuration;


	public static ForgeConfigSpec.BooleanValue mapVoteEnabled;
	public static ForgeConfigSpec.DoubleValue mapVotePhaseDuration;
	public static ForgeConfigSpec.BooleanValue mapVotePhaseAllowResubmit;
	public static ForgeConfigSpec.BooleanValue mapVotePhaseEndEarlyIfMajority;


	public static ForgeConfigSpec.DoubleValue cleanupPhaseTimeout;


	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		init(builder);
		generalConfig = builder.build();
	}

	private static void init(@Nonnull ForgeConfigSpec.Builder builder)
	{
		builder.push("Round Preparation Settings");
		preparingPhaseTimeout = builder.defineInRange("preparing_timeout", 60d, 1d, 360d);
		useDimensionInstancing = builder.define("dimension_instancing", true);
		startInLobbyDimension = builder.define("start_in_lobby_dimenson", true);
		startInLobbyDimensionWhenTeamsInactive = builder.define("start_in_lobby_dimension_when_teams_inactive", false);

		builder.pop();


		builder.push("Gameplay Settings");
		gameplayPhaseDurationMultiplier = builder.defineInRange("gameplay_duration_multiplier", 1d, 0d, 100d);
		builder.pop();

		builder.push("Score Display Settings");
		displayScoresPhaseDuration = builder.defineInRange("display_scores_duration", 15d, 1d, 360d);
		builder.pop();


		builder.push("Map Vote Settings");
		mapVoteEnabled = builder.define("map_vote_enabled", true);
		mapVotePhaseDuration = builder.defineInRange("map_vote_duration", 15d, 1d, 120d);
		mapVotePhaseAllowResubmit = builder.define("map_vote_allow_resubmit", true);
		mapVotePhaseEndEarlyIfMajority = builder.define("map_vote_end_early_if_majority", false);
		builder.pop();

		builder.push("Cleanup Settings");
		cleanupPhaseTimeout = builder.defineInRange("cleanup_error_timeout", 60d, 1d, 360d);
		builder.pop();

	}
}
