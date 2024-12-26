package com.flansmod.teams.server.gamemode;

import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.api.admin.ISettings;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.IMapDetails;
import com.flansmod.teams.api.runtime.IRoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class InvalidGamemode extends Gamemode
{
	public static final ResourceLocation INVALID = TeamsAPI.invalidGamemode;
	public InvalidGamemode(@Nonnull IRoundInstance round, @Nonnull Level level)
	{
		super(round, level);
	}
	@Override
	public void init(@Nonnull ISettings settings) {}
	@Override
	public void tick() { }
	@Override @Nonnull
	public ISpawnPoint getSpawnPoint(@Nonnull IMapDetails map, @Nonnull Player forPlayer)
	{
		return map.getSpawnPoints().get(0);
	}
}
