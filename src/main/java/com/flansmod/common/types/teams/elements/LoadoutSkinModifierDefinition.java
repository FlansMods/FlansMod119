package com.flansmod.common.types.teams.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class LoadoutSkinModifierDefinition
{
	@JsonField
	public ResourceLocation skinID = InvalidLocation;
	@JsonField
	public boolean applyToPlayer = false;
	@JsonField
	public int applyToSlot = 0;
}
