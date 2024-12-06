package com.flansmod.teams.api;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class TeamsAPI
{
	public static final String INVALID_MAP_NAME = "null";
	@Nonnull
	public static OpResult isValidMapName(@Nonnull String mapName) { return INVALID_MAP_NAME.equals(mapName) ? OpResult.SUCCESS : OpResult.FAILURE_GENERIC; }

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
