package com.flansmod.teams.api.admin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPlayerLoadout
{
	@Nonnull Component getName();
	@Nullable ResourceLocation getSkinOverride();
	@Nonnull ItemStack getStackInSlot(int slotIndex);
	@Nonnull ItemStack getEquipmentStack(@Nonnull EquipmentSlot equipSlot);
}
