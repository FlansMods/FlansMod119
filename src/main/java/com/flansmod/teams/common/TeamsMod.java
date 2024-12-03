package com.flansmod.teams.common;

import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.server.TeamsManager;
import com.flansmod.teams.server.UniversalTeamsSettings;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TeamsMod.MODID)
public class TeamsMod
{
	public static final String MODID = "flansteams";
	public static final Logger LOGGER = LogUtils.getLogger();

	private final TeamsManager manager;

	public TeamsMod()
	{
		manager = new TeamsManager();
		TeamsAPI.registerInstance(manager, manager, LOGGER);
		UniversalTeamsSettings.registerDefaultSettings();
	}
}
