package com.flansmod.plugins.tinkers;

import com.flansmod.common.FlansMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import javax.annotation.Nonnull;

public class FlansModTinkersPlugin
	implements FlansModTinkersConstructIntegration.ITinkersIntegration
{
	public static final RegistryObject<ToolPartItem> upperReceiver = FlansMod.ITEMS.register("tc_upper_receiver",
		() -> new ToolPartItem(new Item.Properties(), GunPartMaterialStats.ID));
	public static final RegistryObject<ToolPartItem> lowerReceiver = FlansMod.ITEMS.register("tc_lower_receiver",
		() -> new ToolPartItem(new Item.Properties(), GunPartMaterialStats.ID));
	public static final RegistryObject<ToolPartItem> barrel = FlansMod.ITEMS.register("tc_barrel",
		() -> new ToolPartItem(new Item.Properties(), GunPartMaterialStats.ID));
	public static final RegistryObject<ToolPartItem> stock = FlansMod.ITEMS.register("tc_stock",
		() -> new ToolPartItem(new Item.Properties(), GunPartMaterialStats.ID));

	public void init()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onCreativeTabs(@Nonnull BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey().location().getPath().equals("tool_parts"))
		{
			event.accept(upperReceiver);
			event.accept(lowerReceiver);
			event.accept(barrel);
			event.accept(stock);
		}
	}

	@Override @Nonnull
	public Item createPartItem(@Nonnull ResourceLocation loc)
	{
		return new TCPartItem(loc, GunPartMaterialStats.ID, new Item.Properties());
	}
}
