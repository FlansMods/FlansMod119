package com.flansmod.teams.common.info;

import javax.annotation.Nonnull;
import java.util.List;

public class MapVotingOption
{
	public String mapID;
	public String mapLongName;
	public String gamemodeID;
	public List<String> teamIDs;
	public int numVotes;

	public MapVotingOption(@Nonnull String map)
	{
		mapID = map;
		numVotes = 0;
	}
	public MapVotingOption(@Nonnull String map, int votes)
	{
		mapID = map;
		numVotes = votes;
	}

}
