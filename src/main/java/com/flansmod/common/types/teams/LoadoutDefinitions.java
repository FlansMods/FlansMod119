package com.flansmod.common.types.teams;

import com.flansmod.common.types.Definitions;

public class LoadoutDefinitions extends Definitions<LoadoutDefinition>
{
	public LoadoutDefinitions()
	{
		super(LoadoutDefinition.FOLDER,
			  LoadoutDefinition.class,
			  LoadoutDefinition.INVALID,
			  LoadoutDefinition::new);
	}
}
