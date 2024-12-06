package com.flansmod.teams.server.gamemode;

import com.flansmod.teams.api.GamemodeInfo;
import com.flansmod.teams.api.IRoundInstance;
import com.flansmod.teams.api.ISettings;

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
}
