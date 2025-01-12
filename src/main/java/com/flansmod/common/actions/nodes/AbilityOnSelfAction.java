package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class AbilityOnSelfAction extends ActionInstance
{

	public AbilityOnSelfAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{
		Group.Context.Gun.GetActionStack().EvaluateTrigger(
			EAbilityTrigger.Instant,
			Group.Context,
			TriggerContext.self(Group.Context.Gun.GetShooter()));
	}
}
