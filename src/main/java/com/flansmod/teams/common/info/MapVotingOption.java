package com.flansmod.teams.common.info;

import com.flansmod.teams.api.MapInfo;

import javax.annotation.Nonnull;

public class MapVotingOption
{
	public MapInfo mapInfo;
	public int numVotes;

	public MapVotingOption(@Nonnull MapInfo map)
	{
		mapInfo = map;
		numVotes = 0;
	}
	public MapVotingOption(@Nonnull MapInfo map, int votes)
	{
		mapInfo = map;
		numVotes = votes;
	}

}
