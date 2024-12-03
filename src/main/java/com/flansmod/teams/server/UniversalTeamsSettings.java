package com.flansmod.teams.server;

import com.flansmod.teams.api.ITeamsAdmin;
import com.flansmod.teams.api.TeamsAPI;

public class UniversalTeamsSettings
{
	public static final String AUTO_BALANCE = "autoBalance";
	public static final String VEHICLES_BREAK_BLOCKS = "vehiclesBreakBlocks";

	public static void registerDefaultSettings()
	{
		ITeamsAdmin admin = TeamsAPI.getAdmin();
		if(admin != null)
		{
			admin.registerBooleanParameter(AUTO_BALANCE, true);
			admin.registerBooleanParameter(VEHICLES_BREAK_BLOCKS, false);
		}
	}
}
