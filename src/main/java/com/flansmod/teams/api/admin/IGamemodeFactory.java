package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.runtime.IGamemodeInstance;
import com.flansmod.teams.api.runtime.IRoundInstance;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGamemodeFactory
{
	IGamemodeFactory invalid = new IGamemodeFactory()
	{
		@Override
		public boolean isValid(@Nonnull RoundInfo roundInfo) { return false; }
		@Override
		public boolean isValid(@Nonnull IMapDetails mapDetails) { return false; }
		@Override
		public int getNumTeamsRequired() { return 0; }
		@Override @Nullable
		public IGamemodeInstance createInstance(@Nonnull IRoundInstance roundInstance, @Nonnull Level level) { return null; }
	};

	boolean isValid(@Nonnull RoundInfo roundInfo);
	boolean isValid(@Nonnull IMapDetails mapDetails);
	int getNumTeamsRequired();
	@Nullable IGamemodeInstance createInstance(@Nonnull IRoundInstance roundInstance, @Nonnull Level level);
}
