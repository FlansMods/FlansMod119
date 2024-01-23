package com.flansmod.common.types.crafting.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.recipes.GunFabricationRecipe;
import com.flansmod.common.types.JsonField;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GunCraftingDefinition
{
	@JsonField
	public boolean isActive = false;
	@JsonField
	public String[] craftsByName = new String[0];
	@JsonField
	public String[] craftsByTag = new String[0];
	@JsonField
	public int maxSlots = 8;
	@JsonField
	public int FECostPerCraft = 0;

	// -----------
	// Caching
	@Nullable
	private List<ItemStack> Matches = null;
	@Nonnull
	private final HashMap<Level, List<GunFabricationRecipe>> RecipeCaches = new HashMap<>();

	@Nonnull
	public List<ItemStack> GetAllOutputs()
	{
		if (Matches == null)
		{
			Matches = new ArrayList<>();
			// Check for items by name or tag first
			List<ResourceLocation> matchResLocs = new ArrayList<>(craftsByName.length);
			List<TagKey<Item>> matchTagKeys = new ArrayList<>();
			for(String name : craftsByName)
				matchResLocs.add(new ResourceLocation(name));
			for(String tag : craftsByTag)
				matchTagKeys.add(TagKey.create(Registries.ITEM, new ResourceLocation(tag)));
			for(Item item : ForgeRegistries.ITEMS.getValues())
			{
				if(matchResLocs.contains(item.builtInRegistryHolder().key().location()))
					Matches.add(new ItemStack(item));
				else
				{
					for (TagKey<Item> tag : matchTagKeys)
						if (item.builtInRegistryHolder().is(tag))
						{
							Matches.add(new ItemStack(item));
							break;
						}
				}
			}
		}
		return Matches;
	}
	@Nonnull
	public List<GunFabricationRecipe> GetAllRecipes(@Nonnull Level level)
	{
		List<ItemStack> allOutputs = GetAllOutputs();
		if(!RecipeCaches.containsKey(level))
		{
			List<GunFabricationRecipe> recipeCache = new ArrayList<>();
			List<GunFabricationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(FlansMod.GUN_FABRICATION_RECIPE_TYPE.get());
			for (GunFabricationRecipe recipe : allRecipes)
			{
				boolean allowed = false;
				for (ItemStack validOutput : allOutputs)
					if (ItemStack.isSameItem(validOutput, recipe.getResultItem(RegistryAccess.EMPTY)))
						allowed = true;
				if(recipe.InputIngredients.size() > maxSlots)
					allowed = false;
				if (allowed)
				{
					recipeCache.add(recipe);
				}
			}
			recipeCache.sort((o1, o2) ->
			{
				if(o1 == null)
					return -1;
				if(o2 == null)
					return +1;
				int hash1 = o1.getResultItem(RegistryAccess.EMPTY).getItem().hashCode();
				int hash2 = o2.getResultItem(RegistryAccess.EMPTY).getItem().hashCode();
				if(hash1 < hash2)
					return -1;
				else if(hash2 < hash1)
					return +1;
				else
				{
					FlansMod.LOGGER.error("Equivalient recipes " + o1 + " " + o2 + "!");
					return 0;
				}

			});
			RecipeCaches.put(level, recipeCache);
		}
		return RecipeCaches.get(level);
	}
}
