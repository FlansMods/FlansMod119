package com.flansmod.teams.server;

import com.flansmod.teams.api.admin.TeamInfo;
import com.flansmod.teams.api.runtime.IPlayerGameplayInfo;
import com.flansmod.teams.api.OpResult;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerInstance implements IPlayerGameplayInfo
{
	public final UUID playerID;
	private int mapVote = 0;
	private TeamInfo nextTeamChoice = TeamInfo.invalid;
	private int loadoutChoice = 0;
	private final Map<String, Integer> scores = new HashMap<>();
	private final Map<UUID, List<String>> relationships = new HashMap<>();

	public PlayerInstance(@Nonnull UUID id)
	{
		playerID = id;
	}

	@Override @Nonnull
	public UUID getID() { return playerID; }
	@Override
	public int getMapVote() { return mapVote; }
	@Override @Nonnull
	public OpResult setVote(int index)
	{
		mapVote = index;
		return OpResult.SUCCESS;
	}
	@Override
	public int getScore(@Nonnull String scoreType)
	{
		return scores.getOrDefault(scoreType, 0);
	}
	@Override
	public boolean isBuilder()
	{
		return false;
	}
	@Override @Nonnull
	public OpResult setTeamChoice(@Nonnull TeamInfo teamID)
	{
		nextTeamChoice = teamID;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public TeamInfo getTeamChoice()
	{
		return nextTeamChoice;
	}
	@Override @Nonnull
	public OpResult setLoadoutChoice(int loadoutIndex)
	{
		loadoutChoice = loadoutIndex;
		return OpResult.SUCCESS;
	}
	@Override
	public int getLoadoutChoice() { return loadoutChoice; }
	@Override
	public boolean hasRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType)
	{
		List<String> relationshipsToPlayer = relationships.get(otherPlayer);
		if(relationshipsToPlayer != null)
			return relationshipsToPlayer.contains(relationshipType);

		return false;
	}
	@Override @Nonnull
	public OpResult addRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType)
	{
		if(!relationships.containsKey(otherPlayer))
			relationships.put(otherPlayer, new ArrayList<>(2));

		List<String> relationshipsToPlayer = relationships.get(otherPlayer);
		if(!relationshipsToPlayer.contains(relationshipType))
			relationshipsToPlayer.add(relationshipType);

		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult removeRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType)
	{
		List<String> relationshipsToPlayer = relationships.get(otherPlayer);
		if(relationshipsToPlayer != null)
		{
			relationshipsToPlayer.remove(relationshipType);
		}
		return OpResult.SUCCESS;
	}


	@Override @Nonnull
	public OpResult resetScore(@Nonnull String scoreType)
	{
		scores.remove(scoreType);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult resetAllScores()
	{
		scores.clear();
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult addScore(@Nonnull String scoreType, int add)
	{
		scores.put(scoreType, getScore(scoreType) + add);
		return OpResult.SUCCESS;
	}
}
