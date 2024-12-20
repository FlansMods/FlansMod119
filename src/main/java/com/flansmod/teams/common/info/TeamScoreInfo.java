package com.flansmod.teams.common.info;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TeamScoreInfo
{
	public ResourceLocation teamID;
	public int teamTextColour;

	public int score;
	// In a win situation, each team should have a non-zero rank, with the lowest being the winner
	// In a tie, all teams should have rank 0
	public int rank;

	public List<PlayerScoreInfo> players = new ArrayList<>();

	public boolean isTied() { return rank == 0; }
}
