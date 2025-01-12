package com.flansmod.common.actions.stats;

import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;

public interface IModifierBaker
{
	default void Bake(@Nonnull IStatModifier mod) { Bake(mod, 1, 1); }
	void Bake(@Nonnull IStatModifier mod, int level, int stackCount);
}
