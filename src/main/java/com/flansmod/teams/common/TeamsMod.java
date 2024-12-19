package com.flansmod.teams.common;

import com.flansmod.common.FlansMod;
import com.flansmod.teams.api.admin.GamemodeInfo;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.common.block.BlockEntitySpawnPoint;
import com.flansmod.teams.common.block.BlockSpawnPoint;
import com.flansmod.teams.server.ServerEventHooks;
import com.flansmod.teams.server.TeamsManager;
import com.flansmod.teams.server.UniversalTeamsSettings;
import com.flansmod.teams.server.command.CommandConstruct;
import com.flansmod.teams.server.command.CommandTeams;
import com.flansmod.teams.server.gamemode.Gamemode;
import com.flansmod.teams.server.gamemode.GamemodeTDM;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

@Mod(TeamsMod.MODID)
public class TeamsMod
{
	public static final String MODID = "flansteams";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static TeamsManager MANAGER;
	private final ServerEventHooks EVENTS;
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
	//public static final RegistryObject<MenuType<ChooseTeamMenu>> MENU_CHOOSE_TEAM = MENUS.register("choose_team", () -> IForgeMenuType.create(ChooseTeamMenu::new));

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);


	public static final RegistryObject<Block> SPAWN_POINT_BLOCK = BLOCKS.register("spawn_point", () -> new BlockSpawnPoint(BlockBehaviour.Properties.copy(Blocks.BEDROCK).dynamicShape()));
	public static final RegistryObject<Item> SPAWN_POINT_BLOCK_ITEM = ITEMS.register("spawn_point", () -> new BlockItem(SPAWN_POINT_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<BlockEntityType<?>> SPAWN_POINT_BLOCK_ENTITY_TYPE = TILE_ENTITIES.register("spawn_point",
		() -> BlockEntityType.Builder.of(BlockEntitySpawnPoint::new, SPAWN_POINT_BLOCK.get()).build(null));

	public static final GamemodeInfo TDM = new GamemodeInfo("TDM");

	public TeamsMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TeamsModConfig.generalConfig, "teams-general.toml");

		MANAGER = new TeamsManager();
		TeamsAPI.registerInstance(MANAGER, MANAGER, LOGGER);
		UniversalTeamsSettings.registerDefaultSettings();
		EVENTS = new ServerEventHooks(MANAGER);

		MANAGER.registerGamemode(TDM, Gamemode.createFactory("TDM", 2, GamemodeTDM::new));

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		MENUS.register(modEventBus);
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);


		MinecraftForge.EVENT_BUS.register(this);
	}

	private void commonInit(final FMLCommonSetupEvent event)
	{

	}

	@SubscribeEvent
	public void onRegisterCommands(@Nonnull RegisterCommandsEvent event)
	{
		CommandTeams.register(event.getDispatcher(), event.getBuildContext());
		CommandConstruct.register(event.getDispatcher(), event.getBuildContext());
	}
}
