package com.flansmod.common.crafting.slots;

import com.flansmod.common.crafting.menus.WorkbenchMenuGunCrafting;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.types.crafting.elements.GunCraftingEntryDefinition;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.crafting.elements.TieredIngredientDefinition;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunCraftingInputSlot extends RestrictedSlot
{
	private final WorkbenchMenuGunCrafting Menu;
	public GunCraftingInputSlot(WorkbenchMenuGunCrafting menu, Container container, int index, int x, int y)
	{
		super(container, index, x, y);
		Menu = menu;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		if(!isActive())
			return false;
		if(stack.isEmpty())
			return true;

		GunFabricationRecipe gunRecipe = Menu.BlockEntity.GetSelectedGunRecipe();
		if(gunRecipe == null)
			return false;

		int slotIndex = getSlotIndex();
		if(slotIndex >= gunRecipe.InputIngredients.size())
			return false;

		return gunRecipe.InputIngredients.get(slotIndex).test(stack);
	}
	@Override
	public void set(@Nonnull ItemStack stack)
	{
		super.set(stack);
		Menu.BlockEntity.UpdateGunCraftingOutputSlot();
	}
}
