package com.flansmod.teams.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITeamsRuntime
{
	@Nonnull RoundInfo getCurrentRoundInfo();
	@Nonnull RoundInfo getNextRoundInfo();

	@Nonnull OpResult setNextRoundInfo(@Nonnull RoundInfo round);
	@Nonnull OpResult goToNextRound();

	@Nonnull ERoundPhase getCurrentPhase();
	int getTicksInCurrentPhase();

	@Nullable IRoundInstance getCurrentRound();
	@Nullable IMapInstance getCurrentMap();
	@Nullable IGamemodeInstance getCurrentGamemode();

	@Nonnull default MapInfo getCurrentMapInfo() { return getCurrentRoundInfo().map(); }
	@Nonnull default MapInfo getNextMapInfo() { return getNextRoundInfo().map(); }
	@Nonnull default String getCurrentMapName() { return getCurrentMapInfo().mapName(); }
	@Nonnull default String getNextMapName() { return getNextMapInfo().mapName(); }
	@Nonnull default GamemodeInfo getCurrentGamemodeInfo() { return getCurrentRoundInfo().gamemode(); }
	@Nonnull default GamemodeInfo getNextGamemodeInfo() { return getNextRoundInfo().gamemode(); }
}
