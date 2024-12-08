package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.TeamInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ITeamInstance
{
	@Nonnull
	TeamInfo getDefinition();

	int getScore(@Nonnull String scoreType);
	@Nonnull
	OpResult resetScore(@Nonnull String scoreType);
	@Nonnull OpResult resetAllScores();
	@Nonnull OpResult addScore(@Nonnull String scoreType, int add);
	default int getKills() { return getScore(IPlayerInstance.SCORE_TYPE_KILLS); }
	default int getObjectiveScore() { return getScore(IPlayerInstance.SCORE_TYPE_OBJECTIVES); }
	default int getDeaths() { return getScore(IPlayerInstance.SCORE_TYPE_DEATHS); }
	default OpResult addKills(int add) { return addScore(IPlayerInstance.SCORE_TYPE_KILLS, add); }
	default OpResult addObjectiveScore(int add) { return addScore(IPlayerInstance.SCORE_TYPE_OBJECTIVES, add); }
	default OpResult addDeaths(int add) { return addScore(IPlayerInstance.SCORE_TYPE_DEATHS, add); }

	boolean owns(@Nonnull Entity entity);
	@Nonnull Set<UUID> getMemberUUIDs();
	@Nonnull Collection<Player> getMemberPlayers();
	boolean canAdd(@Nonnull Player player);
	boolean add(@Nonnull Player player);
	boolean remove(@Nonnull Player player);

}
