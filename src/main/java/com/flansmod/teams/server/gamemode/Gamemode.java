package com.flansmod.teams.server.gamemode;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.teams.api.admin.IGamemodeFactory;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.RoundInfo;
import com.flansmod.teams.api.runtime.IGamemodeInstance;
import com.flansmod.teams.api.runtime.IPlayerGameplayInfo;
import com.flansmod.teams.api.runtime.IRoundInstance;
import com.flansmod.teams.api.runtime.ITeamInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Gamemode implements IGamemodeInstance
{
	public static class DefaultGamemodeFactory<T extends Gamemode> implements IGamemodeFactory
	{
		public final ResourceLocation id;
		public final int teamCount;
		public final BiFunction<IRoundInstance, Level, T> createFunc;

		public DefaultGamemodeFactory(@Nonnull ResourceLocation gamemodeID, int count, @Nonnull BiFunction<IRoundInstance, Level, T> constructor)
		{
			id = gamemodeID;
			teamCount = count;
			createFunc = constructor;
		}
		@Override
		public int getNumTeamsRequired() { return teamCount; }
		@Override
		public boolean isValid(@Nonnull RoundInfo roundInfo)
		{
			return roundInfo.gamemodeID().equals(id) && roundInfo.teams().size() == teamCount;
		}
		@Override
		public boolean isValid(@Nonnull IMapDetails mapDetails)
		{
			return mapDetails.getSpawnPoints().size() > 0;
		}
		@Override @Nonnull
		public IGamemodeInstance createInstance(@Nonnull IRoundInstance roundInstance, @Nonnull Level level)
		{
			return createFunc.apply(roundInstance, level);
		}
	}
	@Nonnull
	public static <T extends Gamemode> IGamemodeFactory createFactory(@Nonnull ResourceLocation gamemodeID,
															          int teamCount,
															          @Nonnull BiFunction<IRoundInstance, Level, T> constructor)
	{
		return new DefaultGamemodeFactory<T>(gamemodeID, teamCount, constructor);
	}

	private final IRoundInstance round;
	private final Level dimension;
	public Gamemode(@Nonnull IRoundInstance roundInstance,
					@Nonnull Level dim)
	{
		round = roundInstance;
		dimension = dim;
	}

	@Override @Nonnull
	public Level getDimension() { return dimension; }
	public boolean canDamage(@Nonnull Entity attacker, @Nonnull Entity target) { return true; }
	public int getTeamCountRequirement() { return 2; }
	public boolean allowDuplicateTeams() { return false; }
	public boolean canUseTeam(int teamIndex, @Nonnull ResourceLocation team) { return true; }
	public boolean canUseTeams(@Nonnull List<ResourceLocation> teams)
	{
		if(teams.size() != getTeamCountRequirement())
			return false;
		for(int i = 0; i < teams.size(); i++)
		{
			if(!canUseTeam(i, teams.get(i)))
				return false;

			if(!allowDuplicateTeams())
			{
				for (int j = 0; j < i; j++)
					if (teams.get(i) == teams.get(j))
						return false;
			}
		}
		return true;
	}

	@Nonnull
	protected ISpawnPoint heuristicSpawnHandler(@Nonnull IMapDetails map,
												@Nonnull Player player,
												@Nonnull BiFunction<ISpawnPoint, Player, Float> heuristic)
	{
		float bestValue = 0.0f;
		ISpawnPoint bestSpawnPoint = null;
		for(ISpawnPoint spawnPoint : map.getSpawnPoints())
		{
			float value = heuristic.apply(spawnPoint, player);
			if(value > bestValue)
			{
				bestValue = value;
				bestSpawnPoint = spawnPoint;
			}
		}
		if(bestSpawnPoint == null)
			return map.getDefaultSpawnPoint();

		return bestSpawnPoint;
	}

	@Nonnull
	protected ISpawnPoint defaultSpawnHandler(@Nonnull IMapDetails map,
											  @Nonnull Player forPlayer)
	{
		IPlayerGameplayInfo spawningPlayerData = getPlayerData(forPlayer);
		if(spawningPlayerData == null)
			return map.getDefaultSpawnPoint();

		return heuristicSpawnHandler(map, forPlayer, (spawnPoint, spawningPlayer) -> {

			float totalEnemyProximity = 0.0f;
			for(IPlayerGameplayInfo playerData : round.getParticipatingPlayers())
			{
				if(playerData.getTeamChoice().equals(spawningPlayerData.getTeamChoice()))
					continue;

				Player testPlayer = dimension.getPlayerByUUID(playerData.getID());
				if(testPlayer == null)
					continue;

				double dx = testPlayer.position().x - spawnPoint.getPos().getX();
				double dy = testPlayer.position().y - spawnPoint.getPos().getY();
				double dz = testPlayer.position().z - spawnPoint.getPos().getZ();
				totalEnemyProximity += Maths.fastInvSqrt(dx*dx+dy*dy+dz*dz);
			}
			return totalEnemyProximity;
		});
	}

	protected void defaultDamageProcessing(@Nonnull ServerPlayer damaged,
										   @Nonnull DamageSource damage,
										   float amount,
										   boolean tagAssists)
	{
		if(tagAssists)
		{
			Player damager = getResponsiblePlayer(damage);
			if (damager != null)
			{
				IPlayerGameplayInfo damagerPlayer = getPlayerData(damager);
				if (damagerPlayer != null)
				{
					damagerPlayer.tagAssistOn(damaged.getUUID());
				}
			}
		}
	}
	protected void defaultKillProcessing(@Nonnull ServerPlayer killed,
										 @Nonnull DamageSource damage,
										 boolean addToTeamScores,
										 boolean processAssists)
	{
		IPlayerGameplayInfo killedPlayer = getPlayerData(killed);
		if(killedPlayer != null)
			killedPlayer.addDeaths(1);

		if(addToTeamScores)
		{
			ITeamInstance killedTeam = getTeamOf(killed);
			if (killedTeam != null)
				killedTeam.addDeaths(1);
		}

		Player killer = getResponsiblePlayer(damage);
		if(killer != null)
		{
			IPlayerGameplayInfo killerPlayer = getPlayerData(killer);
			if(killerPlayer != null)
				killerPlayer.addKills(1);

			if(addToTeamScores)
			{
				ITeamInstance killerTeam = getTeamOf(killer);
				if (killerTeam != null)
					killerTeam.addKills(1);
			}
		}

		if(processAssists)
		{
			for(IPlayerGameplayInfo playerData : round.getParticipatingPlayers())
			{
				if(playerData.hasAssistOn(killed.getUUID()))
				{
					playerData.addAssists(1);
					playerData.removeAssistOn(killed.getUUID());
				}
			}
		}
	}

	protected boolean sameTeam(@Nonnull Entity a, @Nonnull Entity b)
	{
		return round.getTeamOf(a) == round.getTeamOf(b);
	}
	@Nullable
	protected IPlayerGameplayInfo getPlayerData(@Nonnull Player player)
	{
		return round.getPlayerData(player.getUUID());
	}
	@Nullable
	protected Player getResponsiblePlayer(@Nonnull DamageSource source)
	{
		if(source.getDirectEntity() instanceof Player player)
			return player;
		if(source.getEntity() instanceof Player player)
			return player;

		return null;
	}

	@Nullable
	public ITeamInstance getTeamOf(@Nonnull Entity entity) { return round.getTeamOf(entity); }
	@Nullable
	public ITeamInstance getTeamOf(@Nonnull BlockEntity blockEntity) { return round.getTeamOf(blockEntity); }
	@Nullable
	public ITeamInstance getTeamOf(@Nonnull BlockPos pos) { return round.getTeamOf(pos); }
	@Nullable
	public ITeamInstance getTeamOf(@Nonnull ChunkPos pos) { return round.getTeamOf(pos); }
}
