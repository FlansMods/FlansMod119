package com.flansmod.common.item;

import com.flansmod.api.IAmmoItem;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.bullets.BulletBagDefinition;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BulletBagItem extends FlanItem implements IForgeItem, IAmmoItem
{
	@Override
	public BulletBagDefinition Def() { return FlansMod.BULLET_BAGS.Get(DefinitionLocation);	}

	public BulletBagItem(@Nonnull ResourceLocation defLoc, @Nonnull Properties properties)
	{
		super(defLoc, properties);
	}



	@Nonnull
	public List<ItemStack> getBulletStacks(@Nonnull ItemStack stack)
	{
		if(stack.hasTag())
		{
			if(stack.getTag().contains("bullets"))
			{
				ImmutableList.Builder<ItemStack> listBuilder = ImmutableList.builder();
				ListTag tagList = stack.getTag().getList("bullets", 10);
				tagList.forEach(tag -> {
					listBuilder.add(ItemStack.of((CompoundTag) tag));
				});
				return listBuilder.build();
			}
		}
		return List.of();
	}
	public void setBulletStacks(@Nonnull ItemStack stack, @Nonnull List<ItemStack> subStacks)
	{
		ListTag tagList = new ListTag();
		int numStacks = 0;

		for(ItemStack subStack : subStacks)
		{
			if(subStack.isEmpty())
				continue;

			if(numStacks >= Def().slotCount)
			{
				FlansMod.LOGGER.warn("Trying to store more bullets in a BulletBag than slots");
				continue;
			}

			tagList.add(subStack.save(new CompoundTag()));
			numStacks++;
		}

		stack.getOrCreateTag().put("bullets", tagList);
	}

	// IAmmoItem
	@Nonnull @Override
	public ItemStack provideAny(@Nonnull ItemStack fromStack)
	{
		ItemStack provided = ItemStack.EMPTY;
		List<ItemStack> contents = getBulletStacks(fromStack);
		for(ItemStack subStack : contents)
		{
			if(subStack.getItem() instanceof IAmmoItem ammoItem)
			{
				provided = ammoItem.provideAny(subStack);
				break;
			}
		}
		setBulletStacks(fromStack, contents);
		return provided;
	}
	@Override
	public boolean matchesTags(@Nonnull ItemStack fromStack, @Nonnull List<TagKey<Item>> matchTags)
	{
		List<ItemStack> contents = getBulletStacks(fromStack);
		for(ItemStack subStack : contents)
			if (subStack.getItem() instanceof IAmmoItem ammoItem)
				if(ammoItem.matchesTags(subStack, matchTags))
					return true;
		return false;
	}
	@Nonnull @Override
	public ItemStack provideForTags(@Nonnull ItemStack fromStack, @Nonnull List<TagKey<Item>> matchTags)
	{
		List<ItemStack> contents = getBulletStacks(fromStack);
		for(ItemStack subStack : contents)
			if (subStack.getItem() instanceof IAmmoItem ammoItem)
				if(ammoItem.matchesTags(subStack, matchTags))
					return ammoItem.provideForTags(subStack, matchTags);
		return ItemStack.EMPTY;
	}
	@Override
	public boolean matchesIDs(@Nonnull ItemStack fromStack, @Nonnull List<ResourceLocation> matchIDs)
	{
		List<ItemStack> contents = getBulletStacks(fromStack);
		for(ItemStack subStack : contents)
			if (subStack.getItem() instanceof IAmmoItem ammoItem)
				if(ammoItem.matchesIDs(subStack, matchIDs))
					return true;
		return false;
	}
	@Nonnull @Override
	public ItemStack provideForIDs(@Nonnull ItemStack fromStack, @Nonnull List<ResourceLocation> matchIDs)
	{
		List<ItemStack> contents = getBulletStacks(fromStack);
		for(ItemStack subStack : contents)
			if (subStack.getItem() instanceof IAmmoItem ammoItem)
				if(ammoItem.matchesIDs(subStack, matchIDs))
					return ammoItem.provideForIDs(subStack, matchIDs);
		return ItemStack.EMPTY;
	}



	@Override
	public void appendHoverText(@Nonnull ItemStack stack,
								@Nullable Level level,
								@Nonnull List<Component> tooltips,
								@Nonnull TooltipFlag flags)
	{
		super.appendHoverText(stack, level, tooltips, flags);

		List<ItemStack> subStacks = getBulletStacks(stack);

		tooltips.add(Component.translatable("flansmod.bullet_bag.fullness", subStacks.size(), Def().slotCount));
		for(ItemStack subStack : subStacks)
		{
			tooltips.add(Component.translatable("flansmod.bullet_bag.substack", subStack.getDisplayName(), subStack.getCount(), Def().maxStackSize));
		}
	}
}
