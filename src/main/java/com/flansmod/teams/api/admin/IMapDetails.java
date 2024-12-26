package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.runtime.IControlPointInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IMapDetails
{
	@Nonnull String getName();
	@Nonnull List<ChunkPos> getChunkLoadTickets();
	@Nonnull List<IControlPointRef> getControlPoints();
	@Nonnull List<ISpawnPoint> getSpawnPoints();
	@Nullable IControlPointInstance tryResolve(@Nonnull Level level, @Nonnull IControlPointRef ref, boolean loadChunks);

	@Nonnull default ISpawnPoint getDefaultSpawnPoint() {
		if(getSpawnPoints().size() > 0)
			return getSpawnPoints().get(0);
		return ISpawnPoint.zero;
	}
}
