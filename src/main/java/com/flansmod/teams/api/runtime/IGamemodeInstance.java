package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.admin.ISettings;
import com.flansmod.teams.api.admin.ISpawnPoint;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGamemodeInstance
{
	@Nonnull Level getDimension();
	void init(@Nonnull ISettings settings);
	void tick();
	default boolean allowSpectators() { return true; }
	default boolean allowAdmins() { return true; }
	default boolean canDamage(@Nonnull Entity attacker, @Nonnull Entity target) { return true; }
	@Nullable default ITeamInstance getBestTeamFor(@Nonnull Player player) { return null; }

	default boolean doInstantRespawn(@Nonnull ResourceLocation from, @Nonnull ResourceLocation to) {
		return from.equals(TeamsAPI.spectatorTeam);
	}

	@Nonnull
	ISpawnPoint getSpawnPoint(@Nonnull IMapDetails map, @Nonnull Player forPlayer);

	// Events
	default void playerKilled(@Nonnull ServerPlayer killed, @Nonnull DamageSource damage) {}
	default void playerDamaged(@Nonnull ServerPlayer damaged, @Nonnull DamageSource damage, float amount) {}
}
