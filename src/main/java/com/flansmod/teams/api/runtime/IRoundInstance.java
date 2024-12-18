package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.RoundInfo;
import com.flansmod.teams.api.admin.TeamInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IRoundInstance
{
	@Nonnull
	RoundInfo getDef();

	@Nonnull List<ITeamInstance> getTeams();
	@Nonnull
	OpResult assignTeams(@Nonnull List<ITeamInstance> teams);

	@Nullable IGamemodeInstance getGamemode();
	@Nonnull OpResult assignGamemode(@Nonnull IGamemodeInstance gamemode);

	@Nullable IMapInstance getMap();
	@Nonnull OpResult assignMap(@Nonnull IMapInstance map);

	@Nonnull List<IPlayerGameplayInfo> getParticipatingPlayers();
	@Nullable
	IPlayerGameplayInfo getPlayerData(@Nonnull UUID playerID);
	@Nonnull OpResult addPlayer(@Nonnull UUID playerID, @Nonnull IPlayerGameplayInfo playerData);
	@Nonnull OpResult removePlayer(@Nonnull UUID playerID);


	@Nonnull OpResult begin();
	@Nonnull OpResult end();

	@Nullable ITeamInstance getTeam(@Nonnull TeamInfo teamID);
	@Nullable ITeamInstance getTeamOf(@Nonnull Entity entity);
	@Nullable ITeamInstance getTeamOf(@Nonnull BlockEntity blockEntity);
	@Nullable ITeamInstance getTeamOf(@Nonnull BlockPos pos);
	@Nullable ITeamInstance getTeamOf(@Nonnull ChunkPos pos);
}
