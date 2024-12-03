package com.flansmod.teams.api;

import javax.annotation.Nonnull;

public interface ITeamsRuntime
{
	@Nonnull String getCurrentMap();
	@Nonnull String getNextMap();
	@Nonnull IMap getMapData(@Nonnull String mapName);


	@Nonnull
	OpResult setNextMap(@Nonnull String mapName);

	@Nonnull
	OpResult goToNextMap();

}
