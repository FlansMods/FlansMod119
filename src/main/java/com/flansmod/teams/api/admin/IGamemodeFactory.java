package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.runtime.IGamemodeInstance;
import com.flansmod.teams.api.runtime.IRoundInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGamemodeFactory
{
	IGamemodeFactory invalid = new IGamemodeFactory()
	{
		@Override
		public boolean isValid(@Nonnull RoundInfo roundInfo) { return false; }
		@Override
		public int getNumTeamsRequired() { return 0; }
		@Override @Nullable
		public IGamemodeInstance createInstance(@Nonnull IRoundInstance roundInstance) { return null; }
	};

	boolean isValid(@Nonnull RoundInfo roundInfo);
	int getNumTeamsRequired();
	@Nullable IGamemodeInstance createInstance(@Nonnull IRoundInstance roundInstance);
}
