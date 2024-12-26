package com.flansmod.teams.common.info;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.flansmod.common.types.teams.LoadoutDefinition;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public record PresetLoadout(@Nonnull ResourceLocation classDef) implements IPlayerLoadout
{
	@Override @Nonnull
	public Component getName() { return Component.translatable(classDef.toLanguageKey()); }
	@Nonnull
	public LoadoutDefinition getDef() { return FlansMod.LOADOUTS.Get(classDef); }
	@Override @Nullable
	public ResourceLocation getSkinOverride() { return getDef().playerSkinOverride; }
	@Override @Nonnull
	public ItemStack getStackInSlot(int slotIndex)
	{
		ItemStackDefinition[] stacks = getDef().startingItems;
		if(slotIndex < stacks.length)
			return stacks[slotIndex].CreateStack();
		return ItemStack.EMPTY;
	}
	@Override @Nonnull
	public ItemStack getEquipmentStack(@Nonnull EquipmentSlot equipSlot)
	{
		return switch (equipSlot)
		{
			case HEAD -> getDef().hat.CreateStack();
			case CHEST -> getDef().chest.CreateStack();
			case LEGS -> getDef().legs.CreateStack();
			case FEET -> getDef().shoes.CreateStack();
			case MAINHAND, OFFHAND -> ItemStack.EMPTY;
		};
	}
}
