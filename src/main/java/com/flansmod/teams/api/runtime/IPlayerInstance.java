package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.OpResult;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IPlayerInstance
{
	String SCORE_TYPE_OBJECTIVES = "obj";
	String SCORE_TYPE_KILLS = "kill";
	String SCORE_TYPE_ASSISTS = "assist";
	String SCORE_TYPE_DEATHS = "death";

	String RELATIONSHIP_TYPE_ASSIST = "assist";

	@Nonnull UUID getID();

	int getMapVote();
	int getScore(@Nonnull String scoreType);
	boolean isBuilder();

	@Nonnull
	OpResult setVote(int index);
	@Nonnull OpResult resetScore(@Nonnull String scoreType);
	@Nonnull OpResult resetAllScores();
	@Nonnull OpResult addScore(@Nonnull String scoreType, int add);


	default int getKills() { return getScore(SCORE_TYPE_KILLS); }
	default int getAssists() { return getScore(SCORE_TYPE_ASSISTS); }
	default int getObjectiveScore() { return getScore(SCORE_TYPE_OBJECTIVES); }
	default int getDeaths() { return getScore(SCORE_TYPE_DEATHS); }
	default OpResult addKills(int add) { return addScore(SCORE_TYPE_KILLS, add); }
	default OpResult addAssists(int add) { return addScore(SCORE_TYPE_ASSISTS, add); }
	default OpResult addObjectiveScore(int add) { return addScore(SCORE_TYPE_OBJECTIVES, add); }
	default OpResult addDeaths(int add) { return addScore(SCORE_TYPE_DEATHS, add); }


	boolean hasRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);
	@Nonnull OpResult addRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);
	@Nonnull OpResult removeRelationship(@Nonnull UUID otherPlayer, @Nonnull String relationshipType);

	default boolean hasAssistOn(@Nonnull UUID otherPlayer) { return hasRelationship(otherPlayer, RELATIONSHIP_TYPE_ASSIST); }
	default OpResult tagAssistOn(@Nonnull UUID otherPlayer) { return addRelationship(otherPlayer, RELATIONSHIP_TYPE_ASSIST); }
	default OpResult removeAssistOn(@Nonnull UUID otherPlayer) { return removeRelationship(otherPlayer, RELATIONSHIP_TYPE_ASSIST); }

}
