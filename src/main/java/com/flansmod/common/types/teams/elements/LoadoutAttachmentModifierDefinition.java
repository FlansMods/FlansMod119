package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;

public class LoadoutAttachmentModifierDefinition
{
	@JsonField
	public int inventorySlot = 0;
	@JsonField
	public ItemStackDefinition attachmentItem = new ItemStackDefinition();
}
