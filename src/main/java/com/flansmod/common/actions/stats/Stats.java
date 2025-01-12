package com.flansmod.common.actions.stats;

import com.flansmod.common.types.abilities.elements.EAccumulationSource;
import com.flansmod.util.formulae.EAccumulationOperation;

import javax.annotation.Nonnull;

public class Stats
{
	public static final String[] EMPTY_GROUP_PATHS = new String[0];
	public static final EAccumulationSource[] NO_SOURCES = new EAccumulationSource[0];

	public record SimpleStatAccumulator(@Nonnull String statID,
										@Nonnull EAccumulationOperation operation,
										float value)
		implements IStatModifier, IStatAccumulator
	{
		@Override @Nonnull
		public String getStat() { return statID; }
		@Override @Nonnull
		public String[] getMatchGroupPaths() { return EMPTY_GROUP_PATHS; }
		@Override @Nonnull
		public IStatAccumulator[] getAccumulators() { return new IStatAccumulator[] { this }; }
		@Override @Nonnull
		public String getSetValue() { return ""; }
		@Override @Nonnull
		public EAccumulationOperation getOperation() { return operation; }
		@Override
		public float getValue() { return value; }
		@Override @Nonnull
		public EAccumulationSource[] getMultipliers() { return NO_SOURCES; }
	}

	@Nonnull
	public static IStatModifier add(@Nonnull String statID, float value)
	{
		return new SimpleStatAccumulator(statID, EAccumulationOperation.BaseAdd, value);
	}
	@Nonnull
	public static IStatModifier stackableMultiplier(@Nonnull String statID, float value)
	{
		return new SimpleStatAccumulator(statID, EAccumulationOperation.StackablePercentage, value);
	}
	@Nonnull
	public static IStatModifier independentMultiplier(@Nonnull String statID, float value)
	{
		return new SimpleStatAccumulator(statID, EAccumulationOperation.IndependentPercentage, value);
	}
}
