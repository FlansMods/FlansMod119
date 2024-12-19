package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.OpResult;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IDimensionInstancer
{
	@Nonnull OpResult beginLoad(@Nonnull String mapName);
	boolean isLoaded(@Nonnull String mapName);
	@Nullable ResourceKey<Level> dimensionOf(@Nonnull String mapName);
	@Nullable String getMapLoadedIn(@Nonnull ResourceKey<Level> dimension);
	@Nonnull OpResult beginUnload(@Nonnull String mapName);
	@Nonnull List<String> printDebug();

	boolean managesDimension(@Nonnull ResourceKey<Level> dimension);
}
