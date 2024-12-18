package com.flansmod.teams.api.runtime;

import com.flansmod.teams.api.admin.IControlPointRef;
import com.flansmod.teams.api.admin.ISpawnPoint;
import com.flansmod.teams.api.admin.MapInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IMapInstance
{
	@Nonnull MapInfo getInfo();
	@Nonnull List<ChunkPos> getChunkLoadTickets();
	@Nonnull List<IControlPointRef> getControlPoints();
	@Nonnull List<ISpawnPoint> getSpawnPoints();
	@Nullable IControlPointInstance tryResolve(@Nonnull Level level, @Nonnull IControlPointRef ref, boolean loadChunks);
}
