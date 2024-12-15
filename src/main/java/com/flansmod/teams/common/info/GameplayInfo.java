package com.flansmod.teams.common.info;

import com.flansmod.teams.api.ERoundPhase;

import java.util.List;

public class GameplayInfo
{
	private static final List<String> defaultScoreTypes = List.of("points", "kills", "deaths");

	public ERoundPhase currentPhase;
	public int ticksRemaining;
	public int scoreLimit;
	public boolean isBuilder;

	public List<String> scoreTypes = defaultScoreTypes;
}
