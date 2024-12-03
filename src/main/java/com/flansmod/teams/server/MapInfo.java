package com.flansmod.teams.server;

import com.flansmod.teams.api.IMap;
import com.flansmod.teams.api.ISettings;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MapInfo implements IMap {

	private String mapName;
	private ResourceKey<Level> primaryDimension;
	private List<ChunkPos> chunkLoadTickets;
	private Settings settingsOverride;


	@Nonnull @Override
	public String getName() { return mapName; }
	@Nonnull @Override
	public ResourceKey<Level> getPrimaryDimension(@Nonnull String mapName) { return primaryDimension; }
	@Nonnull @Override
	public List<ChunkPos> getChunkLoadTickets() { return chunkLoadTickets; }
	public boolean hasSettingsOverride() { return settingsOverride != null; }
	@Nullable @Override
	public ISettings getSettingsOverride() { return settingsOverride; }
}
