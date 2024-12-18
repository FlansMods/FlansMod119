package com.flansmod.teams.common.dimension;

import com.flansmod.teams.common.TeamsMod;
import com.google.common.io.Files;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
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
		public String currentMap = null;
		public String loadingMap = null;
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

	public void serverTick()
	{
		for(Instance instance : instances)
		{
			if(instance.runningTask != null && instance.runningTask.isDone())
			{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				ServerLevel level = server.getLevel(instance.dimension);
				if(level != null)
				{
					level.getChunkSource().tick(() -> true, false);
				}
				instance.runningTask = null;
			}
		}
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
			list.add("["+i+"]: "+instance.dimension.location()+", map='"+instance.currentMap+"', "+(instance.inUse ? "ACTIVE":"INACTIVE"));
		}
		return list;
	}
	@Nullable
	public String getLoadedLevel(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return null;
		return instances.get(instanceID).currentMap;
	}
	@Nullable
	public String getLoadingLevel(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return null;
		return instances.get(instanceID).loadingMap;
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
	public boolean beginLoadLevel(int instanceID, @Nonnull String levelID, @Nullable CommandSourceStack listener)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return false;
		Instance instance = instances.get(instanceID);
		if (!instance.inUse)
		{
			if(listener != null)
				listener.sendFailure(Component.translatable("teams.construct.load.failure_instance_not_empty"));
			return false;
		}
		if (instance.runningTask != null)
		{
			if(listener != null)
				listener.sendFailure(Component.translatable("teams.construct.load.failure_instance_busy"));
			return false;
		}
		if(levelID.equals(instance.currentMap))
		{
			if(listener != null)
				listener.sendFailure(Component.translatable("teams.construct.load.map_already_loaded"));
			return false;
		}

		// TODO: Find lobby spawn point
		kickAllPlayersFrom(instance.dimension, fallbackDimension, fallbackSpawn, listener);
		closeInstance(instance.dimension);

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		instance.loadingMap = levelID;
		instance.runningTask = server.submit(() -> runLevelCopy(instance, levelID, listener));

		return true;
	}
	public boolean checkLoadLevelComplete(int instanceID)
	{
		if(instanceID < 0 || instanceID >= instances.size())
			return true;
		Instance instance = instances.get(instanceID);

		return instance.runningTask == null;
	}
	public boolean beginSaveChunksToLevel(@Nonnull ResourceKey<Level> srcDimension, @Nonnull ChunkPos min, @Nonnull ChunkPos max, @Nonnull String levelName, @Nullable CommandSourceStack listener)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		server.submit(() -> runChunkCopy(srcDimension, min, max, levelName, listener));
		return true;
	}

	private void kickAllPlayersFrom(@Nonnull ResourceKey<Level> dimension,
									@Nonnull ResourceKey<Level> fallbackDimension,
									@Nonnull BlockPos fallbackPos,
									@Nullable CommandSourceStack listener)
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
			TeamsMod.LOGGER.info("Moved "+playersToTeleport.size()+" player(s) from "+dimension+" to "+fallbackDimension);
			if(listener != null)
				listener.sendSystemMessage(Component.translatable("teams.moved_players.success", playersToTeleport.size()));
		}
		else
		{
			TeamsMod.LOGGER.warn("Could not kick all players from "+dimension+" to "+fallbackDimension);
			if(listener != null)
				listener.sendFailure(Component.translatable("teams.moved_players.failure"));
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

			// TODO: We might still need to reflect to set emptyTime=TIMEOUT
			// This should clear the loaded chunk list so that next time we use the level, it loads from our disk copy.

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

	private boolean runChunkCopy(@Nonnull ResourceKey<Level> srcDimension, @Nonnull ChunkPos min, @Nonnull ChunkPos max, @Nonnull String levelID, @Nullable CommandSourceStack listener)
	{
		try
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			File serverDir = server.getServerDirectory();
			File dstDir = new File(serverDir.getPath() + "/teams_maps/" + levelID);
			if (!dstDir.exists())
			{
				Files.createParentDirs(dstDir);
			}

			Path levelRoot = server.getWorldPath(LevelResource.ROOT);
			Path dimRoot = DimensionType.getStorageFolder(srcDimension, levelRoot);
			File srcDir = dimRoot.toFile();
			if (srcDir.exists() && srcDir.isDirectory())
			{
				// I think level.dat is for the whole SAVE, not per dimension
				//File srcDat = new File(srcDir.getPath() + "/level.dat");
				//if(srcDat.exists())
				//{
				//	File dstDat = new File(dstDir.getPath() + "/level.dat");
				//	Files.createParentDirs(dstDat);
				//	Files.copy(srcDat, dstDat);
				//}
				//else
				//{
				//	TeamsMod.LOGGER.warn("[runChunkCopy] Missing level.dat file '"+srcDat+"'");
				//	if(listener != null)
				//		listener.sendFailure(Component.translatable("teams.construct.clone.failure_bad_level"));
				//}

				File srcRegionsDir = new File(srcDir.getPath() + "/region/");
				if (srcRegionsDir.exists() && srcRegionsDir.isDirectory())
				{
					File dstRegionsDir = new File(dstDir.getPath() + "/region/");

					int regionMinX = min.getRegionX();
					int regionMinZ = min.getRegionZ();
					int regionMaxX = max.getRegionX();
					int regionMaxZ = max.getRegionZ();

					int regionCount = (regionMaxX - regionMinX + 1) * (regionMaxZ - regionMinZ + 1);
					int completedCount = 0;
					int skippedCount = 0;

					for (int i = regionMinX; i <= regionMaxX; i++)
					{
						for (int j = regionMinZ; j <= regionMaxZ; j++)
						{
							boolean shouldOutputDebug = false;
							File srcRegionIJ = new File(srcRegionsDir.getPath() + "/r." + i + "." + j + ".mca");
							File dstRegionIJ = new File(dstRegionsDir.getPath() + "/r." + i + "." + j + ".mca");
							if (srcRegionIJ.exists())
							{
								Files.createParentDirs(dstRegionIJ);
								Files.copy(srcRegionIJ, dstRegionIJ);

								int tenthsDoneBefore = ((completedCount + skippedCount) * 10) / regionCount;
								completedCount++;
								int tenthsDoneAfter = ((completedCount + skippedCount) * 10) / regionCount;
								shouldOutputDebug = tenthsDoneAfter != tenthsDoneBefore;
							}
							else
							{
								skippedCount++;
								shouldOutputDebug = true;
							}
							if(shouldOutputDebug)
							{
								TeamsMod.LOGGER.warn("[runChunkCopy] Chunk Clone at ["+(completedCount+skippedCount)+"/"+regionCount+"] (Skipped "+skippedCount+")");
								if(listener != null)
									listener.sendSystemMessage(Component.translatable("teams.construct.clone.progress", completedCount+skippedCount, regionCount, skippedCount));
							}


						}
					}

					TeamsMod.LOGGER.warn("[runChunkCopy] Level cloned from '"+srcDir+"' to '"+dstDir+"'");
					if(listener != null)
						listener.sendSuccess(() -> Component.translatable("teams.construct.clone.success", levelID), true);
					return true;
				}
				else
				{
					TeamsMod.LOGGER.warn("[runChunkCopy] Level for clone has no region files '"+srcRegionsDir+"'");
					if(listener != null)
						listener.sendFailure(Component.translatable("teams.construct.clone.failure_bad_level"));
				}
			}
			else
			{
				TeamsMod.LOGGER.warn("[runChunkCopy] Level directory for "+srcDimension+" missing '"+srcDir+"'");
				if(listener != null)
					listener.sendFailure(Component.translatable("teams.construct.clone.failure_bad_level"));
			}
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
			if(listener != null)
				listener.sendFailure(Component.literal(e.toString()));
		}

		return false;
	}

	private boolean runLevelCopy(@Nonnull Instance instance, @Nonnull String levelID, @Nullable CommandSourceStack listener)
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
					File[] regionFiles = dstRegionsDir.listFiles();
					if(regionFiles != null)
					{
						for (File regionFile : regionFiles)
						{
							if (!regionFile.delete())
							{
								TeamsMod.LOGGER.error("[runLevelCopy] Region file '" + regionFile + "' could not be deleted");
								if (listener != null)
									listener.sendFailure(Component.translatable("teams.construct.load.failure_io_error"));
								return false;
							}
						}
					}
				}

				File srcDir = new File(serverDir.getPath() + "/teams_maps/" + levelID);
				if (srcDir.exists() && srcDir.isDirectory())
				{
					File srcRegionsDir = new File(srcDir.getPath() + "/region/");
					if (srcRegionsDir.exists() && srcRegionsDir.isDirectory())
					{
						File[] srcRegionFiles = srcRegionsDir.listFiles();
						if(srcRegionFiles != null)
						{
							for (File srcRegionFile : srcRegionFiles)
							{
								File dstRegionFile = new File(dstRegionsDir + srcRegionFile.getName());
								Files.createParentDirs(dstRegionFile);
								Files.copy(srcRegionFile, dstRegionFile);
							}
						}
					}
					else
					{
						TeamsMod.LOGGER.error("[runLevelCopy] Regions folder does not exist at '"+srcRegionsDir+"'");
						if(listener != null)
							listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
						return false;
					}

					// I think level.dat is for the whole SAVE, not per dimension
					//File srcDat = new File(srcDir.getPath() + "/level.dat");
					//if(srcDat.exists())
					//{
					//	File dstDat = new File(dstDir.getPath() + "/level.dat");
					//	Files.copy(srcDat, dstDat);
					//}
					//else
					//{
					//	TeamsMod.LOGGER.error("[runLevelCopy] Level.dat does not exist at '"+srcDat+"'");
					//	if(listener != null)
					//		listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
					//	return false;
					//}

					instance.currentMap = instance.loadingMap;
					instance.loadingMap = null;
					instance.runningTask = null;

					TeamsMod.LOGGER.info("[runLevelCopy] Successfully copied '"+srcDir+"' to '"+dstDir+"'");
					if(listener != null)
						listener.sendSuccess(() -> Component.translatable("teams.construct.load.success", levelID), true);
					return true;
				}
				else
				{
					TeamsMod.LOGGER.error("[runLevelCopy] Level directory does not exist at '"+srcDir+"'");
					if(listener != null)
						listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
					return false;
				}
			}
			else
			{
				TeamsMod.LOGGER.error("[runLevelCopy] Target level directory does not exist at '"+dstDir+"'");
				if(listener != null)
					listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
				return false;
			}
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
			if(listener != null)
				listener.sendFailure(Component.literal(e.toString()));
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
