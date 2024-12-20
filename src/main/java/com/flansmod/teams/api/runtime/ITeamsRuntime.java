package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.RoundInfo;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ITeamsRuntime
{
	@Nonnull
	OpResult start();
	@Nonnull OpResult stop();

	@Nonnull
	RoundInfo getCurrentRoundInfo();
	@Nonnull RoundInfo getNextRoundInfo();

	@Nonnull OpResult setNextRoundInfo(@Nonnull RoundInfo round);
	@Nonnull OpResult goToNextRound();

	@Nonnull
	ERoundPhase getCurrentPhase();
	int getTicksInCurrentPhase();

	@Nullable IRoundInstance getCurrentRound();
	@Nullable
	IMapDetails getCurrentMap();
	@Nullable IGamemodeInstance getCurrentGamemode();

	@Nonnull List<String> getDimensionInfo();

	@Nonnull default String getCurrentMapName() { return getCurrentRoundInfo().mapName(); }
	@Nonnull default String getNextMapName() { return getNextRoundInfo().mapName(); }
	@Nonnull default ResourceLocation getCurrentGamemodeID() { return getCurrentRoundInfo().gamemodeID(); }
	@Nonnull default ResourceLocation getNextGamemodeID() { return getNextRoundInfo().gamemodeID(); }
}
