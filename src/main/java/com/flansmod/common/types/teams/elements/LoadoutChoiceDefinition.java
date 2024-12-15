package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;

public class LoadoutChoiceDefinition
{
	@JsonField
	public String choiceName = "";
	@JsonField
	public boolean selectionMandatory = false;
	@JsonField
	public LoadoutOptionDefinition[] options = new LoadoutOptionDefinition[0];
}
