package com.flansmod.common.actions.stats;

import com.flansmod.common.types.abilities.elements.EAccumulationSource;
import com.flansmod.util.formulae.EAccumulationOperation;

import javax.annotation.Nonnull;

public interface IStatAccumulator
{
	@Nonnull EAccumulationOperation getOperation();
	float getValue();
	@Nonnull EAccumulationSource[] getMultipliers();
}
