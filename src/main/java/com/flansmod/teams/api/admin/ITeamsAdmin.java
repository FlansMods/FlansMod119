package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.OpResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ITeamsAdmin
{
	@Nonnull ResourceKey<Level> getLobbyDimension();
	@Nonnull ISpawnPoint getLobbySpawnPoint();

	@Nonnull Collection<String> getAllMaps();
	@Nonnull Collection<ResourceLocation> getAllGamemodes();
	@Nonnull ISettings getDefaultSettings();

	@Nonnull Collection<RoundInfo> getMapRotation();
	@Nonnull
	OpResult createNewSettings(@Nonnull String settingsName);
	@Nullable IMapDetails getMapData(@Nonnull String mapName);
	default boolean hasMap(@Nonnull String mapName) { return getMapData(mapName) != null; }
	@Nullable IGamemodeFactory getGamemode(@Nonnull ResourceLocation gamemodeID);
	default boolean hasGamemode(@Nonnull ResourceLocation gamemodeID) { return getGamemode(gamemodeID) != null; }

	@Nullable RoundInfo tryCreateRoundInfo(@Nonnull String mapName, @Nonnull ResourceLocation gamemodeID, @Nonnull String ... teamNames);
	boolean isInBuildMode(@Nonnull UUID player);
	@Nonnull OpResult setBuildMode(@Nonnull UUID player, boolean set);

	@Nullable IPlayerPersistentInfo getPlayerData(@Nonnull UUID playerID);

	@Nonnull OpResult createMap(@Nonnull String mapName);
	@Nonnull OpResult deleteMap(@Nonnull String mapName);
	@Nonnull OpResult enableMapRotation();
	@Nonnull OpResult disableMapRotation();
	@Nonnull OpResult setMapRotation(@Nonnull List<RoundInfo> mapNames);
	@Nonnull OpResult addMapToRotation(@Nonnull RoundInfo round, int positionHint);
	@Nonnull OpResult removeMapFromRotation(int inPosition);

	@Nonnull OpResult registerGamemode(@Nonnull ResourceLocation gamemodeID, @Nonnull IGamemodeFactory factory);
	@Nonnull OpResult registerIntParameter(@Nonnull String parameterName, int defaultValue);
	@Nonnull OpResult registerBooleanParameter(@Nonnull String parameterName, boolean defaultValue);
}
