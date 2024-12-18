package com.flansmod.teams.client.gui;

import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.api.admin.GamemodeInfo;
import com.flansmod.teams.api.admin.MapInfo;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.client.gamemode.IClientGamemode;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.info.GameplayInfo;
import com.flansmod.teams.common.info.KillInfo;
import com.flansmod.teams.common.info.PlayerScoreInfo;
import com.flansmod.teams.common.info.TeamScoreInfo;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamsHUD
{
	public static final ResourceLocation hudTexture = new ResourceLocation(TeamsMod.MODID, "gui/teams_scores.png");
	private static final Map<UUID, GameProfile> profileCache = new HashMap<>();

	public void renderTeamHeader(@Nonnull RenderGuiOverlayEvent event)
	{
		GuiGraphics graphics = event.getGuiGraphics();
		Font font = Minecraft.getInstance().font;
		Player player = MinecraftHelpers.getClient().player;
		if(player == null)
			return;

		int i = MinecraftHelpers.getClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.getClient().getWindow().getGuiScaledHeight();

		IClientGamemode gamemode = TeamsModClient.MANAGER.getCurrentClientGamemode();
		GameplayInfo gameplay = TeamsModClient.MANAGER.currentState;
		MapInfo map = TeamsModClient.MANAGER.currentMap;
		List<TeamScoreInfo> teamScores = TeamsModClient.MANAGER.getNonSpectatorTeamScores();
		PlayerScoreInfo myScore = TeamsModClient.MANAGER.getMyScoreInfo();

		int secondsLeft = gameplay.ticksRemaining / 20;
		int minutesLeft = secondsLeft / 60;
		secondsLeft %= 60;

		if(myScore != null && gamemode != null)
		{
			graphics.blit(hudTexture, i/2 - 43, 0, 85, 0, 86, 27, 256, 256);

			// If we are in a two team gametype, draw the team scores at the top of the screen
			if(teamScores.size() == 2) // && teamInfo.sortedByTeam)
			{
				// Draw team 1 colour bit
				TeamScoreInfo team1 = teamScores.get(0);
				graphics.setColor(
					((team1.teamTextColour >> 16) & 0xff) / 256F,
					((team1.teamTextColour >> 8) & 0xff) / 256F,
					(team1.teamTextColour & 0xff) / 256F,
					1f);
				graphics.blit(hudTexture, i/2 - 43, 0, 0, 98, 24, 27, 256, 256);

				// Draw team 2 colour bit
				TeamScoreInfo team2 = teamScores.get(0);
				graphics.setColor(
					((team2.teamTextColour >> 16) & 0xff) / 256F,
					((team2.teamTextColour >> 8) & 0xff) / 256F,
					(team2.teamTextColour & 0xff) / 256F,
					1f);
				graphics.blit(hudTexture, i/2 + 19, 0, 62, 98, 24, 27, 256, 256);

				graphics.setColor(1f, 1f, 1f, 1f);

				// Draw the team scores
				graphics.drawString(font, team1.score + "", i / 2 - 35, 9, 0x000000);
				graphics.drawString(font, team1.score + "", i / 2 - 36, 8, 0xffffff);
				graphics.drawString(font, team2.score + "", i / 2 + 35 - font.width(team2.score + ""), 9, 0x000000);
				graphics.drawString(font, team2.score + "", i / 2 + 34 - font.width(team2.score + ""), 8, 0xffffff);
			}

			graphics.drawString(font, gamemode.getName(), i / 2 + 48, 9, 0x000000);
			graphics.drawString(font, gamemode.getName(), i / 2 + 47, 8, 0xffffff);
			graphics.drawString(font, map.mapName(), i / 2 - 47 - font.width(map.mapName()), 9, 0x000000);
			graphics.drawString(font, map.mapName(), i / 2 - 48 - font.width(map.mapName()), 8, 0xffffff);

			String timeLeft = minutesLeft + ":" + (secondsLeft < 10 ? "0" + secondsLeft : secondsLeft);
			graphics.drawString(font, timeLeft, i / 2 - font.width(timeLeft) / 2 - 1, 29, 0x000000);
			graphics.drawString(font, timeLeft, i / 2 - font.width(timeLeft) / 2, 30, 0xffffff);


			graphics.drawString(font, gamemode.getScore(TeamsAPI.SCORE_TYPE_OBJECTIVES, myScore.scores) + "", i / 2 - 7, 1, 0x000000);
			graphics.drawString(font, gamemode.getScore(TeamsAPI.SCORE_TYPE_KILLS, myScore.scores) + "", i / 2 - 7, 9, 0x000000);
			graphics.drawString(font, gamemode.getScore(TeamsAPI.SCORE_TYPE_DEATHS, myScore.scores) + "", i / 2 - 7, 17, 0x000000);
		}
	}

	@Nonnull
	private GameProfile getProfile(@Nonnull UUID id)
	{
		if(!profileCache.containsKey(id))
		{
			GameProfile profile = new GameProfile(id, null);
			Minecraft.getInstance().getMinecraftSessionService().fillProfileProperties(profile, false);
			profileCache.put(id, profile);
		}
		return profileCache.get(id);
	}

	public void renderKillMessages(@Nonnull RenderGuiOverlayEvent event)
	{
		GuiGraphics graphics = event.getGuiGraphics();
		Font font = Minecraft.getInstance().font;
		Player player = MinecraftHelpers.getClient().player;
		if(player == null)
			return;

		int i = MinecraftHelpers.getClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.getClient().getWindow().getGuiScaledHeight();

		int lineHeight = 0;
		for(KillInfo killMessage : TeamsModClient.MANAGER.kills)
		{
			GameProfile killerProfile = getProfile(killMessage.killer());
			GameProfile killedProfile = getProfile(killMessage.killed());
			ItemStack stack = ItemStack.EMPTY;
			// TODO: Locate gun

			String iconSpacer = killMessage.headshot() ? "         ":"     ";
			Component killerName = Component.translatable("teams.kill_message.killer_format", killerProfile.getName());
			Component killedName = Component.translatable("teams.kill_message.killed_format", killedProfile.getName());


			int xOffset = i - 6 - font.width(killedName);
			int yOffset = j - 32 - lineHeight * 16;

			// Render killed (going right to left)
			graphics.drawString(font, killedName, xOffset, yOffset, 0xffffff);
			xOffset -= font.width(iconSpacer);

			// Render icon
			graphics.renderItem(stack, xOffset, yOffset - 4);

			// Render killer
			xOffset -= font.width(killerName);
			graphics.drawString(font, killerName, xOffset, yOffset, 0xffffff);

			lineHeight++;
		}
	}
}
