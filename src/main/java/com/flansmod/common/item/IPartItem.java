package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Map;

public interface IPartItem
{
	@Nonnull PartDefinition Def();
	boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType);
	void CollectAbilities(@Nonnull ItemStack stack, @Nonnull Map<CraftingTraitDefinition, Integer> abilityMap);
}
