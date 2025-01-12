package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.stats.IStatAccumulator;
import com.flansmod.common.types.JsonField;
import com.flansmod.util.formulae.EAccumulationOperation;

import javax.annotation.Nonnull;

public class StatAccumulatorDefinition implements IStatAccumulator
{
	@JsonField
	public EAccumulationOperation operation = EAccumulationOperation.BaseAdd;
	@JsonField
	public float value = 0.0f;
	@JsonField
	public EAccumulationSource[] multiplyPer = new EAccumulationSource[0];

	public float GetValue()
	{
		switch(operation)
		{
			case StackablePercentage, IndependentPercentage -> { return value / 100f; }
		}
		return value;
	}

	@Override @Nonnull
	public EAccumulationOperation getOperation() { return operation; }
	@Override
	public float getValue() { return value; }
	@Override @Nonnull
	public EAccumulationSource[] getMultipliers() { return multiplyPer; }
}
