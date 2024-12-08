package com.flansmod.teams.server.gamemode;

import com.flansmod.teams.api.IMapInstance;
import com.flansmod.teams.api.IRoundInstance;
import com.flansmod.teams.api.ISettings;
import com.flansmod.teams.api.ISpawnPoint;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class GamemodeTDM extends Gamemode
{
	public boolean friendlyFire = false;
	public boolean autoBalanceTeams = false;
	public int scoreLimit = 50;
	public int autoBalanceIntervalTicks = 1200;

	public GamemodeTDM(@Nonnull IRoundInstance roundInstance)
	{
		super(roundInstance);
	}

	@Override
	public void init(@Nonnull ISettings settings)
	{
		friendlyFire = settings.getBooleanParameter("friendlyFire");
		autoBalanceTeams = settings.getBooleanParameter("autoBalance");
		autoBalanceIntervalTicks = (int)Math.floor(settings.getFloatParameter("autoBalanceInterval") * 20d);
		scoreLimit = settings.getIntegerParameter("scoreLimit");
	}

	@Override
	public void tick()
	{

	}

	@Override
	public boolean canDamage(@Nonnull Entity attacker, @Nonnull Entity target)
	{
		if(!friendlyFire)
		{
			return !sameTeam(attacker, target);
		}
		return true;
	}

	@Override @Nonnull
	public ISpawnPoint getSpawnPoint(@Nonnull IMapInstance map, @Nonnull Player forPlayer)
	{
		return null;
	}

	@Override
	public void playerDamaged(@Nonnull ServerPlayer damaged, @Nonnull DamageSource damage, float amount)
	{
		defaultDamageProcessing(damaged, damage, amount, true);
	}
	@Override
	public void playerKilled(@Nonnull ServerPlayer killed, @Nonnull DamageSource damage)
	{
		defaultKillProcessing(killed, damage, true, true);
	}
}

