package com.flansmod.packs.tinkers;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.IPartItem;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import javax.annotation.Nonnull;
import java.util.Map;

public class TCPartItem extends ToolPartItem implements IPartItem
{
	private final ResourceLocation DefinitionLocation;

	public TCPartItem(@Nonnull ResourceLocation partDefLoc, @Nonnull MaterialStatsId materialID, @Nonnull Properties properties)
	{
		super(properties, materialID);

		DefinitionLocation = partDefLoc;
	}

	@Override @Nonnull
	public PartDefinition Def() { return FlansMod.PARTS.Get(DefinitionLocation); }
	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }
	@Override
	public void CollectAbilities(@Nonnull ItemStack stack, @Nonnull Map<CraftingTraitDefinition, Integer> abilityMap)
	{
		for(CraftingTraitProviderDefinition provider : Def().traits)
			abilityMap.put(provider.GetAbility(), provider.level);
	}
}
