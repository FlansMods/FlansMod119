package com.flansmod.teams.server.gamemode;

import com.flansmod.teams.api.admin.GamemodeInfo;
import com.flansmod.teams.api.admin.ISettings;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.runtime.IMapInstance;
import com.flansmod.teams.api.runtime.IRoundInstance;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class InvalidGamemode extends Gamemode
{
	public static final GamemodeInfo INVALID = GamemodeInfo.invalid;
	public InvalidGamemode(@Nonnull IRoundInstance round)
	{
		super(round);
	}
	@Override
	public void init(@Nonnull ISettings settings) {}
	@Override
	public void tick() { }
	@Override @Nonnull
	public ISpawnPoint getSpawnPoint(@Nonnull IMapInstance map, @Nonnull Player forPlayer)
	{
		return map.getSpawnPoints().get(0);
	}
}
