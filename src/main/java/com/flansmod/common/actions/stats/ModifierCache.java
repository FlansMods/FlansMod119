package com.flansmod.common.actions.stats;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ModifierCache implements IModifierBaker
{
	private boolean IsCurrent = false;
	private final Map<String, StatAccumulator> ModifierFormulae = new HashMap<>();
	private final Map<String, String> StringOverrides = new HashMap<>();
	private final Consumer<IModifierBaker> BakeFunc;

	public ModifierCache(@Nonnull Consumer<IModifierBaker> bakeFunc)
	{
		BakeFunc = bakeFunc;
	}
	public boolean IsUpToDate()
	{
		return IsCurrent;
	}
	public void CheckBake()
	{
		if(!IsCurrent)
		{
			Clear();
			BakeFunc.accept(this);
			IsCurrent = true;
		}
	}


	@Nonnull
	public StatAccumulator GetModifierFormula(@Nonnull String stat)
	{
		CheckBake();
		return ModifierFormulae.getOrDefault(stat, StatAccumulator.EmptyAccumulator);
	}
	@Nonnull
	public Optional<String> GetStringOverride(@Nonnull String stat)
	{
		CheckBake();
		return Optional.ofNullable(StringOverrides.get(stat));
	}

	public void Clear()
	{
		ModifierFormulae.clear();
		StringOverrides.clear();
		IsCurrent = false;
	}

	@Override
	public void Bake(@Nonnull IStatModifier mod, int level, int stackCount)
	{
		if(!IsCurrent)
		{
			// Is this a float modifier?
			if (mod.getAccumulators().length > 0)
			{
				StatAccumulator existing = ModifierFormulae.get(mod.getStat());
				if(existing == null)
				{
					existing = new StatAccumulator();
					ModifierFormulae.put(mod.getStat(), existing);
				}

				existing.Stack(level, stackCount, mod.getAccumulators());
			}
			if (!mod.getSetValue().isEmpty())
			{
				StringOverrides.put(mod.getStat(), mod.getSetValue());
			}
		}
		else FlansMod.LOGGER.error("Can't modify a finished ModifierCache");
	}
}
