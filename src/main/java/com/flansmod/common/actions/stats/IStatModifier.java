package com.flansmod.common.actions.stats;

import javax.annotation.Nonnull;

public interface IStatModifier
{
	@Nonnull String getStat();
	@Nonnull String[] getMatchGroupPaths();
	@Nonnull IStatAccumulator[] getAccumulators();
	@Nonnull String getSetValue();
}
