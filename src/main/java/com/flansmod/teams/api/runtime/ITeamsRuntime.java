package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.GamemodeInfo;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.MapInfo;
import com.flansmod.teams.api.admin.RoundInfo;

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

	@Nonnull default MapInfo getCurrentMapInfo() { return getCurrentRoundInfo().map(); }
	@Nonnull default MapInfo getNextMapInfo() { return getNextRoundInfo().map(); }
	@Nonnull default String getCurrentMapName() { return getCurrentMapInfo().mapName(); }
	@Nonnull default String getNextMapName() { return getNextMapInfo().mapName(); }
	@Nonnull default GamemodeInfo getCurrentGamemodeInfo() { return getCurrentRoundInfo().gamemode(); }
	@Nonnull default GamemodeInfo getNextGamemodeInfo() { return getNextRoundInfo().gamemode(); }
}
