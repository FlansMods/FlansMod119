package com.flansmod.teams.common.dimension;

import com.flansmod.teams.common.TeamsMod;
import com.google.common.io.Files;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DimensionInstancingManager
{
	public static final int INVALID_INSTANCE = -1;
	private final List<Instance> instances;
	private final ResourceKey<Level> fallbackDimension;
	private final BlockPos fallbackSpawn;

	private static class Instance
	{
		public final ResourceKey<Level> dimension;
		public CompletableFuture<Boolean> runningTask = null;
		public boolean inUse = false;

		public Instance(@Nonnull ResourceKey<Level> dim)
		{
			dimension = dim;
		}
	}

	public DimensionInstancingManager(@Nonnull List<ResourceKey<Level>> instanceDimensions,
									  @Nonnull ResourceKey<Level> fallbackDim,
									  @Nonnull BlockPos fallbackPos)
	{
		instances = new ArrayList<>(instanceDimensions.size());
		for(ResourceKey<Level> dim : instanceDimensions)
			instances.add(new Instance(dim));
		fallbackDimension = fallbackDim;
		fallbackSpawn = fallbackPos;
	}

	public int getNumInstances() { return instances.size(); }
	public int getNumFreeInstances()
	{
		int numFree = 0;
		for(Instance instance : instances)
			if(!instance.inUse)
				numFree++;

		return numFree;
	}
	@Nonnull
	public List<String> getInfo()
	{
		List<String> list = new ArrayList<>(getNumInstances());
		for(int i = 0; i < instances.size(); i++)
		{
			Instance instance = instances.get(i);
			list.add("["+i+"]: "+instance.dimension.location()+", "+(instance.inUse ? "ACTIVE":"INACTIVE"));
		}
		return list;
	}

	@Nullable
	public ResourceKey<Level> getDimension(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return null;
		return instances.get(instanceID).dimension;
	}
	public int reserveInstance()
	{
		for(int i = 0; i < instances.size(); i++)
		{
			Instance instance = instances.get(i);
			if (!instance.inUse)
			{
				instance.inUse = true;
				return i;
			}
		}
		return INVALID_INSTANCE;
	}
	public boolean beginLoadLevel(int instanceID, @Nonnull String levelID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return false;
		Instance instance = instances.get(instanceID);
		if (!instance.inUse)
			return false;

		// TODO: Find lobby spawn point
		kickAllPlayersFrom(instance.dimension, fallbackDimension, fallbackSpawn);
		closeInstance(instance.dimension);

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		instance.runningTask = server.submit(() -> runLevelCopy(instance, levelID));

		return true;
	}
	public boolean isLoadLevelComplete(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return true;
		Instance instance = instances.get(instanceID);
		if (instance.runningTask == null)
			return true;

		return instance.runningTask.isDone();
	}
	public boolean checkLoadLevelComplete(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return true;
		Instance instance = instances.get(instanceID);
		if (instance.runningTask == null)
			return true;

		if(instance.runningTask.isDone())
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerLevel level = server.getLevel(instance.dimension);
			if(level != null)
			{
				level.getChunkSource().tick(() -> true, false);
			}
			return true;
		}

		return false;
	}
	public boolean beginSaveChunksToLevel(@Nonnull ResourceKey<Level> srcDimension, @Nonnull ChunkPos min, @Nonnull ChunkPos max, @Nonnull String levelName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		server.submit(() -> runChunkCopy(srcDimension, min, max, levelName));
		return true;
	}

	private void kickAllPlayersFrom(@Nonnull ResourceKey<Level> dimension, @Nonnull ResourceKey<Level> fallbackDimension, @Nonnull BlockPos fallbackPos)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server.getLevel(dimension);
		ServerLevel targetLevel = server.getLevel(fallbackDimension);
		if(level != null && targetLevel != null)
		{
			List<ServerPlayer> playersToTeleport = new ArrayList<>(level.players());
			for(ServerPlayer player : playersToTeleport)
			{
				player.teleportTo(targetLevel, fallbackPos.getX(), fallbackPos.getY(), fallbackPos.getZ(), 0f, 0f);
				player.setRespawnPosition(fallbackDimension, fallbackPos, 0f, true, false);
			}
		}
	}
	private void closeInstance(@Nonnull ResourceKey<Level> dimension)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server.getLevel(dimension);
		if(level != null)
		{
			level.noSave = true;
			level.getChunkSource().removeTicketsOnClosing();
			level.getChunkSource().tick(() -> true, false);
			//try
			//{
			//	net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Unload(level));
			//	level.close();
			//}
			//catch (IOException e)
			//{
			//	TeamsMod.LOGGER.error(e.toString());
			//}
		}
	}

	private boolean runChunkCopy(@Nonnull ResourceKey<Level> srcDimension, @Nonnull ChunkPos min, @Nonnull ChunkPos max, @Nonnull String levelID)
	{


		return true;
	}

	private boolean runLevelCopy(@Nonnull Instance instance, @Nonnull String levelID)
	{
		try
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			File serverDir = server.getServerDirectory();
			Path levelRoot = server.getWorldPath(LevelResource.ROOT);
			Path dimRoot = DimensionType.getStorageFolder(instance.dimension, levelRoot);

			File dstDir = dimRoot.toFile();
			if (dstDir.exists() && dstDir.isDirectory())
			{
				File dstRegionsDir = new File(dstDir.getPath() + "/region/");
				if (dstRegionsDir.exists() && dstRegionsDir.isDirectory())
				{
					if (!dstRegionsDir.delete())
						return false;
				}

				File srcDir = new File(serverDir.getPath() + "/teams_maps/" + levelID);
				if (srcDir.exists() && srcDir.isDirectory())
				{
					File srcRegionsDir = new File(srcDir.getPath() + "/region/");
					if (srcRegionsDir.exists() && srcRegionsDir.isDirectory())
					{
						Files.copy(srcRegionsDir, dstRegionsDir);
					}

					File srcDat = new File(srcDir.getPath() + "/level.dat");
					if(srcDat.exists())
					{
						File dstDat = new File(dstDir.getPath() + "/level.dat");
						Files.copy(srcDat, dstDat);
					}
				}

			}
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
		}
		return false;
	}

	public boolean enterInstance(@Nonnull ServerPlayer player, int instanceID, double x, double y, double z)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return false;

		Instance instance = instances.get(instanceID);
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server.getLevel(instance.dimension);
		if(level != null)
		{
			player.teleportTo(level, x, y, z, 0f, 0f);
			return true;
		}

		return false;
	}
}
