package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.ISettings;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.TeamInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGamemodeInstance
{
	void init(@Nonnull ISettings settings);
	void tick();
	default boolean allowSpectators() { return true; }
	default boolean allowAdmins() { return true; }
	default boolean canDamage(@Nonnull Entity attacker, @Nonnull Entity target) { return true; }
	@Nullable default ITeamInstance getBestTeamFor(@Nonnull Player player) { return null; }

	default boolean doInstantRespawn(@Nonnull TeamInfo from, @Nonnull TeamInfo to) {
		return from.equals(TeamInfo.spectator);
	}

	@Nonnull
	ISpawnPoint getSpawnPoint(@Nonnull IMapDetails map, @Nonnull Player forPlayer);

	// Events
	default void playerKilled(@Nonnull ServerPlayer killed, @Nonnull DamageSource damage) {}
	default void playerDamaged(@Nonnull ServerPlayer damaged, @Nonnull DamageSource damage, float amount) {}
}
