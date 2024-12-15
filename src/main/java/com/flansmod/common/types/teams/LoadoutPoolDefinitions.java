package com.flansmod.common.types.teams;

import com.flansmod.common.types.Definitions;

public class LoadoutPoolDefinitions extends Definitions<LoadoutPoolDefinition>
{
	public LoadoutPoolDefinitions()
	{
		super(LoadoutPoolDefinition.FOLDER,
			  LoadoutPoolDefinition.class,
			  LoadoutPoolDefinition.INVALID,
			  LoadoutPoolDefinition::new);
	}
}
