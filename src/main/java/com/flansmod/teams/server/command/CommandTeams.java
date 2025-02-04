package com.flansmod.teams.server.command;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.IPlayerPersistentInfo;
import com.flansmod.teams.api.admin.ITeamsAdmin;
import com.flansmod.teams.api.admin.RoundInfo;
import com.flansmod.teams.api.runtime.ITeamsRuntime;
import com.flansmod.teams.common.TeamsMod;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommandTeams
{
	public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatch,
								@Nonnull CommandBuildContext context)
	{
		dispatch.register(
			Commands.literal("teams")
				.requires((player) -> player.hasPermission(2))
				.then(Commands.literal("start").executes(ctx -> start(ctx.getSource())))
				.then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())))
				.then(Commands.literal("getState").executes(ctx -> getCurrentState(ctx.getSource())))

				.then(Commands.literal("listMaps").executes(ctx -> listMaps(ctx.getSource())))
				.then(Commands.literal("nextMap").executes(ctx -> goToNextMap(ctx.getSource())))
				//.then(Commands.literal("setNextRound")
				//	.then(Commands.argument("index", IntegerArgumentType.integer()))
				//		.executes(ctx -> setNextMap(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))
				//)
				.then(Commands.literal("getNextMap").executes(ctx -> getNextMap(ctx.getSource())))
				.then(Commands.literal("getCurrentMap").executes(ctx -> getCurrentMap(ctx.getSource())))

				.then(Commands.literal("rotation")
					.then(Commands.literal("list").executes(ctx -> listRotation(ctx.getSource())))
					.then(Commands.literal("enable").executes(ctx -> enableRotation(ctx.getSource())))
					.then(Commands.literal("disable").executes(ctx -> disableRotation(ctx.getSource())))
					.then(Commands.literal("add")
						.then(Commands.argument("mapName", MapArgument.mapArgument())
							.then(Commands.argument("gamemode", GamemodeArgument.gamemodeArgument())
								.then(Commands.argument("team1", TeamArgument.teamArgument())
									.then(Commands.argument("team2", TeamArgument.teamArgument())
										.executes(CommandTeams::addMapToRotation)
										.then(Commands.argument("atPosition", IntegerArgumentType.integer(0, 999))
											.executes(CommandTeams::addMapToRotation)
										)
									)
								)
							)
						)
					)
					.then(Commands.literal("remove")
						.then(Commands.argument("index", IntegerArgumentType.integer())
							.executes(ctx -> removeMapFromRotation(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))
						)
					)
				)
				.then(Commands.literal("builders")
					.then(Commands.literal("add")
						.then(Commands.argument("targets", GameProfileArgument.gameProfile())
							.executes(CommandTeams::addPlayerToBuilders)
						)
					)
					.then(Commands.literal("remove")
						.then(Commands.argument("targets", GameProfileArgument.gameProfile())
							.executes(CommandTeams::removePlayerFromBuilders)
						)
					)
				)
				.then(Commands.literal("instances")
					.then(Commands.literal("list")
						.executes(ctx -> listInstances(ctx.getSource()))
					)
				)
				//.then(Commands.literal("settings")
				//	.then(Commands.argument("settingsName", StringArgumentType.word())
				//		.then())
				//	.then(Commands.literal("default").executes(ctx -> selectSettings(ctx.getSource(), ISettings.DEFAULT_KEY)))
				//	.then(Commands.literal("select")
				//		.then(Commands.argument("settingsName", StringArgumentType.word())
				//			.executes(ctx -> selectSettings(ctx.getSource(), StringArgumentType.getString(ctx, "settingsName")))
				//		)
				//	)
				//)
			);
	}

	private static int start(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, ITeamsRuntime::start,
			() -> Component.translatable("teams.command.start.success"),
			(errorType) -> switch(errorType)
			{
				case FAILURE_MAP_ROTATION_EMPTY -> Component.translatable("teams.command.start.failure_map_rotation_empty");
				case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.start.failure_invalid_map_name");
				case FAILURE_INVALID_GAMEMODE_ID -> Component.translatable("teams.command.start.failure_invalid_gamemode_id");
				case FAILURE_INVALID_TEAM_ID -> Component.translatable("teams.command.start.failure_invalid_team_id");

				default -> Component.translatable("teams.command.start.failure");
			});
	}
	private static int stop(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, ITeamsRuntime::stop,
			() -> Component.translatable("teams.command.stop.success"),
			(errorType) -> Component.translatable("teams.command.stop.failure"));
	}
	private static int enableRotation(@Nonnull CommandSourceStack source)
	{
		return adminFunc(source, ITeamsAdmin::enableMapRotation,
				() -> Component.translatable("teams.command.enable_rotation.success"),
				(errorType) -> Component.translatable("teams.command.enable_rotation.failure"));
	}
	private static int disableRotation(@Nonnull CommandSourceStack source)
	{
		return adminFunc(source, ITeamsAdmin::disableMapRotation,
				() -> Component.translatable("teams.command.disable_rotation.success"),
				(errorType) -> Component.translatable("teams.command.disable_rotation.failure"));
	}
	private static int listRotation(@Nonnull CommandSourceStack source)
	{
		return adminFunc(source, (src, admin) -> {
			for(RoundInfo round : admin.getMapRotation())
			{
				src.sendSystemMessage(Component.literal(round.toString()));
			}
			src.sendSystemMessage(Component.translatable("teams.command.listRotation.count", admin.getMapRotation().size()));
		});
	}
	private static int listMaps(@Nonnull CommandSourceStack source)
	{
		return adminFunc(source, (src, admin) -> {
			for(String map : admin.getAllMaps())
			{
				src.sendSystemMessage(Component.literal(map));
			}
			src.sendSystemMessage(Component.translatable("teams.command.listMaps.count", admin.getAllMaps().size()));
		});
	}
	private static int addPlayerToBuilders(@Nonnull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException
	{
		Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "targets");
		for(GameProfile profile : profiles)
		{
			IPlayerPersistentInfo playerInfo = TeamsMod.MANAGER.getOrCreatePlayerData(profile.getId());
			playerInfo.setBuilder(true);
			ctx.getSource().sendSuccess(() -> Component.translatable("teams.command.add_builder.success", profile.getName()), true);
		}
		return -1;
	}
	private static int removePlayerFromBuilders(@Nonnull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException
	{
		Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "targets");
		for(GameProfile profile : profiles)
		{
			IPlayerPersistentInfo playerInfo = TeamsMod.MANAGER.getOrCreatePlayerData(profile.getId());
			playerInfo.setBuilder(false);
			ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(profile.getId());
			if(player != null)
				TeamsMod.MANAGER.onPlayerLogin(player);
			ctx.getSource().sendSuccess(() -> Component.translatable("teams.command.remove_builder.success", profile.getName()), true);
		}
		return -1;
	}
	private static int addMapToRotation(@Nonnull CommandContext<CommandSourceStack> ctx)
	{
		String mapName = tryGetString(ctx, "mapName", null);
		String gamemodeString = tryGetString(ctx, "gamemode", "TDM");
		ResourceLocation gamemodeID = gamemodeString == null ? null : ResourceLocation.tryParse(gamemodeString);
		String team1Name = tryGetString(ctx, "team1", null);
		String team2Name = tryGetString(ctx, "team2", null);
		int positionHint = tryGetInt(ctx, "atPosition", -1);

		if(mapName == null || gamemodeID == null || team1Name == null || team2Name == null)
		{
			ctx.getSource().sendFailure(Component.translatable("teams.command.rotation_add_map.failure"));
			return -1;
		}

		return adminFunc(ctx.getSource(), (admin) ->
			{
				RoundInfo roundInfo = admin.tryCreateRoundInfo(mapName, gamemodeID, team1Name, team2Name);
				if(roundInfo != null)
					return admin.addMapToRotation(roundInfo, positionHint);

				return OpResult.FAILURE_GENERIC;
			},
			() -> Component.translatable("teams.command.rotation_add_map.success", mapName),
			(errorType) -> switch(errorType)
				{
					case FAILURE_INVALID_MAP_INDEX -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_index", mapName, positionHint);
					case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_name", mapName);
					default -> Component.translatable("teams.command.rotation_add_map.failure", mapName);
				});
	}
	private static int addMapToRotation(@Nonnull CommandSourceStack source, @Nonnull RoundInfo round, int positionHint)
	{
		return adminFunc(source, (admin) -> admin.addMapToRotation(round, positionHint),
				() -> Component.translatable("teams.command.rotation_add_map.success", round),
				(errorType) -> switch(errorType)
				{
					case FAILURE_INVALID_MAP_INDEX -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_index", round.mapName(), positionHint);
					case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_name", round.mapName());
					default -> Component.translatable("teams.command.rotation_add_map.failure", round.mapName());
				});
	}
	private static int removeMapFromRotation(@Nonnull CommandSourceStack source, int index)
	{
		return adminFunc(source, (admin) -> admin.removeMapFromRotation(index),
				() -> Component.translatable("teams.command.rotation_remove_map.success", index),
				(errorType) -> switch(errorType)
				{
					case FAILURE_MAP_NOT_FOUND -> Component.translatable("teams.command.rotation_remove_map.failure.map_not_found", index);
					case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.rotation_remove_map.failure.invalid_map_name", index);
					default -> Component.translatable("teams.command.rotation_remove_map.failure", index);
				});
	}
	private static int listInstances(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, (src, runtime) -> {
			for(String info : runtime.getDimensionInfo())
			{
				src.sendSuccess(() -> Component.literal(info), true);
			}
		});
	}
	private static int getCurrentMap(@Nonnull CommandSourceStack source)
	{
		return runtimeGetFunc(source,
				ITeamsRuntime::getCurrentMapName,
				TeamsAPI::validateMapName,
				(result) -> Component.translatable("teams.command.get_current_map.success", result),
				(errorType, result) -> Component.translatable("teams.command.get_current_map.failure"));
	}
	private static int getNextMap(@Nonnull CommandSourceStack source)
	{
		return runtimeGetFunc(source,
				ITeamsRuntime::getNextMapName,
				TeamsAPI::validateMapName,
				(result) -> Component.translatable("teams.command.get_next_map.success", result),
				(errorType, result) -> Component.translatable("teams.command.get_next_map.failure"));
	}
	//private static int setNextMap(@Nonnull CommandSourceStack source, int roundIndex)
	//{
	//	return runtimeFunc(source, (runtime) -> runtime.setNextRound(roundIndex),
	//			() -> Component.translatable("teams.command.set_next_map.success", mapName),
	//			(errorType) -> Component.translatable("teams.command.set_next_map.failure", mapName));
	//}
	private static int goToNextMap(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, ITeamsRuntime::goToNextRound,
					() -> Component.translatable("teams.command.go_to_next_map.success"),
					(errorType) -> Component.translatable("teams.command.go_to_next_map.failure"));
	}
	private static int getCurrentState(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, (src, runtime) -> {
			source.sendSuccess(() -> Component.literal(
				runtime.getCurrentGamemodeID() + ": "
					+ runtime.getCurrentMapName() + " - "
					+ runtime.getCurrentPhase()), true);
		});
	}
	//private static int setSetting(@Nonnull CommandSourceStack source, @Nonnull String settingsName, @Nonnull String parameterName, @Nonnull String parameterValue)
	//{
	//	return adminFunc(source, (src, admin) -> {
	//		admin.
	//	}, )
	//}


	private static <T> int adminGetFunc(@Nonnull CommandSourceStack source,
								        @Nonnull Function<ITeamsAdmin, T> getFunc,
								        @Nonnull Function<T, OpResult> successFunc,
										@Nonnull Function<T, Component> successMsg,
										@Nonnull BiFunction<OpResult, T, Component> failureMsg)
	{
		return adminFunc(source, (src, admin) -> {
			T result = getFunc.apply(admin);
			OpResult error = successFunc.apply(result);
			if(error.success())
				src.sendSuccess(() -> successMsg.apply(result), true);
			else
				src.sendFailure(failureMsg.apply(error, result));
		});
	}
	private static int adminFunc(@Nonnull CommandSourceStack source,
								 @Nonnull Function<ITeamsAdmin, OpResult> funcWithSuccess,
								 @Nonnull Supplier<Component> successMsg,
								 @Nonnull Function<OpResult, Component> failureMsg)
	{
		return adminFunc(source, (src, admin) -> {
			OpResult error = funcWithSuccess.apply(admin);
			if(error.success())
				src.sendSuccess(successMsg, true);
			else
				src.sendFailure(failureMsg.apply(error));
		});
	}
	private static int adminFunc(@Nonnull CommandSourceStack source, @Nonnull BiConsumer<CommandSourceStack, ITeamsAdmin> func)
	{
		return adminFunc(source, (a, b) -> { func.accept(a, b); return -1; });
	}
	private static int adminFunc(@Nonnull CommandSourceStack source, @Nonnull BiFunction<CommandSourceStack, ITeamsAdmin, Integer> func)
	{
		ITeamsAdmin admin = TeamsAPI.getAdmin();
		if(admin != null)
		{
			return func.apply(source, admin);
		}
		else
		{
			source.sendFailure(Component.translatable("teams.command.admin_not_found"));
			return -1;
		}
	}

	private static <T> int runtimeGetFunc(@Nonnull CommandSourceStack source,
										  @Nonnull Function<ITeamsRuntime, T> getFunc,
										  @Nonnull Function<T, OpResult> successFunc,
										  @Nonnull Function<T, Component> successMsg,
										  @Nonnull BiFunction<OpResult, T, Component> failureMsg)
	{
		return runtimeFunc(source, (src, runtime) -> {
			T result = getFunc.apply(runtime);
			OpResult error = successFunc.apply(result);
			if(error.success())
				src.sendSuccess(() -> successMsg.apply(result), true);
			else
				src.sendFailure(failureMsg.apply(error, result));
		});
	}
	private static int runtimeFunc(@Nonnull CommandSourceStack source,
								   @Nonnull Function<ITeamsRuntime, OpResult> funcWithSuccess,
								   @Nonnull Supplier<Component> successMsg,
								   @Nonnull Function<OpResult, Component> failureMsg)
	{
		return runtimeFunc(source, (src, runtime) -> {
			OpResult error = funcWithSuccess.apply(runtime);
			if(error.success())
				src.sendSuccess(successMsg, true);
			else
				src.sendFailure(failureMsg.apply(error));
		});
	}
	private static int runtimeFunc(@Nonnull CommandSourceStack source, @Nonnull BiConsumer<CommandSourceStack, ITeamsRuntime> func)
	{
		return runtimeFunc(source, (a, b) -> { func.accept(a, b); return -1; });
	}
	private static int runtimeFunc(@Nonnull CommandSourceStack source, @Nonnull BiFunction<CommandSourceStack, ITeamsRuntime, Integer> func)
	{
		ITeamsRuntime runtime = TeamsAPI.getRuntime();
		if(runtime != null)
		{
			return func.apply(source, runtime);
		}
		else
		{
			source.sendFailure(Component.translatable("teams.command.runtime_not_found"));
			return -1;
		}
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
