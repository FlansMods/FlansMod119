package com.flansmod.teams.common;

import com.flansmod.teams.api.admin.GamemodeInfo;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.server.ServerEventHooks;
import com.flansmod.teams.server.TeamsManager;
import com.flansmod.teams.server.UniversalTeamsSettings;
import com.flansmod.teams.server.command.CommandConstruct;
import com.flansmod.teams.server.command.CommandTeams;
import com.flansmod.teams.server.gamemode.Gamemode;
import com.flansmod.teams.server.gamemode.GamemodeTDM;
import com.mojang.logging.LogUtils;
import net.minecraft.world.inventory.MenuType;
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

	public static final GamemodeInfo TDM = Gamemode.createInfo("TDM", 2,GamemodeTDM::new);

	public TeamsMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TeamsModConfig.generalConfig, "teams-general.toml");

		MANAGER = new TeamsManager();
		TeamsAPI.registerInstance(MANAGER, MANAGER, LOGGER);
		UniversalTeamsSettings.registerDefaultSettings();
		EVENTS = new ServerEventHooks(MANAGER);

		MANAGER.registerGamemode(TDM);

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		MENUS.register(modEventBus);

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
