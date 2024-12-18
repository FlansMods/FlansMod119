package com.flansmod.teams.server.command;

import com.flansmod.teams.api.admin.IPlayerBuilderSettings;
import com.flansmod.teams.api.admin.IPlayerPersistentInfo;
import com.flansmod.teams.api.admin.MapInfo;
import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.dimension.DimensionInstancingManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

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
					.then(Commands.argument("mapName", StringArgumentType.word())
						.executes(CommandConstruct::loadMapIntoConstruct)
					)
				)
				.then(Commands.literal("save").executes(CommandConstruct::saveConstruct))
				.then(Commands.literal("clone")
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
					.then(Commands.argument("dimension", DimensionArgument.dimension()))
				)
			);
	}

	private static int listInstances(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		for(String info : dimManager.getInfo())
		{
			context.getSource().sendSuccess(() -> Component.literal(info), true);
		}
		return -1;
	}
	private static int saveConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		String levelID = dimManager.getLoadedLevel(0);

		if(levelID == null)
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.save.failure_construct_empty"));
			return -1;
		}

		if(dimManager.saveChangesInInstance(0, context.getSource()))
			context.getSource().sendSuccess(() -> Component.translatable("teams.construct.save.started"), true);
		return -1;
	}
	private static int enterConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		String levelID = dimManager.getLoadedLevel(0);

		if(levelID == null)
		{
			context.getSource().sendFailure(Component.translatable("teams.construct.enter.failure_construct_empty"));
			return -1;
		}

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
					if(buildSettings != null)
					{
						BlockPos pos = buildSettings.getLastPositionInConstruct(levelID);
						if(pos != null)
						{
							x = pos.getX();
							y = pos.getY();
							z = pos.getZ();
						}
					}
				}

				boolean enterSuccess = dimManager.enterInstance(context.getSource().getPlayer(), 0, x, y, z);
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
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		ResourceKey<Level> constructDimension = dimManager.getDimension(0);
		if(context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if(player != null && player.level().dimension().equals(constructDimension))
			{
				dimManager.exitInstance(player);
				context.getSource().sendSuccess(() -> Component.translatable("teams.construct.exit.success"), true);
				return -1;
			}
		}

		context.getSource().sendSuccess(() -> Component.translatable("teams.construct.exit.failure"), true);
		return -1;
	}

	private static int cloneAreaIntoConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		String mapName = StringArgumentType.getString(context, "mapName");

		ResourceKey<Level> sourceDimension =  context.getSource().getEntity() != null ? context.getSource().getEntity().level().dimension() : Level.OVERWORLD;
		BlockPos defaultPos = context.getSource().getEntity() != null ? context.getSource().getEntity().blockPosition() : BlockPos.ZERO;
		BlockPos min = tryGetBlockPos(context, "min", defaultPos);
		BlockPos max = tryGetBlockPos(context, "max", defaultPos);
		int chunkRadius = tryGetInt(context, "chunkRadius", -1);

		MapInfo existingMap = TeamsMod.MANAGER.getMapData(mapName);
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

		dimManager.beginSaveChunksToLevel(sourceDimension, minChunk, maxChunk, mapName, context.getSource());
		context.getSource().sendSuccess(() -> Component.translatable("teams.construct.clone.started", mapName), true);

		return -1;
	}
	private static int loadMapIntoConstruct(@Nonnull CommandContext<CommandSourceStack> context)
	{
		DimensionInstancingManager dimManager = TeamsMod.MANAGER.getConstructs();
		int constructIndex = tryGetInt(context, "index", 0);
		String mapName = StringArgumentType.getString(context, "mapName");

		boolean success = dimManager.beginLoadLevel(constructIndex, mapName, context.getSource());
		if(success)
			context.getSource().sendSuccess(() -> Component.translatable("teams.construct.load.started", mapName), true);

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
	@Nonnull
	private static BlockPos tryGetBlockPos(@Nonnull CommandContext<CommandSourceStack> ctx, @Nonnull String name, @Nonnull BlockPos defaultValue)
	{
		try { return BlockPosArgument.getBlockPos(ctx, name); }
		catch(Exception ignored) { return defaultValue; }
	}
}
