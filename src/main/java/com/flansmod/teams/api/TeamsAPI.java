package com.flansmod.teams.api;

import com.flansmod.teams.api.admin.ITeamsAdmin;
import com.flansmod.teams.api.runtime.ITeamsRuntime;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamsAPI
{
	public static final String INVALID_MAP_NAME = "null";
	public static final String SCORE_TYPE_OBJECTIVES = "obj";
	public static final String SCORE_TYPE_KILLS = "kill";
	public static final String SCORE_TYPE_ASSISTS = "assist";
	public static final String SCORE_TYPE_DEATHS = "death";
	public static final String RELATIONSHIP_TYPE_ASSIST = "assist";

	@Nonnull
	public static OpResult isValidMapName(@Nonnull String mapName) { return INVALID_MAP_NAME.equals(mapName) ? OpResult.FAILURE_GENERIC : OpResult.SUCCESS; }

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
