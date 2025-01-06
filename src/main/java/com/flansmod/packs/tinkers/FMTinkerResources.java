package com.flansmod.packs.tinkers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import java.util.function.Supplier;

public class FMTinkerResources
{
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, FlansTinkersMod.MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FlansTinkersMod.MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FlansTinkersMod.MODID);
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FlansTinkersMod.MODID);
	protected static final ItemDeferredRegisterExtension ITEMS_EXTENDED =
		new ItemDeferredRegisterExtension(FlansTinkersMod.MODID);

	public static final CastItemObject UPPER_RECEIVER_CAST = ITEMS_EXTENDED.registerCast("upper_receiver", new Item.Properties());
	public static final ItemObject<ToolPartItem> UPPER_RECEIVER = ITEMS_EXTENDED.register("upper_receiver",
		() -> new TCPartItem(new ResourceLocation(FlansTinkersMod.MODID, "upper_receiver"), GunPartMaterialStats.ID, new Item.Properties()));

	public static final CastItemObject LOWER_RECEIVER_CAST = ITEMS_EXTENDED.registerCast("lower_receiver", new Item.Properties());
	public static final ItemObject<ToolPartItem> LOWER_RECEIVER = ITEMS_EXTENDED.register("lower_receiver",
		() -> new TCPartItem(new ResourceLocation(FlansTinkersMod.MODID, "lower_receiver"), GunPartMaterialStats.ID, new Item.Properties()));

	public static final CastItemObject BARREL_CAST = ITEMS_EXTENDED.registerCast("barrel", new Item.Properties());
	public static final ItemObject<ToolPartItem> BARREL = ITEMS_EXTENDED.register("barrel",
		() -> new TCPartItem(new ResourceLocation(FlansTinkersMod.MODID, "barrel"), GunPartMaterialStats.ID, new Item.Properties()));

	public static final CastItemObject STOCK_CAST = ITEMS_EXTENDED.registerCast("stock", new Item.Properties());
	public static final ItemObject<ToolPartItem> STOCK = ITEMS_EXTENDED.register("stock",
		() -> new TCPartItem(new ResourceLocation(FlansTinkersMod.MODID, "stock"), GunPartMaterialStats.ID, new Item.Properties()));

	public static final CastItemObject GRIP_CAST = ITEMS_EXTENDED.registerCast("grip", new Item.Properties());
	public static final ItemObject<ToolPartItem> GRIP = ITEMS_EXTENDED.register("grip",
		() -> new TCPartItem(new ResourceLocation(FlansTinkersMod.MODID, "grip"), GunPartMaterialStats.ID, new Item.Properties()));



	public static final ResourceLocation DUMMY_RIFLE_DEFINITION = new ResourceLocation(FlansTinkersMod.MODID, "rifle");
	public static final ToolDefinition RIFLE_DEFINITION =
		ToolDefinition.create(DUMMY_RIFLE_DEFINITION);
	public static final ItemObject<ModifiableGunItem> RIFLE =
		ITEMS_EXTENDED.register("rifle", () -> new ModifiableGunItem(DUMMY_RIFLE_DEFINITION, new Item.Properties(), RIFLE_DEFINITION));





}
