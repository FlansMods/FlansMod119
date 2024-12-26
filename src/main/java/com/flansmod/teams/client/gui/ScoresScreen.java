package com.flansmod.teams.client.gui;

import com.flansmod.teams.api.ERoundPhase;
import com.flansmod.teams.client.TeamsClientManager;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.info.GameplayInfo;
import com.flansmod.teams.common.info.PlayerScoreInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class ScoresScreen extends AbstractTeamsScreen
{
	public static final ResourceLocation texture = new ResourceLocation("flansmod", "gui/teamsScores.png");
	public static final ResourceLocation texture2 = new ResourceLocation("flansmod", "gui/teamsScores2.png");

	public final boolean isRoundOver;
	@Override
	public boolean canBeOpenInPhase(@Nonnull ERoundPhase phase)
	{
		return phase == ERoundPhase.DisplayScores || phase == ERoundPhase.Gameplay;
	}

	public ScoresScreen(@Nonnull Component title, int guiWidth, int guiHeight, boolean isOver)
	{
		super(title, guiWidth, guiHeight);

		isRoundOver = isOver;
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float f)
	{
		super.render(graphics, mouseX, mouseY, f);
	}

	protected int getKills(@Nonnull GameplayInfo game, @Nonnull PlayerScoreInfo player) { return getScore(game, player, "kills"); }
	protected int getDeaths(@Nonnull GameplayInfo game, @Nonnull PlayerScoreInfo player) { return getScore(game, player, "deaths"); }
	protected int getScore(@Nonnull GameplayInfo game, @Nonnull PlayerScoreInfo player) { return getScore(game, player, "score"); }
	protected int getScore(@Nonnull GameplayInfo game, @Nonnull PlayerScoreInfo player, @Nonnull String scoreType)
	{
		int index = game.scoreTypes.indexOf(scoreType);
		if(index != -1)
		{
			return player.scores.get(index);
		}
		return 0;
	}
	@Nonnull
	protected Component getUsername(@Nonnull UUID playerID)
	{
		if(Minecraft.getInstance().level != null)
		{
			Player player = Minecraft.getInstance().level.getPlayerByUUID(playerID);
			if(player != null)
				return player.getName();
		}
		return Component.translatable("teams.player_name.unknown");
	}

	@Nonnull
	protected String getTimeRemaining()
	{
		TeamsClientManager teams = TeamsModClient.MANAGER;
		int ticksRemaining = teams.getTicksRemaining();
		int secondsRemaining = ticksRemaining / 20;
		ticksRemaining %= 20;
		int minutesRemaining = secondsRemaining / 60;
		secondsRemaining %= 60;

		if(minutesRemaining == 0)
			return String.format("%02d.%02d", secondsRemaining, ticksRemaining * 5);
		return String.format("%02d:%02d.%02d", minutesRemaining, secondsRemaining, ticksRemaining * 5);
	}

}
