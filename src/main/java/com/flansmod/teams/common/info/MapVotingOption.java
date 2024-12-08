package com.flansmod.teams.common.info;

import com.flansmod.teams.api.MapInfo;

import javax.annotation.Nonnull;
import java.util.List;

public class MapVotingOption
{
	public String mapID;
	public String mapLongName;
	public String gamemodeID;
	public List<String> teamIDs;
	public int numVotes;

	public MapVotingOption(@Nonnull MapInfo map)
	{
		mapID = map.mapName();
		numVotes = 0;
	}
	public MapVotingOption(@Nonnull MapInfo map, int votes)
	{
		mapID = map.mapName();
		numVotes = votes;
	}

}
