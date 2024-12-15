package com.flansmod.common.types.teams;

import com.flansmod.common.types.Definitions;

public class ClassDefinitions extends Definitions<ClassDefinition>
{
	public ClassDefinitions()
	{
		super(ClassDefinition.FOLDER,
			  ClassDefinition.class,
			  ClassDefinition.INVALID,
			  ClassDefinition::new);
	}
}
