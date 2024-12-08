package com.flansmod.teams.common.info;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public record LoadoutInfo(@Nonnull String name, @Nonnull List<ItemStack> stacks)
{
}
