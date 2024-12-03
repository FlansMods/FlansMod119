package com.flansmod.teams.api;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface ITeamsAdmin
{
	@Nonnull Collection<String> getMapNames();
	@Nonnull Collection<String> getMapRotation();
	@Nonnull ISettings getDefaultSettings();
	@Nonnull
	OpResult createNewSettings(@Nonnull String settingsName);
	boolean hasMap(@Nonnull String mapName);



	@Nonnull
	OpResult createMap(@Nonnull String mapName);
	@Nonnull
	OpResult deleteMap(@Nonnull String mapName);
	@Nonnull
	OpResult enableMapRotation();
	@Nonnull
	OpResult disableMapRotation();
	@Nonnull
	OpResult setMapRotation(@Nonnull List<String> mapNames);
	@Nonnull
	OpResult addMapToRotation(@Nonnull String mapName, int positionHint);
	@Nonnull
	OpResult removeMapFromRotation(@Nonnull String mapName);
	@Nonnull
	OpResult removeMapFromRotation(int inPosition);

	@Nonnull
	OpResult registerIntParameter(@Nonnull String parameterName, int defaultValue);
	@Nonnull
	OpResult registerBooleanParameter(@Nonnull String parameterName, boolean defaultValue);
}
