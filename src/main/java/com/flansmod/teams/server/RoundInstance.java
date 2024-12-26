package com.flansmod.teams.server;

import com.flansmod.teams.api.*;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.RoundInfo;
import com.flansmod.teams.api.runtime.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoundInstance implements IRoundInstance
{
	private final RoundInfo info;

	private IGamemodeInstance gamemode;
	private IMapDetails map;
	public final List<ITeamInstance> teams = new ArrayList<>(2);
	public final List<IPlayerGameplayInfo> players = new ArrayList<>(8);
	public ERoundPhase phase = ERoundPhase.Preparing;

	public RoundInstance(@Nonnull RoundInfo roundInfo)
	{
		info = roundInfo;
	}

	@Override @Nonnull
	public RoundInfo getDef() { return info; }


	@Override @Nullable
	public IMapDetails getMap() { return map; }
	@Override @Nonnull
	public OpResult assignMap(@Nonnull IMapDetails set)
	{
		if(phase == ERoundPhase.Preparing)
		{
			map = set;
			return OpResult.SUCCESS;
		}
		else
			return OpResult.FAILURE_WRONG_PHASE;
	}

	@Override @Nullable
	public IGamemodeInstance getGamemode() { return gamemode; }
	@Override @Nonnull
	public OpResult assignGamemode(@Nonnull IGamemodeInstance set)
	{
		if(phase == ERoundPhase.Preparing)
		{
			gamemode = set;
			return OpResult.SUCCESS;
		}
		else
			return OpResult.FAILURE_WRONG_PHASE;
	}

	@Override @Nonnull
	public List<ITeamInstance> getTeams() { return teams; }
	@Override @Nonnull
	public OpResult assignTeams(@Nonnull List<ITeamInstance> set)
	{
		if(phase == ERoundPhase.Preparing)
		{
			teams.clear();
			teams.addAll(set);
			return OpResult.SUCCESS;
		}
		else
			return OpResult.FAILURE_WRONG_PHASE;
	}
	@Override @Nonnull
	public OpResult begin()
	{
		if(gamemode == null || map == null)
			return OpResult.FAILURE_GENERIC;

		//if(teams.size() != getDef().gamemode().factory().getNumTeamsRequired())
		//	return OpResult.FAILURE_INCORRECT_TEAMS;

		return OpResult.SUCCESS;
	}

	@Override @Nonnull
	public OpResult end()
	{
		return OpResult.SUCCESS;
	}

	@Override
	public boolean isParticipating(@Nonnull UUID playerID)
	{
		for(IPlayerGameplayInfo playerInfo : players)
			if(playerInfo.getID().equals(playerID))
				return true;

		return false;
	}
	@Override @Nonnull
	public List<IPlayerGameplayInfo> getParticipatingPlayers()
	{
		return players;
	}

	@Override @Nullable
	public IPlayerGameplayInfo getPlayerData(@Nonnull UUID playerID)
	{
		for(IPlayerGameplayInfo playerData : players)
			if(playerData.getID().equals(playerID))
				return playerData;

		PlayerInstance data = new PlayerInstance(playerID);
		players.add(data);
		return data;
	}
	@Override @Nonnull
	public OpResult addPlayer(@Nonnull UUID playerID, @Nonnull IPlayerGameplayInfo playerData)
	{
		players.add(playerData);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult removePlayer(@Nonnull UUID playerID)
	{
		for(int i = players.size() - 1; i >= 0; i--)
			if(players.get(i).getID().equals(playerID))
				players.remove(i);
		return OpResult.SUCCESS;
	}
	@Override @Nullable
	public ITeamInstance getTeam(@Nonnull ResourceLocation teamID)
	{
		for(ITeamInstance team : getTeams())
		{
			if(team.getTeamID().equals(teamID))
				return team;
		}
		return null;
	}
	@Override @Nullable
	public ITeamInstance getTeamOf(@Nonnull Entity entity)
	{
		for(ITeamInstance team : getTeams())
		{
			if(team.owns(entity))
				return team;
		}
		return null;
	}
	@Override @Nullable
	public ITeamInstance getTeamOf(@Nonnull BlockEntity blockEntity) { return null; }
	@Override @Nullable
	public ITeamInstance getTeamOf(@Nonnull BlockPos pos) { return null; }
	@Override @Nullable
	public ITeamInstance getTeamOf(@Nonnull ChunkPos pos) { return null; }
}
