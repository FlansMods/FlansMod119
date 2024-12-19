package com.flansmod.teams.server.dimension;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.server.map.MapDetails;
import com.google.common.io.Files;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
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
import java.util.List;

public class ConstructManager extends DimensionInstancingManager
{
	public ConstructManager(@Nonnull List<ResourceKey<Level>> instanceDimensions,
							@Nonnull ResourceKey<Level> fallbackDim,
							@Nonnull BlockPos fallbackPos)
	{
		super(instanceDimensions, fallbackDim, fallbackPos);
	}
	public void serverTick()
	{
		super.serverTick();
	}

	@Nonnull
	public OpResult saveChangesInInstance(@Nonnull ResourceKey<Level> dimension, @Nullable String saveAsMapName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		String mapName = getMapLoadedIn(dimension);
		if(mapName == null)
			return OpResult.FAILURE_MAP_NOT_FOUND;

		String saveName = saveAsMapName != null ? saveAsMapName : mapName;

		for(Instance instance : instances)
		{
			if(mapName.equals(instance.currentMap))
				return instance.tryStartSave(server, this::saveConstruct, saveName);
		}
		return OpResult.FAILURE_MAP_NOT_FOUND;
	}
	private boolean saveConstruct(@Nonnull Instance instance)
	{
		String mapName = instance.currentMap;

		try
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			File serverDir = server.getServerDirectory();
			Path levelRoot = server.getWorldPath(LevelResource.ROOT);
			Path dimRoot = DimensionType.getStorageFolder(instance.dimension, levelRoot);

			File dstDir = new File(serverDir.getPath() + "/teams_maps/" + mapName);
			File srcDir = dimRoot.toFile();

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
								TeamsMod.LOGGER.error("[saveConstruct] Region file '" + regionFile + "' could not be deleted");
								for(var listener : listeners)
									listener.sendFailure(Component.translatable("teams.construct.save.failure_io_error"));
								return false;
							}
						}
					}
				}
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
						TeamsMod.LOGGER.error("[saveConstruct] Regions folder does not exist at '"+srcRegionsDir+"'");
						for(var listener : listeners)
							listener.sendFailure(Component.translatable("teams.construct.save.failure_bad_level"));
						return false;
					}

					instance.runningTask = null;

					TeamsMod.LOGGER.info("[saveConstruct] Successfully copied '"+srcDir+"' to '"+dstDir+"'");
					for(var listener : listeners)
						listener.sendSuccess(() -> Component.translatable("teams.construct.save.success", mapName), true);
					return true;
				}
				else
				{
					TeamsMod.LOGGER.error("[saveConstruct] Level directory does not exist at '"+srcDir+"'");
					for(var listener : listeners)
						listener.sendFailure(Component.translatable("teams.construct.save.failure_bad_level"));
					return false;
				}
			}
			else
			{
				TeamsMod.LOGGER.error("[saveConstruct] Target level directory does not exist at '"+dstDir+"'");
				for(var listener : listeners)
					listener.sendFailure(Component.translatable("teams.construct.save.failure_bad_level"));
				return false;
			}
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
			for(var listener : listeners)
				listener.sendFailure(Component.literal(e.toString()));
		}
		return false;
	}




	// Slight out-of-scope, this is not going via the construct dimension
	public boolean beginSaveChunksToLevel(@Nonnull ResourceKey<Level> srcDimension,
										  @Nonnull ChunkPos min,
										  @Nonnull ChunkPos max,
										  @Nonnull String levelName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		server.submit(() -> cloneRegions(srcDimension, min, max, levelName));
		return true;
	}

	private boolean cloneRegions(@Nonnull ResourceKey<Level> srcDimension,
								 @Nonnull ChunkPos min,
								 @Nonnull ChunkPos max,
								 @Nonnull String levelID)
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
								TeamsMod.LOGGER.warn("[cloneRegions] Chunk Clone at ["+(completedCount+skippedCount)+"/"+regionCount+"] (Skipped "+skippedCount+")");
								for(var listener : listeners)
									listener.sendSystemMessage(Component.translatable("teams.construct.clone.progress", completedCount+skippedCount, regionCount, skippedCount));
							}


						}
					}

					TeamsMod.LOGGER.warn("[cloneRegions] Level cloned from '"+srcDir+"' to '"+dstDir+"'");
					for(var listener : listeners)
						listener.sendSuccess(() -> Component.translatable("teams.construct.clone.success", levelID), true);
					return true;
				}
				else
				{
					TeamsMod.LOGGER.warn("[cloneRegions] Level for clone has no region files '"+srcRegionsDir+"'");
					for(var listener : listeners)
						listener.sendFailure(Component.translatable("teams.construct.clone.failure_bad_level"));
				}
			}
			else
			{
				TeamsMod.LOGGER.warn("[cloneRegions] Level directory for "+srcDimension+" missing '"+srcDir+"'");
				for(var listener : listeners)
					listener.sendFailure(Component.translatable("teams.construct.clone.failure_bad_level"));
			}
		}
		catch(IOException e)
		{
			TeamsMod.LOGGER.error(e.toString());
			for(var listener : listeners)
				listener.sendFailure(Component.literal(e.toString()));
		}

		return false;
	}




	// --------------------------------------------------------------------------------------------------------------
	// Construct
	//@Nonnull
	//public IMapDetails summariseConstructContents() { return constructMapDetails; }
	//public void onLoadConstruct(@Nonnull IMapDetails constructMap)
	//{
	//	constructMapDetails = new MapDetails(constructMap);
	//}

}
