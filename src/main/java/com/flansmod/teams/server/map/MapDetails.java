package com.flansmod.teams.server.map;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.runtime.IControlPointInstance;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.border.WorldBorder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MapDetails implements IMapDetails
{
	private final String mapName;
	private final List<ChunkPos> chunks = new ArrayList<>();
	private final List<ISpawnPoint> spawnPoints = new ArrayList<>();
	private final List<IControlPointRef> controlPoints = new ArrayList<>();
	private final WorldBorder worldBorder = new WorldBorder();

	public MapDetails(@Nonnull String name)
	{
		mapName = name;
	}
	public MapDetails(@Nonnull IMapDetails other)
	{
		mapName = other.getName();
		chunks.addAll(other.getChunkLoadTickets());
		spawnPoints.addAll(other.getSpawnPoints());
		controlPoints.addAll(other.getControlPoints());
	}

	@Override @Nullable
	public IControlPointInstance tryResolve(@Nonnull Level level, @Nonnull IControlPointRef ref, boolean loadChunks)
	{
		if(loadChunks || level.isLoaded(ref.getPos()))
		{
			BlockEntity blockEntity = level.getBlockEntity(ref.getPos());
			if(blockEntity instanceof IControlPointInstance inst)
				return inst;
		}
		return null;
	}
	@Override @Nonnull
	public String getName() { return mapName; }
	@Override @Nonnull
	public List<ChunkPos> getChunkLoadTickets()
	{
		return chunks;
	}
	@Override @Nonnull
	public List<ISpawnPoint> getSpawnPoints() { return spawnPoints; }
	@Override @Nonnull
	public List<IControlPointRef> getControlPoints() { return controlPoints; }
	public void setWorldBorder(double x, double z, double size)
	{
		worldBorder.setCenter(x, z);
		worldBorder.setSize(size);
	}
	@Override
	public double getWorldBorderCenterX() { return worldBorder.getCenterX(); }
	@Override
	public double getWorldBorderCenterZ() { return worldBorder.getCenterZ(); }
	@Override
	public double getWorldBorderSize() { return worldBorder.getSize(); }

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
			spawnPoints.add(SpawnPointRef.of(spawnPointTag));
		}

		ListTag controlPointsTag = tags.getList("controlPoints", 10);
		for(int i = 0; i < controlPointsTag.size(); i++)
		{
			CompoundTag controlPointTag = controlPointsTag.getCompound(i);
			controlPoints.add(ControlPointRef.of(controlPointTag));
		}

		CompoundTag worldBorderTags = tags.getCompound("border");
		double centerX = worldBorderTags.getDouble("centerX");
		double centerZ = worldBorderTags.getDouble("centerZ");
		double size = worldBorderTags.getDouble("size");
		worldBorder.setCenter(centerX, centerZ);
		worldBorder.setSize(size);
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

		CompoundTag worldBorderTags = new CompoundTag();
		worldBorderTags.putDouble("centerX", worldBorder.getCenterX());
		worldBorderTags.putDouble("centerZ", worldBorder.getCenterZ());
		worldBorderTags.putDouble("size", worldBorder.getSize());
		tags.put("border", worldBorderTags);
	}
}
