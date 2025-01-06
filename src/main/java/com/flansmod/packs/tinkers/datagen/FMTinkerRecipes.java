package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.ISmelteryRecipeHelper;
import slimeknights.tconstruct.library.data.recipe.IToolRecipeHelper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FMTinkerRecipes extends RecipeProvider implements
	IConditionBuilder,
	IMaterialRecipeHelper,
	IToolRecipeHelper,
	ISmelteryRecipeHelper,
	IRecipeHelper
{
	public FMTinkerRecipes(@Nonnull PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override @Nonnull
	public String getModId() { return FlansTinkersMod.MODID; }
	@Override
	protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer)
	{
		String toolFolder = "tools/building/";
		String partFolder = "tools/parts/";
		String castFolder = "smeltery/casts/";

		// Gun Parts
		partRecipes(consumer, FMTinkerResources.UPPER_RECEIVER, FMTinkerResources.UPPER_RECEIVER_CAST, 2, partFolder, castFolder);
		partRecipes(consumer, FMTinkerResources.LOWER_RECEIVER, FMTinkerResources.LOWER_RECEIVER_CAST, 2, partFolder, castFolder);
		partRecipes(consumer, FMTinkerResources.STOCK, FMTinkerResources.STOCK_CAST, 2, partFolder, castFolder);
		partRecipes(consumer, FMTinkerResources.GRIP, FMTinkerResources.GRIP_CAST, 2, partFolder, castFolder);
		partRecipes(consumer, FMTinkerResources.BARREL, FMTinkerResources.BARREL_CAST, 2, partFolder, castFolder);

		// Gun Types
		toolBuilding(consumer, FMTinkerResources.RIFLE, toolFolder);


		// What's an inlay?
		// castCreation(withCondition(consumer, tagConditionDomain("forge", "inlays")), FMTinkerItemTags.INLAYS, MaterialisResources.INLAY_CAST, castFolder);


	}

}
