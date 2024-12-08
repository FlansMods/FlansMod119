package com.flansmod.teams.server.map;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.runtime.IControlPointInstance;
import com.flansmod.teams.api.runtime.IMapInstance;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.MapInfo;
import com.flansmod.teams.server.map.ControlPointRef;
import com.flansmod.teams.server.map.SpawnPointRef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	@Override @Nullable
	public IControlPointInstance tryResolve(@Nonnull IControlPointRef ref, boolean loadChunks)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
		{
			ServerLevel level = server.getLevel(dimension);
			if(level != null)
			{
				if(loadChunks || level.isLoaded(ref.getPos()))
				{
					BlockEntity blockEntity = level.getBlockEntity(ref.getPos());
					if(blockEntity instanceof IControlPointInstance inst)
						return inst;
				}
			}
		}
		return null;
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

	public void loadFrom(@Nonnull CompoundTag tags)
	{
		ListTag chunksTag = tags.getList("chunks", 10);
		for(int i = 0; i < chunksTag.size(); i++)
		{
			CompoundTag chunkTag = chunksTag.getCompound(i);
			chunks.add(new ChunkPos(chunkTag.getInt("x"), chunkTag.getInt("z")));
		}

		ListTag spawnPointsTag = tags.getList("spawnPoints", 10);
		for(int i = 0; i < spawnPointsTag.size(); i++)
		{
			CompoundTag spawnPointTag = spawnPointsTag.getCompound(i);
			spawnPoints.add(SpawnPointRef.of(dimension, spawnPointTag));
		}

		ListTag controlPointsTag = tags.getList("controlPoints", 10);
		for(int i = 0; i < controlPointsTag.size(); i++)
		{
			CompoundTag controlPointTag = controlPointsTag.getCompound(i);
			controlPoints.add(ControlPointRef.of(dimension, controlPointTag));
		}
	}
	public void saveTo(@Nonnull CompoundTag tags)
	{
		ListTag chunksTag = new ListTag();
		for (ChunkPos chunkPos : chunks)
		{
			CompoundTag chunkTag = new CompoundTag();
			chunkTag.putInt("x", chunkPos.x);
			chunkTag.putInt("z", chunkPos.z);
			chunksTag.add(chunkTag);
		}
		tags.put("chunks", chunksTag);

		ListTag spawnPointsTag = new ListTag();
		for (ISpawnPoint spawnPoint : spawnPoints)
		{
			CompoundTag spawnPointTag = new CompoundTag();
			spawnPointTag.putInt("x", spawnPoint.getPos().getX());
			spawnPointTag.putInt("y", spawnPoint.getPos().getY());
			spawnPointTag.putInt("z", spawnPoint.getPos().getZ());
			spawnPointsTag.add(spawnPointTag);
		}
		tags.put("spawnPoints", spawnPointsTag);

		ListTag controlPointsTag = new ListTag();
		for (IControlPointRef controlPoint : controlPoints)
		{
			CompoundTag controlPointTag = new CompoundTag();
			controlPointTag.putInt("x", controlPoint.getPos().getX());
			controlPointTag.putInt("y", controlPoint.getPos().getY());
			controlPointTag.putInt("z", controlPoint.getPos().getZ());
			controlPointTag.putDouble("radius", controlPoint.getRadius());
			controlPointTag.putInt("team", controlPoint.getStartingTeamIndex());
			controlPointsTag.add(controlPointTag);
		}
		tags.put("controlPoints", controlPointsTag);
	}
}
