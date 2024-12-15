package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class LoadoutItemModifierDefinition
{
	@JsonField
	public int inventorySlot = 0;
	@JsonField
	public ItemStackDefinition item = new ItemStackDefinition();
}
