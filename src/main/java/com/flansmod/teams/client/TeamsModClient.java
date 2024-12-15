package com.flansmod.teams.client;

import com.flansmod.teams.client.gamemode.TwoTeamClientGamemode;
import com.flansmod.teams.common.TeamsMod;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = TeamsMod.MODID)
public class TeamsModClient
{
	public static final TeamsClientManager MANAGER = new TeamsClientManager();

	@SubscribeEvent
	public static void clientInit(final FMLClientSetupEvent event)
	{
		//???MenuScreens.register(ChooseTeamScreen::new);
		MANAGER.registerClientGamemode("TDM", new TwoTeamClientGamemode(Component.translatable("teams.tdm.title"), Component.translatable("teams.tdm.description")));
	}

	@SubscribeEvent
	public void onKeyMappings(final RegisterKeyMappingsEvent event)
	{
		event.register(TeamsKeyMappings.TEAMS_MENU_MAPPING.get());
	}
	@SubscribeEvent
	public void clientTick(final TickEvent.ClientTickEvent event)
	{
		if(TeamsKeyMappings.TEAMS_MENU_MAPPING.get().consumeClick())
		{
			MANAGER.openBestGUI();
		}
	}

}
