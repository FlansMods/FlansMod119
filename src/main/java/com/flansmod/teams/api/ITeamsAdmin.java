package com.flansmod.teams.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ITeamsAdmin
{
	@Nonnull Collection<MapInfo> getAllMaps();
	@Nonnull Collection<GamemodeInfo> getAllGamemodes();
	@Nonnull ISettings getDefaultSettings();

	@Nonnull Collection<RoundInfo> getMapRotation();
	@Nonnull OpResult createNewSettings(@Nonnull String settingsName);
	@Nullable MapInfo getMapData(@Nonnull String mapName);
	default boolean hasMap(@Nonnull String mapName) { return getMapData(mapName) != null; }
	@Nullable GamemodeInfo getGamemode(@Nonnull String gamemode);
	default boolean hasGamemode(@Nonnull String gamemode) { return getGamemode(gamemode) != null; }

	@Nullable RoundInfo tryCreateRoundInfo(@Nonnull String mapName, @Nonnull String gamemodeID, @Nonnull String ... teamNames);


	@Nonnull OpResult createMap(@Nonnull String mapName);
	@Nonnull OpResult deleteMap(@Nonnull String mapName);
	@Nonnull OpResult enableMapRotation();
	@Nonnull OpResult disableMapRotation();
	@Nonnull OpResult setMapRotation(@Nonnull List<RoundInfo> mapNames);
	@Nonnull OpResult addMapToRotation(@Nonnull RoundInfo round, int positionHint);
	@Nonnull OpResult removeMapFromRotation(int inPosition);

	@Nonnull OpResult registerGamemode(@Nonnull GamemodeInfo gamemode);
	@Nonnull OpResult registerIntParameter(@Nonnull String parameterName, int defaultValue);
	@Nonnull OpResult registerBooleanParameter(@Nonnull String parameterName, boolean defaultValue);
}
