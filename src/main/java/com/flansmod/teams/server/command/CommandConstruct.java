package com.flansmod.teams.server.command;

import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.IPlayerBuilderSettings;
import com.flansmod.teams.api.admin.IPlayerPersistentInfo;
import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.server.dimension.ConstructManager;
import com.flansmod.teams.server.dimension.DimensionInstancingManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandConstruct
{
	public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatch,
								@Nonnull CommandBuildContext context)
	{
		dispatch.register(
			Commands.literal("construct")
				.requires(player -> player.hasPermission(2))
				.then(Commands.literal("list").executes(CommandConstruct::listInstances))
				.then(Commands.literal("enter").executes(CommandConstruct::enterConstruct)
					//.then(Commands.argument("index", IntegerArgumentType.integer(0))
					.executes(CommandConstruct::enterConstruct)
					.then(Commands.argument("x", DoubleArgumentType.doubleArg())
						.then(Commands.argument("y", DoubleArgumentType.doubleArg())
							.then(Commands.argument("z", DoubleArgumentType.doubleArg())
								.executes(CommandConstruct::enterConstruct)
							)
						)
					)
				)
				.then(Commands.literal("exit").executes(CommandConstruct::exitConstruct))
				.then(Commands.literal("load")
					.then(Commands.argument("mapName", MapArgument.mapArgument())
						.executes(CommandConstruct::loadMapIntoConstruct)
					)
				)
				.then(Commands.literal("save").executes(CommandConstruct::saveConstruct))
				.then(Commands.literal("saveAs")
					.then(Commands.argument("mapName", StringArgumentType.word())
						.executes(CommandConstruct::saveAsConstruct)
					)
				)
				.then(Commands.literal("close").executes(CommandConstruct::closeConstruct)
					.then(Commands.argument("mapName", MapArgument.mapArgument())
						.executes(CommandConstruct::closeConstruct)
					)
				)
				.then(Commands.literal("cloneChunks")
					.then(Commands.argument("mapName", StringArgumentType.word())
						.then(Commands.argument("min", BlockPosArgument.blockPos())
							.then(Commands.argument("max", BlockPosArgument.blockPos())
								.executes(CommandConstruct::cloneAreaIntoConstruct)
							)
						)
						.then(Commands.argument("chunkRadius", IntegerArgumentType.integer(0))
							.executes(CommandConstruct::cloneAreaIntoConstruct)
						)
					)
				)
				.then(Commands.literal("create")
					.then(Commands.argument("mapName", StringArgumentType.word())
						.executes(CommandConstruct::createMap)
						//.then(Commands.argument("genType", StringArgumentType.word())
						//	.executes(CommandConstruct::createMap))
					)
				)
				.then(Commands.literal("link")
					.then(Commands.literal("auto").executes(CommandConstruct::autoLink))
				)
				.then(Commands.literal("validate")
					.executes(CommandConstruct::validateConstruct)
					.then(Commands.argument("gamemode", GamemodeArgument.gamemodeArgument())
						.executes(CommandConstruct::validateConstruct)
					)
				)
			);
	}

	private static int listInstances(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		for(String info : dimManager.printDebug())
		{
			context.getSource().sendSuccess(() -> Component.literal(info), true);
		}
		return -1;
	}
	private static int createMap(@Nonnull CommandContext<CommandSourceStack> context)
	{
		String mapName = tryGetString(context, "mapName", null);
		if(mapName == null)
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.create.failure_map_name_invalid", "null"));
			return -1;
		}
		if(!TeamsAPI.isValidMapName(mapName))
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.create.failure_map_name_invalid", mapName));
			return -1;
		}

		if(TeamsMod.MANAGER.createMap(mapName).success())
			context.getSource().sendSuccess(() -> Component.translatable("teams.construct.create.success"), true);
		else
			context.getSource().sendFailure(Component.translatable("teams.construct.create.failure"));


		return -1;
	}
	private static int validateConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		String gamemodeID = tryGetString(context, "gamemode", null);
		if(gamemodeID != null)
		{
			//TeamsMod.MANAGER.validate
		}

		return -1;
	}
	private static int saveConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		constructManager.registerListener(context.getSource());

		OpResult saveResult = constructManager.saveChangesInInstance(context.getSource().getLevel().dimension(), null);
		switch(saveResult)
		{
			case SUCCESS -> context.getSource().sendSuccess(() -> Component.translatable("teams.construct.save.started"), true);
			case FAILURE_MAP_NOT_FOUND -> context.getSource().sendFailure(Component.translatable("teams.construct.save.failure_construct_empty"));
			default -> context.getSource().sendFailure(Component.translatable("teams.construct.save.failure"));
		}

		return -1;
	}
	private static int saveAsConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		constructManager.registerListener(context.getSource());

		String mapName = tryGetString(context, "mapName", null);
		if(mapName == null || !TeamsAPI.isValidMapName(mapName))
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.save_as.failure_invalid_name"));
			return -1;
		}

		OpResult saveResult = constructManager.saveChangesInInstance(context.getSource().getLevel().dimension(), mapName);
		switch(saveResult)
		{
			case SUCCESS -> context.getSource().sendSuccess(() -> Component.translatable("teams.construct.save.started"), true);
			case FAILURE_MAP_NOT_FOUND -> context.getSource().sendFailure(Component.translatable("teams.construct.save.failure_construct_empty"));
			default -> context.getSource().sendFailure(Component.translatable("teams.construct.save.failure"));
		}

		return -1;
	}
	private static int enterConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		constructManager.registerListener(context.getSource());

		String mapName = tryGetString(context, "mapName", null);
		if(context.getSource().isPlayer())
		{
			//int constructIndex = tryGetInt(context, "index", 0);
			ServerPlayer player = context.getSource().getPlayer();
			if(player != null)
			{
				double x = tryGetDouble(context, "x", Double.NaN);
				double y = tryGetDouble(context, "y", Double.NaN);
				double z = tryGetDouble(context, "z", Double.NaN);

				if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z))
				{
					x = 0d;
					y = 0d;
					z = 0d;

					// Step 1: Did we save an exit point?
					IPlayerPersistentInfo playerData = TeamsMod.MANAGER.getPlayerData(player.getUUID());
					IPlayerBuilderSettings buildSettings = playerData != null ? playerData.getBuilderSettings() : null;
					if(buildSettings != null && mapName != null)
					{
						BlockPos pos = buildSettings.getLastPositionInConstruct(mapName);
						if(pos != null)
						{
							x = pos.getX();
							y = pos.getY();
							z = pos.getZ();
						}
					}
				}

				boolean enterSuccess = constructManager.enterInstance(context.getSource().getPlayer(), mapName, x, y, z);
				if(enterSuccess)
					context.getSource().sendSuccess(() -> Component.translatable("teams.construct.enter.success"), true);
				else
					context.getSource().sendFailure(Component.translatable("teams.construct.enter.tp_failed"));
			}
			else
				context.getSource().sendFailure(Component.translatable("teams.construct.enter.no_player"));
		}
		else
			context.getSource().sendFailure(Component.translatable("teams.construct.enter.no_player"));

		return -1;
	}
	private static int exitConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		if(context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if(player != null && constructManager.managesDimension(player.level().dimension()))
			{
				constructManager.exitInstance(player);
				context.getSource().sendSuccess(() -> Component.translatable("teams.construct.exit.success"), true);
				return -1;
			}
		}

		context.getSource().sendSuccess(() -> Component.translatable("teams.construct.exit.failure"), true);
		return -1;
	}

	private static int cloneAreaIntoConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		String mapName = StringArgumentType.getString(context, "mapName");

		ResourceKey<Level> sourceDimension =  context.getSource().getEntity() != null ? context.getSource().getEntity().level().dimension() : Level.OVERWORLD;
		BlockPos defaultPos = context.getSource().getEntity() != null ? context.getSource().getEntity().blockPosition() : BlockPos.ZERO;
		BlockPos min = tryGetBlockPos(context, "min", defaultPos);
		BlockPos max = tryGetBlockPos(context, "max", defaultPos);
		int chunkRadius = tryGetInt(context, "chunkRadius", -1);

		IMapDetails existingMap = TeamsMod.MANAGER.getMapData(mapName);
		if(existingMap != null)
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.clone.conflict", mapName));
			return -1;
		}

		OpResult createMapResult = TeamsMod.MANAGER.createMap(mapName);
		if(createMapResult.failure())
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.clone.create_fail", mapName));
			return -1;
		}

		ChunkPos minChunk = new ChunkPos(min);
		ChunkPos maxChunk = new ChunkPos(max);
		if(chunkRadius != -1)
		{
			ChunkPos centerChunk = new ChunkPos(defaultPos);
			minChunk = new ChunkPos(centerChunk.x - chunkRadius, centerChunk.z - chunkRadius);
			maxChunk = new ChunkPos(centerChunk.x + chunkRadius, centerChunk.z + chunkRadius);
		}

		constructManager.beginSaveChunksToLevel(sourceDimension, minChunk, maxChunk, mapName);
		context.getSource().sendSuccess(() -> Component.translatable("teams.construct.clone.started", mapName), true);

		return -1;
	}
	private static int loadMapIntoConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		String mapName = StringArgumentType.getString(context, "mapName");

		OpResult loadResult = constructManager.beginLoad(mapName);
		switch(loadResult)
		{
			case SUCCESS -> context.getSource().sendSuccess(() -> Component.translatable("teams.construct.load.started", mapName), true);
			case FAILURE_ALREADY_COMPLETE -> context.getSource().sendFailure(Component.translatable("teams.construct.load.map_already_loaded"));
			case FAILURE_NO_INSTANCES_AVAILABLE -> context.getSource().sendFailure(Component.translatable("teams.construct.load.failure_instance_not_empty"));
			default -> context.getSource().sendFailure(Component.translatable("teams.construct.load.failure", mapName));
		}
		return -1;
	}
	private static int closeConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		String mapName = tryGetString(context, "mapName", null);
		if(mapName == null)
			mapName = constructManager.getMapLoadedIn(context.getSource().getLevel().dimension());

		if(mapName != null)
		{
			final String mapToClose = mapName;
			OpResult closeResult = constructManager.beginUnload(mapToClose);
			switch (closeResult)
			{
				case SUCCESS -> context.getSource().sendSuccess(() -> Component.translatable("teams.construct.close.started", mapToClose), true);
				default -> context.getSource().sendFailure(Component.translatable("teams.construct.close.failure", mapToClose));
			}
		}
		return -1;
	}
	private static int autoLink(@Nonnull CommandContext<CommandSourceStack> context)
	{
		ConstructManager constructManager = TeamsMod.MANAGER.getConstructs();
		if(context.getSource().isPlayer())
		{
			ResourceKey<Level> dimension = context.getSource().getLevel().dimension();

		}

		return -1;
	}


	private static int tryGetInt(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String name, int defaultValue)
	{
		try { return IntegerArgumentType.getInteger(ctx, name); }
		catch(Exception ignored) { return defaultValue; }
	}
	private static double tryGetDouble(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String name, double defaultValue)
	{
		try { return DoubleArgumentType.getDouble(ctx, name); }
		catch(Exception ignored) { return defaultValue; }
	}
	@Nullable
	private static String tryGetString(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String name, @Nullable String defaultValue)
	{
		try { return StringArgumentType.getString(ctx, name); }
		catch(Exception ignored) { return defaultValue; }
	}
	@Nonnull
	private static BlockPos tryGetBlockPos(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String name, @Nonnull BlockPos defaultValue)
	{
		try { return BlockPosArgument.getBlockPos(ctx, name); }
		catch(Exception ignored) { return defaultValue; }
	}
}
