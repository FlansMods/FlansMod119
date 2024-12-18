package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.OpResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IPlayerPersistentInfo
{
	@Nonnull UUID getID();

	@Nonnull OpResult setBuilder(boolean set);
	boolean isBuilder();
	@Nullable IPlayerBuilderSettings getBuilderSettings();

	int getPlayerRank();
	int getPlayerXP();
	@Nonnull OpResult addPlayerXP(int amount);
	@Nonnull OpResult processLevelUp(@Nonnull Function<Integer, Integer> xpFunction, @Nonnull Consumer<Integer> levelUpFunction);

	int getNumCustomLoadouts();
	@Nullable
	IPlayerLoadout getCustomLoadout(int index);
}
