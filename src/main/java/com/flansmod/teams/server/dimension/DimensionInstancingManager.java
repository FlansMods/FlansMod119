package com.flansmod.teams.server.dimension;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.runtime.IDimensionInstancer;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class DimensionInstancingManager implements IDimensionInstancer
{
	public static final int INVALID_INSTANCE = -1;
	protected final List<CommandSourceStack> listeners = new ArrayList<>();
	protected final List<Instance> instances;
	protected final ResourceKey<Level> fallbackDimension;
	protected final BlockPos fallbackSpawn;

	protected static class Instance
	{
		private enum State
		{
			Unloaded,
			LoadingMap,
			Loaded,
			SavingMap,
			UnloadingMap,
		}

		public final ResourceKey<Level> dimension;
		private AtomicReference<State> currentState = new AtomicReference<>(State.Unloaded);
		private int ticksInState = 0;
		public String currentMap = null;
		public String loadingMap = null;
		public CompletableFuture<Boolean> runningTask = null;
		public Instance(@Nonnull ResourceKey<Level> dim)
		{
			dimension = dim;
		}
		private void switchState(@Nonnull State newState)
		{
			currentState.set(newState);
			ticksInState = 0;
		}
		public boolean isLoaded() { return currentMap != null; }
		public boolean isLoadedWithMap(@Nonnull String mapName)
		{
			return mapName.equals(currentMap);
		}
		@Nonnull
		public OpResult tryStartLoad(@Nonnull MinecraftServer server,
									@Nonnull Function<Instance, Boolean> loadTask,
									@Nonnull String mapName)
		{
			if(currentState.weakCompareAndSetVolatile(State.Unloaded, State.LoadingMap))
			{
				loadingMap = mapName;
				runningTask = server.submit(() -> loadTask.apply(this));
				return OpResult.SUCCESS;
			}
			return OpResult.FAILURE_WRONG_STATE;
		}
		@Nonnull
		public OpResult tryStartUnload(@Nonnull MinecraftServer server,
									  @Nonnull Function<Instance, Boolean> unloadTask,
									  @Nonnull String mapName)
		{
			if(currentState.weakCompareAndSetVolatile(State.Loaded, State.UnloadingMap))
			{
				runningTask = server.submit(() -> unloadTask.apply(this));
				return OpResult.SUCCESS;
			}
			return OpResult.FAILURE_WRONG_STATE;
		}
		@Nonnull
		public OpResult tryStartSave(@Nonnull MinecraftServer server,
									 @Nonnull Function<Instance, Boolean> saveTask,
									 @Nonnull String saveAsMapName)
		{
			if(currentState.weakCompareAndSetVolatile(State.Loaded, State.SavingMap))
			{
				runningTask = server.submit(() -> saveTask.apply(this));
				return OpResult.SUCCESS;
			}
			return OpResult.FAILURE_WRONG_STATE;
		}
		public void serverTick(@Nonnull MinecraftServer server)
		{
			ticksInState++;
			switch(currentState.get())
			{
				case LoadingMap ->
				{
					if(runningTask != null && runningTask.isDone())
					{
						ServerLevel level = server.getLevel(dimension);
						if(level != null)
							level.getChunkSource().tick(() -> true, false);
						runningTask = null;
						currentMap = loadingMap;
						loadingMap = null;
						onLoadComplete();
						switchState(State.Loaded);
					}
				}
				case SavingMap ->
				{
					if(runningTask != null && runningTask.isDone())
					{
						runningTask = null;
						switchState(State.Loaded);
					}
				}
				case UnloadingMap ->
				{
					if(runningTask != null && runningTask.isDone())
					{
						runningTask = null;
						currentMap = null;
						switchState(State.Unloaded);
					}
				}
			}
		}
		protected void onLoadComplete() {}

		@Override
		public String toString()
		{
			return "["+dimension+"]="+currentMap+" ("+currentState+" for "+ticksInState+" ticks)";
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
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		for(Instance instance : instances)
		{
			instance.serverTick(server);
		}
	}

	public void registerListener(@Nonnull CommandSourceStack listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);
	}
	public void removeListener(@Nonnull CommandSourceStack listener)
	{
		if(listeners.contains(listener))
			listeners.remove(listener);
	}

	@Override @Nonnull
	public OpResult beginLoad(@Nonnull String mapName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(isLoaded(mapName))
			return OpResult.FAILURE_ALREADY_COMPLETE;

		for(Instance instance : instances)
		{
			if(instance.tryStartLoad(server, this::load, mapName).success())
				return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_NO_INSTANCES_AVAILABLE;
	}
	@Override
	public boolean isLoaded(@Nonnull String mapName)
	{
		for(Instance instance : instances)
			if(instance.isLoadedWithMap(mapName))
				return true;
		return false;
	}
	@Override @Nullable
	public ResourceKey<Level> dimensionOf(@Nonnull String mapName)
	{
		for(Instance instance : instances)
			if(instance.isLoadedWithMap(mapName))
				return instance.dimension;
		return null;
	}
	@Override @Nullable
	public String getMapLoadedIn(@Nonnull ResourceKey<Level> dimension)
	{
		for(Instance instance : instances)
			if(instance.dimension.equals(dimension))
				return instance.currentMap;
		return null;
	}
	@Override @Nonnull
	public OpResult beginUnload(@Nonnull String mapName)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		for(Instance instance : instances)
		{
			if(mapName.equals(instance.currentMap))
				return instance.tryStartUnload(server, this::unload, mapName);
		}
		return OpResult.FAILURE_ALREADY_COMPLETE;
	}
	@Override @Nonnull
	public List<String> printDebug()
	{
		List<String> list = new ArrayList<>(instances.size());
		for(int i = 0; i < instances.size(); i++)
		{
			Instance instance = instances.get(i);
			list.add(i+": "+instance);
		}
		return list;
	}
	@Override
	public boolean managesDimension(@Nonnull ResourceKey<Level> dimension)
	{
		for(Instance instance : instances)
			if(instance.dimension.equals(dimension))
				return true;
		return false;
	}
	public boolean enterInstance(@Nonnull ServerPlayer player, @Nullable String mapName, double x, double y, double z)
	{
		Instance target = null;
		for(Instance instance : instances)
		{
			if(mapName == null)
			{
				if(instance.isLoaded())
				{
					target = instance;
					break;
				}
			}
			else if(mapName.equals(instance.currentMap))
			{
				target = instance;
			}
		}

		if(target != null)
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerLevel level = server.getLevel(target.dimension);
			if(level != null)
			{
				player.teleportTo(level, x, y, z, 0f, 0f);
				return true;
			}
		}

		return false;
	}
	public boolean exitInstance(@Nonnull ServerPlayer player)
	{
		player.respawn();
		return true;
	}





	protected boolean load(@Nonnull Instance instance)
	{
		kickAllPlayersFrom(instance.dimension, fallbackDimension, fallbackSpawn);
		closeInstance(instance.dimension);

		String mapName = instance.loadingMap;
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
								TeamsMod.LOGGER.error("[load] Region file '" + regionFile + "' could not be deleted");
								for(var listener : listeners)
									listener.sendFailure(Component.translatable("teams.construct.load.failure_io_error"));
								return false;
							}
						}
					}
				}

				File srcDir = new File(serverDir.getPath() + "/teams_maps/" + mapName);
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
						TeamsMod.LOGGER.error("[load] Regions folder does not exist at '"+srcRegionsDir+"'");
						for(var listener : listeners)
							listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
						return false;
					}

					TeamsMod.LOGGER.info("[load] Successfully copied '"+srcDir+"' to '"+dstDir+"'");
					for(var listener : listeners)
						listener.sendSuccess(() -> Component.translatable("teams.construct.load.success", mapName), true);
					return true;
				}
				else
				{
					TeamsMod.LOGGER.error("[load] Level directory does not exist at '"+srcDir+"'");
					for(var listener : listeners)
						listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
					return false;
				}
			}
			else
			{
				TeamsMod.LOGGER.error("[load] Target level directory does not exist at '"+dstDir+"'");
				for(var listener : listeners)
					listener.sendFailure(Component.translatable("teams.construct.load.failure_bad_level"));
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
	protected boolean unload(@Nonnull Instance instance)
	{
		closeInstance(instance.dimension);
		return true;
	}
	protected void kickAllPlayersFrom(@Nonnull ResourceKey<Level> dimension,
									  @Nonnull ResourceKey<Level> fallbackDimension,
									  @Nonnull BlockPos fallbackPos)
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
			for(var listener : listeners)
				listener.sendSystemMessage(Component.translatable("teams.moved_players.success", playersToTeleport.size()));
		}
		else
		{
			TeamsMod.LOGGER.warn("Could not kick all players from "+dimension+" to "+fallbackDimension);
			for(var listener : listeners)
				listener.sendFailure(Component.translatable("teams.moved_players.failure"));
		}
	}
	protected void closeInstance(@Nonnull ResourceKey<Level> dimension)
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
}
