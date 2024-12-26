package com.flansmod.teams.api;

import com.flansmod.teams.api.admin.ITeamsAdmin;
import com.flansmod.teams.api.runtime.ITeamsRuntime;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamsAPI
{
	public static final String SCORE_TYPE_OBJECTIVES = "obj";
	public static final String SCORE_TYPE_KILLS = "kill";
	public static final String SCORE_TYPE_ASSISTS = "assist";
	public static final String SCORE_TYPE_DEATHS = "death";
	public static final String RELATIONSHIP_TYPE_ASSIST = "assist";

	// Gamemode IDs
	public static final ResourceLocation invalidGamemode = new ResourceLocation("flansteams", "null");
	public static boolean isValidGamemodeID(@Nonnull ResourceLocation gamemodeID) { return !invalidGamemode.equals(gamemodeID); }

	// Map Names
	public static final String invalidMapName = "null";
	public static boolean isValidMapName(@Nonnull String mapName) { return !mapName.isEmpty() && !invalidMapName.equals(mapName); }
	public static OpResult validateMapName(@Nonnull String mapName) { return isValidMapName(mapName) ? OpResult.SUCCESS : OpResult.FAILURE_INVALID_MAP_NAME; }

	// Team IDs
	public static final ResourceLocation invalidTeam = new ResourceLocation("flansteams", "null");
	public static final ResourceLocation spectatorTeam = new ResourceLocation("flansteams", "spectator");
	public static boolean isSpectator(@Nonnull ResourceLocation teamID) { return spectatorTeam.equals(teamID); }
	public static boolean isValidTeamID(@Nonnull ResourceLocation teamID) { return !invalidTeam.equals(teamID); }

	// Settings Names
	public static final String defaultSettingsName = "default";
	public static boolean isValidSettingsName(@Nonnull String settingsName) { return !settingsName.isEmpty(); }
	public static OpResult validateSettingsName(@Nonnull String settingsName) { return isValidSettingsName(settingsName) ? OpResult.SUCCESS : OpResult.FAILURE_INVALID_MAP_NAME; }




	private static ITeamsAdmin adminInstance;
	private static ITeamsRuntime runtimeInstance;

	public static void registerInstance(@Nonnull ITeamsAdmin admin,
										@Nonnull ITeamsRuntime runtime,
										@Nonnull Logger logger)
	{
		if(!isInitialized())
		{
			adminInstance = admin;
			runtimeInstance = runtime;
		}
		else
			logger.error("Trying to initialize TeamsAPI twice??");
	}

	public static boolean isInitialized() { return runtimeInstance != null && adminInstance != null; }
	@Nullable
	public static ITeamsAdmin getAdmin() { return adminInstance; }
	@Nullable
	public static ITeamsRuntime getRuntime() { return runtimeInstance; }
}
