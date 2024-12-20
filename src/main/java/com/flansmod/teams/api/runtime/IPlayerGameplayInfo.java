package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.TeamsAPI;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IPlayerGameplayInfo
{


	@Nonnull UUID getID();

	int getMapVote();
	int getScore(@Nonnull String scoreType);
	boolean isBuilder();

	@Nonnull
	OpResult setVote(int index);
	@Nonnull OpResult resetScore(@Nonnull String scoreType);
	@Nonnull OpResult resetAllScores();
	@Nonnull OpResult addScore(@Nonnull String scoreType, int add);

	@Nonnull OpResult setTeamChoice(@Nonnull ResourceLocation teamID);
	@Nonnull ResourceLocation getTeamChoice();
	@Nonnull OpResult setLoadoutChoice(int loadoutIndex);
	int getLoadoutChoice();

	default int getKills() { return getScore(TeamsAPI.SCORE_TYPE_KILLS); }
	default int getAssists() { return getScore(TeamsAPI.SCORE_TYPE_ASSISTS); }
	default int getObjectiveScore() { return getScore(TeamsAPI.SCORE_TYPE_OBJECTIVES); }
	default int getDeaths() { return getScore(TeamsAPI.SCORE_TYPE_DEATHS); }
	default OpResult addKills(int add) { return addScore(TeamsAPI.SCORE_TYPE_KILLS, add); }
	default OpResult addAssists(int add) { return addScore(TeamsAPI.SCORE_TYPE_ASSISTS, add); }
	default OpResult addObjectiveScore(int add) { return addScore(TeamsAPI.SCORE_TYPE_OBJECTIVES, add); }
	default OpResult addDeaths(int add) { return addScore(TeamsAPI.SCORE_TYPE_DEATHS, add); }


	boolean hasRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);
	@Nonnull OpResult addRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);
	@Nonnull OpResult removeRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);

	default boolean hasAssistOn(@Nonnull UUID otherPlayer) { return hasRelationship(otherPlayer, TeamsAPI.RELATIONSHIP_TYPE_ASSIST); }
	default OpResult tagAssistOn(@Nonnull UUID otherPlayer) { return addRelationship(otherPlayer, TeamsAPI.RELATIONSHIP_TYPE_ASSIST); }
	default OpResult removeAssistOn(@Nonnull UUID otherPlayer) { return removeRelationship(otherPlayer, TeamsAPI.RELATIONSHIP_TYPE_ASSIST); }

}
