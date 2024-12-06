package com.flansmod.teams.server;

import com.flansmod.teams.api.IMapInstance;
import com.flansmod.teams.api.MapInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class MapInstance implements IMapInstance
{
	private final MapInfo def;

	public MapInstance(@Nonnull MapInfo mapDef)
	{
		def = mapDef;
	}

	@Override @Nonnull
	public MapInfo getInfo() { return def; }

	@Nonnull
	@Override
	public ResourceKey<Level> getPrimaryDimension()
	{
		return null;
	}

	@Nonnull
	@Override
	public List<ChunkPos> getChunkLoadTickets()
	{
		return null;
	}
}
