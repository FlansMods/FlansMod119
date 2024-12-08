package com.flansmod.teams.common.info;

import com.flansmod.teams.api.admin.TeamInfo;

import java.util.ArrayList;
import java.util.List;

public class TeamScoreInfo
{
	public TeamInfo teamID;
	public int teamTextColour;

	public int score;
	// In a win situation, each team should have a non-zero rank, with the lowest being the winner
	// In a tie, all teams should have rank 0
	public int rank;

	public List<PlayerScoreInfo> players = new ArrayList<>();

	public boolean isTied() { return rank == 0; }
}
