package com.flansmod.common.types.teams;

import com.flansmod.common.types.Definitions;

public class TeamDefinitions extends Definitions<TeamDefinition>
{
	public TeamDefinitions()
	{
		super(TeamDefinition.FOLDER,
			  TeamDefinition.class,
			  TeamDefinition.INVALID,
			  TeamDefinition::new);
	}
}
