package com.flansmod.packs.tinkers;

import com.flansmod.packs.tinkers.datagen.*;
import com.flansmod.packs.tinkers.materialstats.GunHandleMaterialStats;
import com.flansmod.packs.tinkers.materialstats.GunInternalMaterialStats;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.client.data.material.GeneratorPartTextureJsonGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;

import javax.annotation.Nonnull;

@Mod(FlansTinkersMod.MODID)
public class FlansTinkersMod
{
	public static final String MODID = "flanstinkers";
	private static final Logger LOGGER = LogUtils.getLogger();


	public FlansTinkersMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onGatherData);
		modEventBus.addListener(this::onCreativeTabs);
		modEventBus.addListener(this::loadComplete);
		FMTinkerResources.ITEMS.register(modEventBus);
		FMTinkerResources.BLOCKS.register(modEventBus);
		FMTinkerResources.ITEMS_EXTENDED.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void loadComplete(final FMLLoadCompleteEvent event)
	{
		MaterialRegistry.getInstance().registerStatType(GunHandleMaterialStats.TYPE);
		MaterialRegistry.getInstance().registerStatType(GunInternalMaterialStats.TYPE);
	}

	public void onCreativeTabs(@Nonnull BuildCreativeModeTabContentsEvent event)
	{
		switch(event.getTabKey().location().getPath())
		{
			case "tool_parts" -> {
				FMTinkerResources.UPPER_RECEIVER.get().addVariants(event::accept, "");
				FMTinkerResources.LOWER_RECEIVER.get().addVariants(event::accept, "");
				FMTinkerResources.BARREL.get().addVariants(event::accept, "");
				FMTinkerResources.STOCK.get().addVariants(event::accept, "");
				FMTinkerResources.GRIP.get().addVariants(event::accept, "");
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

		GeneratorPartTextureJsonGenerator.StatOverride.Builder builder = new GeneratorPartTextureJsonGenerator.StatOverride.Builder();


		builder.add(GunInternalMaterialStats.ID, MaterialIds.copper);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.iron);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.slimesteel);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.amethystBronze);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.nahuatl);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.pigIron);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.roseGold);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.cobalt);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.manyullyn);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.hepatizon);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.osmium);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.tungsten);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.platinum);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.silver);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.lead);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.aluminum);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.steel);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.bronze);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.constantan);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.invar);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.necronium);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.electrum);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.gold);
		builder.add(GunInternalMaterialStats.ID, MaterialIds.obsidian);

		builder.add(GunHandleMaterialStats.ID, MaterialIds.copper);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.iron);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.slimesteel);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.amethystBronze);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.nahuatl);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.pigIron);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.roseGold);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.cobalt);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.manyullyn);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.hepatizon);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.osmium);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.tungsten);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.platinum);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.silver);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.lead);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.aluminum);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.steel);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.bronze);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.constantan);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.invar);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.necronium);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.electrum);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.gold);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.obsidian);

		builder.add(GunHandleMaterialStats.ID, MaterialIds.wood);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.bone);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.slimewood);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.necroticBone);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.blazingBone);
		builder.add(GunHandleMaterialStats.ID, MaterialIds.platedSlimewood);

		GeneratorPartTextureJsonGenerator.StatOverride statOverride = builder.build();

		//builder.add(GunPartMaterialStats.ID, MaterialIds.searedStone);





		// Generate Flan's Mod parts with Tinkers materials
		TinkerMaterialSpriteProvider tinkerMaterialSprites = new TinkerMaterialSpriteProvider();
		generator.addProvider(client, new MaterialPartTextureGenerator(packOutput, existingFileHelper, new FMTinkerPartTextures(), statOverride, tinkerMaterialSprites));
		generator.addProvider(client, new FMTinkerItemModels(packOutput, existingFileHelper));
		generator.addProvider(client, new FMTinkerLang(packOutput));
		generator.addProvider(client, new FMTinkerToolItemModels(packOutput, existingFileHelper));

		generator.addProvider(server, new FMTinkerToolDefinitions(packOutput));
		generator.addProvider(server, new FMTinkerRecipes(packOutput));
		BlockTagsProvider blockTags = new FMTinkerBlockTags(packOutput, event.getLookupProvider(), existingFileHelper);
		generator.addProvider(server, blockTags);
		generator.addProvider(server, new FMTinkerItemTags(packOutput, event.getLookupProvider(), blockTags.contentsGetter(), existingFileHelper));
		generator.addProvider(server, new FMTinkerToolSlotLayouts(packOutput));


		MaterialDataProvider materials = new MaterialDataProvider(packOutput);
		generator.addProvider(server, materials);
		generator.addProvider(server, new FMTinkerMaterialStats(packOutput, materials));
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
