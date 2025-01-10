package com.flansmod.packs.tinkers.datagen;

import com.flansmod.common.FlansMod;
import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.common.registration.CastItemObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static slimeknights.tconstruct.common.TinkerTags.Items.*;

public class FMTinkerItemTags extends ItemTagsProvider
{
	public FMTinkerItemTags(@Nonnull PackOutput packOutput,
							@Nonnull CompletableFuture<HolderLookup.Provider> lookupFunc,
							@Nonnull CompletableFuture<TagLookup<Block>> blockLookupFunc,
							@Nullable ExistingFileHelper existingFileHelper)
	{
		super(packOutput, lookupFunc, blockLookupFunc, FlansTinkersMod.MODID, existingFileHelper);
	}

	public static final TagKey<Item> UPPER_RECEIVER_CAST = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/multi_use/upper_receiver"));
	public static final TagKey<Item> UPPER_RECEIVER_CAST_SINGLE = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/single_use/upper_receiver"));
	public static final TagKey<Item> LOWER_RECEIVER_CAST = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/multi_use/lower_receiver"));
	public static final TagKey<Item> LOWER_RECEIVER_CAST_SINGLE = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/single_use/lower_receiver"));
	public static final TagKey<Item> GRIP_CAST = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/multi_use/grip"));
	public static final TagKey<Item> GRIP_CAST_SINGLE = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/single_use/grip"));
	public static final TagKey<Item> STOCK_CAST = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/multi_use/stock"));
	public static final TagKey<Item> STOCK_CAST_SINGLE = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/single_use/stock"));
	public static final TagKey<Item> BARREL_CAST = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/multi_use/barrel"));
	public static final TagKey<Item> BARREL_CAST_SINGLE = ItemTags.create(new ResourceLocation(FlansTinkersMod.MODID, "casts/single_use/barrel"));

	public static final TagKey<Item> RIFLE = ItemTags.create(new ResourceLocation(FlansMod.MODID, "rifle"));


	@Override
	protected void addTags(@Nonnull HolderLookup.Provider provider)
	{
		addCastTags(FMTinkerResources.UPPER_RECEIVER_CAST);
		addCastTags(FMTinkerResources.LOWER_RECEIVER_CAST);
		addCastTags(FMTinkerResources.GRIP_CAST);
		addCastTags(FMTinkerResources.STOCK_CAST);
		addCastTags(FMTinkerResources.BARREL_CAST);


		tag(UPPER_RECEIVER_CAST).add(FMTinkerResources.UPPER_RECEIVER_CAST.get());
		tag(UPPER_RECEIVER_CAST_SINGLE).add(FMTinkerResources.UPPER_RECEIVER_CAST.getSand());
		tag(UPPER_RECEIVER_CAST_SINGLE).add(FMTinkerResources.UPPER_RECEIVER_CAST.getRedSand());
		tag(LOWER_RECEIVER_CAST).add(FMTinkerResources.LOWER_RECEIVER_CAST.get());
		tag(LOWER_RECEIVER_CAST_SINGLE).add(FMTinkerResources.LOWER_RECEIVER_CAST.getSand());
		tag(LOWER_RECEIVER_CAST_SINGLE).add(FMTinkerResources.LOWER_RECEIVER_CAST.getRedSand());
		tag(GRIP_CAST).add(FMTinkerResources.GRIP_CAST.get());
		tag(GRIP_CAST_SINGLE).add(FMTinkerResources.GRIP_CAST.getSand());
		tag(GRIP_CAST_SINGLE).add(FMTinkerResources.GRIP_CAST.getRedSand());
		tag(STOCK_CAST).add(FMTinkerResources.STOCK_CAST.get());
		tag(STOCK_CAST_SINGLE).add(FMTinkerResources.STOCK_CAST.getSand());
		tag(STOCK_CAST_SINGLE).add(FMTinkerResources.STOCK_CAST.getRedSand());
		tag(BARREL_CAST).add(FMTinkerResources.BARREL_CAST.get());
		tag(BARREL_CAST_SINGLE).add(FMTinkerResources.BARREL_CAST.getSand());
		tag(BARREL_CAST_SINGLE).add(FMTinkerResources.BARREL_CAST.getRedSand());

		tag(TOOL_PARTS).add(FMTinkerResources.UPPER_RECEIVER.get());
		tag(TOOL_PARTS).add(FMTinkerResources.LOWER_RECEIVER.get());
		tag(TOOL_PARTS).add(FMTinkerResources.GRIP.get());
		tag(TOOL_PARTS).add(FMTinkerResources.STOCK.get());
		tag(TOOL_PARTS).add(FMTinkerResources.BARREL.get());
		addAllTags(FMTinkerResources.RIFLE,
			MULTIPART_TOOL,
			MELEE,
			INTERACTABLE_RIGHT);

	}

	public final void addCastTags(CastItemObject cast)
	{
		tag(GOLD_CASTS).addOptional(FMTinkerResources.UPPER_RECEIVER_CAST.getName());

		ResourceLocation sandID = ForgeRegistries.ITEMS.getKey(FMTinkerResources.UPPER_RECEIVER_CAST.getSand());
		if(sandID != null)
			tag(SAND_CASTS).addOptional(sandID);

		ResourceLocation redSandID = ForgeRegistries.ITEMS.getKey(FMTinkerResources.UPPER_RECEIVER_CAST.getRedSand());
		if(redSandID != null)
			tag(RED_SAND_CASTS).addOptional(redSandID);
	}
	@SafeVarargs
	public final void addAllTags(ItemLike provider, TagKey<Item>... tags) {
		Item item = provider.asItem();
		for (TagKey<Item> tag : tags) {
			tag(tag).add(item);
		}
	}
}
