package com.flansmod.teams.server;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.runtime.IMapInstance;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.MapInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SingleDimensionMapInstance implements IMapInstance
{
	private final MapInfo def;
	private final ResourceKey<Level> dimension;
	private final List<ChunkPos> chunks = new ArrayList<>();
	private final List<ISpawnPoint> spawnPoints = new ArrayList<>();
	private final List<IControlPointRef> controlPoints = new ArrayList<>();

	public SingleDimensionMapInstance(@Nonnull MapInfo mapDef,
									  @Nonnull ResourceKey<Level> dim)
	{
		def = mapDef;
		dimension = dim;
	}

	@Override @Nonnull
	public MapInfo getInfo() { return def; }
	@Override @Nonnull
	public ResourceKey<Level> getPrimaryDimension()
	{
		return dimension;
	}
	@Override @Nonnull
	public List<ChunkPos> getChunkLoadTickets()
	{
		return chunks;
	}
	@Override @Nonnull
	public List<ISpawnPoint> getSpawnPoints() { return spawnPoints; }
	@Override @Nonnull
	public List<IControlPointRef> getControlPoints() { return controlPoints; }
}
