package com.flansmod.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IAmmoItem
{
	@Nonnull
	ItemStack provideAny(@Nonnull ItemStack fromStack);

	boolean matchesTags(@Nonnull ItemStack fromStack, @Nonnull List<TagKey<Item>> matchTags);
	@Nonnull
	ItemStack provideForTags(@Nonnull ItemStack fromStack, @Nonnull List<TagKey<Item>> matchTags);

	boolean matchesIDs(@Nonnull ItemStack fromStack, @Nonnull List<ResourceLocation> matchIDs);
	@Nonnull
	ItemStack provideForIDs(@Nonnull ItemStack fromStack, @Nonnull List<ResourceLocation> matchIDs);




}
