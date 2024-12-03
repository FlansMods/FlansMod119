package com.flansmod.teams.server.command;

import com.flansmod.teams.api.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
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
				.then(Commands.literal("nextMap").executes(ctx -> goToNextMap(ctx.getSource())))
				.then(Commands.literal("setNextMap")
					.then(Commands.argument("mapName", StringArgumentType.word()))
						.executes(ctx -> setNextMap(ctx.getSource(), StringArgumentType.getString(ctx, "mapName")))
				)
				.then(Commands.literal("getNextMap").executes(ctx -> getNextMap(ctx.getSource())))
				.then(Commands.literal("getCurrentMap").executes(ctx -> getCurrentMap(ctx.getSource())))
				.then(Commands.literal("rotation")
					.then(Commands.literal("enable").executes(ctx -> enableRotation(ctx.getSource())))
					.then(Commands.literal("disable").executes(ctx -> disableRotation(ctx.getSource())))
					.then(Commands.literal("add")
						.then(Commands.argument("mapName", StringArgumentType.word())
							.executes(ctx -> addMapToRotation(ctx.getSource(), StringArgumentType.getString(ctx, "mapName"), -1)))
							.then(Commands.argument("atPosition", IntegerArgumentType.integer(0, 999))
								.executes(ctx -> addMapToRotation(ctx.getSource(), StringArgumentType.getString(ctx, "mapName"), IntegerArgumentType.getInteger(ctx, "atPosition"))))
					)
					.then(Commands.literal("remove")
						.then(Commands.argument("mapName", StringArgumentType.word())
							.executes(ctx -> removeMapFromRotation(ctx.getSource(), StringArgumentType.getString(ctx, "mapName"))))
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
	private static int addMapToRotation(@Nonnull CommandSourceStack source, @Nonnull String mapName, int positionHint)
	{
		return adminFunc(source, (admin) -> admin.addMapToRotation(mapName, positionHint),
				() -> Component.translatable("teams.command.rotation_add_map.success", mapName),
				(errorType) -> switch(errorType)
				{
					case FAILURE_INVALID_MAP_INDEX -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_index", mapName, positionHint);
					case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.rotation_add_map.failure.invalid_map_name", mapName);
					default -> Component.translatable("teams.command.rotation_add_map.failure", mapName);
				});
	}
	private static int removeMapFromRotation(@Nonnull CommandSourceStack source, @Nonnull String mapName)
	{
		return adminFunc(source, (admin) -> admin.removeMapFromRotation(mapName),
				() -> Component.translatable("teams.command.rotation_remove_map.success", mapName),
				(errorType) -> switch(errorType)
				{
					case FAILURE_MAP_NOT_FOUND -> Component.translatable("teams.command.rotation_remove_map.failure.map_not_found", mapName);
					case FAILURE_INVALID_MAP_NAME -> Component.translatable("teams.command.rotation_remove_map.failure.invalid_map_name", mapName);
					default -> Component.translatable("teams.command.rotation_remove_map.failure", mapName);
				});
	}

	private static int getCurrentMap(@Nonnull CommandSourceStack source)
	{
		return runtimeGetFunc(source,
				ITeamsRuntime::getCurrentMap,
				TeamsAPI::isValidMapName,
				(result) -> Component.translatable("teams.command.get_current_map.success", result),
				(errorType, result) -> Component.translatable("teams.command.get_current_map.failure"));
	}
	private static int getNextMap(@Nonnull CommandSourceStack source)
	{
		return runtimeGetFunc(source,
				ITeamsRuntime::getNextMap,
				TeamsAPI::isValidMapName,
				(result) -> Component.translatable("teams.command.get_next_map.success", result),
				(errorType, result) -> Component.translatable("teams.command.get_next_map.failure"));
	}
	private static int setNextMap(@Nonnull CommandSourceStack source, @Nonnull String mapName)
	{
		return runtimeFunc(source, (runtime) -> runtime.setNextMap(mapName),
				() -> Component.translatable("teams.command.set_next_map.success", mapName),
				(errorType) -> Component.translatable("teams.command.set_next_map.failure", mapName));
	}
	private static int goToNextMap(@Nonnull CommandSourceStack source)
	{
		return runtimeFunc(source, ITeamsRuntime::goToNextMap,
					() -> Component.translatable("teams.command.go_to_next_map.success"),
					(errorType) -> Component.translatable("teams.command.go_to_next_map.failure"));
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
}
