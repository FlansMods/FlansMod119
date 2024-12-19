package com.flansmod.teams.server;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.IPlayerBuilderSettings;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import com.flansmod.teams.api.admin.IPlayerPersistentInfo;
import com.flansmod.teams.server.dimension.PlayerBuilderSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerSaveData implements IPlayerPersistentInfo
{
	private final UUID playerID;

	private int rank = 0;
	private int xp = 0;
	private boolean inBuildMode = false;
	private IPlayerBuilderSettings builderSettings = null;

	public PlayerSaveData(@Nonnull UUID id)
	{
		playerID = id;
	}

	@Override @Nonnull
	public OpResult setBuilder(boolean set)
	{
		inBuildMode = set;
		if(set && builderSettings == null)
			builderSettings = new PlayerBuilderSettings();
		return OpResult.SUCCESS;
	}
	@Override
	public boolean isBuilder() { return inBuildMode; }
	@Override @Nullable
	public IPlayerBuilderSettings getBuilderSettings() { return builderSettings; }
	@Override @Nonnull
	public UUID getID() { return playerID; }
	@Override
	public int getPlayerRank() { return rank; }
	@Override
	public int getPlayerXP() { return xp; }
	@Override @Nonnull
	public OpResult addPlayerXP(int amount)
	{
		xp += amount;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult processLevelUp(@Nonnull Function<Integer, Integer> xpFunction, @Nonnull Consumer<Integer> levelUpFunction)
	{
		final int MAX_LEVEL_UPS_PER_TICK = 20;
		int xpToLevelUp = xpFunction.apply(rank);
		int numLevelUps = 0;
		while(xp >= xpToLevelUp && numLevelUps < MAX_LEVEL_UPS_PER_TICK)
		{
			xp -= xpToLevelUp;
			rank++;
			levelUpFunction.accept(rank);
			xpToLevelUp = xpFunction.apply(rank);
			numLevelUps++;
		}
		return OpResult.SUCCESS;
	}
	@Override
	public int getNumCustomLoadouts()
	{
		return 0;
	}

	@Nullable
	@Override
	public IPlayerLoadout getCustomLoadout(int index)
	{
		return null;
	}
}
