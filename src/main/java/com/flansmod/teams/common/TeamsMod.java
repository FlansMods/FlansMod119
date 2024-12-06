package com.flansmod.teams.common;

import com.flansmod.teams.api.GamemodeInfo;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.server.ServerEventHooks;
import com.flansmod.teams.server.TeamsManager;
import com.flansmod.teams.server.UniversalTeamsSettings;
import com.flansmod.teams.server.gamemode.Gamemode;
import com.flansmod.teams.server.gamemode.GamemodeTDM;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(TeamsMod.MODID)
public class TeamsMod
{
	public static final String MODID = "flansteams";
	public static final Logger LOGGER = LogUtils.getLogger();

	private final TeamsManager MANAGER;
	private final ServerEventHooks EVENTS;

	public static final GamemodeInfo TDM = Gamemode.createInfo("TDM", 2,GamemodeTDM::new);

	public TeamsMod()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TeamsModConfig.generalConfig, "teams-general.toml");

		MANAGER = new TeamsManager();
		TeamsAPI.registerInstance(MANAGER, MANAGER, LOGGER);
		UniversalTeamsSettings.registerDefaultSettings();
		EVENTS = new ServerEventHooks(MANAGER);

		MANAGER.registerGamemode(TDM);
	}
}
