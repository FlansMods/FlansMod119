package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;

public class LoadoutOptionDefinition
{
	@JsonField
	public EUnlockType unlockType = EUnlockType.Unlocked;
	@JsonField
	public int unlockAtRank = 0;
	@JsonField
	public String externalUnlockKey = "";

	@JsonField
	public LoadoutItemModifierDefinition[] addItems = new LoadoutItemModifierDefinition[0];
	@JsonField
	public LoadoutAttachmentModifierDefinition[] attachItems = new LoadoutAttachmentModifierDefinition[0];
	@JsonField
	public LoadoutSkinModifierDefinition[] changeSkins = new LoadoutSkinModifierDefinition[0];

}
