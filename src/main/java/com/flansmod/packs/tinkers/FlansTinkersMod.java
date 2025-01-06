package com.flansmod.packs.tinkers;

import com.flansmod.common.FlansMod;
import com.flansmod.packs.tinkers.datagen.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerPartSpriteProvider;

import javax.annotation.Nonnull;

@Mod(FlansTinkersMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FlansTinkersMod
{
	public static final String MODID = "flanstinkers";
	private static final Logger LOGGER = LogUtils.getLogger();


	public FlansTinkersMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onGatherData);
		FMTinkerResources.ITEMS.register(modEventBus);
		FMTinkerResources.BLOCKS.register(modEventBus);
		FMTinkerResources.ITEMS_EXTENDED.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onCreativeTabs(@Nonnull BuildCreativeModeTabContentsEvent event)
	{
		switch(event.getTabKey().location().getPath())
		{
			case "tool_parts" -> {
				event.accept(FMTinkerResources.UPPER_RECEIVER.get());
				event.accept(FMTinkerResources.LOWER_RECEIVER.get());
				event.accept(FMTinkerResources.BARREL.get());
				event.accept(FMTinkerResources.STOCK.get());
				event.accept(FMTinkerResources.GRIP.get());
			}
			case "smeltery" -> {
				event.accept(FMTinkerResources.UPPER_RECEIVER_CAST.get());
				event.accept(FMTinkerResources.LOWER_RECEIVER_CAST.get());
				event.accept(FMTinkerResources.BARREL_CAST.get());
				event.accept(FMTinkerResources.STOCK_CAST.get());
				event.accept(FMTinkerResources.GRIP_CAST.get());
			}
		}
	}

	public void onGatherData(final GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		boolean server = event.includeServer();
		boolean client = event.includeClient();

		// Generate Flan's Mod parts with Tinkers materials
		TinkerMaterialSpriteProvider tinkerMaterialSprites = new TinkerMaterialSpriteProvider();
		generator.addProvider(client, new MaterialPartTextureGenerator(packOutput, existingFileHelper, new FMTinkerPartTextures(), tinkerMaterialSprites));
		generator.addProvider(client, new FMTinkerItemModels(packOutput, existingFileHelper));
		generator.addProvider(client, new FMTinkerLang(packOutput));

		generator.addProvider(server, new FMTinkerToolDefinitions(packOutput));
		generator.addProvider(server, new FMTinkerRecipes(packOutput));
		BlockTagsProvider blockTags = new FMTinkerBlockTags(packOutput, event.getLookupProvider(), existingFileHelper);
		generator.addProvider(server, blockTags);
		generator.addProvider(server, new FMTinkerItemTags(packOutput, event.getLookupProvider(), blockTags.contentsGetter(), existingFileHelper));
		generator.addProvider(server, new FMTinkerToolSlotLayouts(packOutput));
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for (var entry : FMTinkerResources.ITEMS.getEntries())
			{
				event.register(new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
				shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
			}
		}
	}
}
