package com.flansmod.teams.client;

import com.flansmod.teams.api.ERoundPhase;
import com.flansmod.teams.api.TeamsAPI;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import com.flansmod.teams.client.gamemode.IClientGamemode;
import com.flansmod.teams.client.gui.AbstractTeamsScreen;
import com.flansmod.teams.client.gui.ChooseLoadoutScreen;
import com.flansmod.teams.client.gui.ChooseTeamScreen;
import com.flansmod.teams.client.gui.VotingScreen;
import com.flansmod.teams.common.TeamsMod;
import com.flansmod.teams.common.info.*;
import com.flansmod.teams.common.network.TeamsModPacketHandler;
import com.flansmod.teams.common.network.toclient.*;
import com.flansmod.teams.common.network.toserver.PlaceVoteMessage;
import com.flansmod.teams.common.network.toserver.SelectPresetLoadoutMessage;
import com.flansmod.teams.common.network.toserver.SelectTeamMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamsClientManager
{
	public final List<MapVotingOption> votingOptions = new ArrayList<>();
	public final List<ResourceLocation> teamOptions = new ArrayList<>();
	public final List<IPlayerLoadout> loadoutOptions = new ArrayList<>();

	public final List<TeamScoreInfo> scores = new ArrayList<>();
	public ResourceLocation currentGamemode = TeamsAPI.invalidGamemode;
	public String currentMap = TeamsAPI.invalidMapName;
	public GameplayInfo currentState = new GameplayInfo();
	public boolean isBuilder = false;
	public final List<BuilderMapInfo> builderInfo = new ArrayList<>();
	public final List<KillInfo> kills = new ArrayList<>();

	private final Map<String, IClientGamemode> clientGamemodes = new HashMap<>();

	public TeamsClientManager()
	{
		TeamsModPacketHandler.registerClientHandler(
			MapVotingUpdateMessage.class, MapVotingUpdateMessage::new, () -> this::receiveMapVoteUpdate);
		TeamsModPacketHandler.registerClientHandler(
			MapVotingOptionsMessage.class, MapVotingOptionsMessage::new, () -> this::receiveMapVoteOptions);
		TeamsModPacketHandler.registerClientHandler(
			TeamOptionsMessage.class, TeamOptionsMessage::new, () -> this::receiveTeamsOptions);
		TeamsModPacketHandler.registerClientHandler(
			PresetLoadoutOptionsMessage.class, PresetLoadoutOptionsMessage::new, () -> this::receiveLoadoutOptions);
		TeamsModPacketHandler.registerClientHandler(
			PhaseUpdateMessage.class, PhaseUpdateMessage::new, () -> this::receivePhaseUpdate);
		TeamsModPacketHandler.registerClientHandler(
			AddKillsMessage.class, AddKillsMessage::new, () -> this::receiveKillsInfo);

		TeamsModPacketHandler.registerClientHandler(
			BuilderAdminMessage.class, BuilderAdminMessage::new, () -> this::receiveBuilderInfo);
	}

	public void registerClientGamemode(@Nonnull String gamemodeID, @Nonnull IClientGamemode clientGamemode)
	{
		clientGamemodes.put(gamemodeID, clientGamemode);
	}

	public boolean isBuilder() { return isBuilder; }
	@Nonnull
	public List<BuilderMapInfo> getBuilderInfo() { return builderInfo; }
	@Nonnull
	public List<TeamScoreInfo> getTeamScores()
	{
		return scores;
	}
	@Nonnull
	public List<TeamScoreInfo> getNonSpectatorTeamScores()
	{
		List<TeamScoreInfo> nonSpecs = new ArrayList<>(Math.max(scores.size() - 1, 0));
		for(TeamScoreInfo teamScore : scores)
			if(!teamScore.teamID.equals(TeamsAPI.spectatorTeam))
				nonSpecs.add(teamScore);
		return nonSpecs;
	}
	@Nonnull
	public List<MapVotingOption> getMapVoteOptions()
	{
		return votingOptions;
	}

	public int getNumTeamOptions()
	{
		return teamOptions.size();
	}

	@Nonnull
	public List<ResourceLocation> getTeamOptions()
	{
		return teamOptions;
	}

	public int getNumLoadoutOptions()
	{
		return loadoutOptions.size();
	}

	@Nonnull
	public String getCurrentMap()
	{
		return currentMap;
	}

	@Nonnull
	public ResourceLocation getCurrentGamemode()
	{
		return currentGamemode;
	}

	public int getTicksRemaining()
	{
		return currentState.ticksRemaining;
	}

	public boolean isGameRunning()
	{
		return currentState.currentPhase == ERoundPhase.Gameplay;
	}

	public boolean isGameTied()
	{
		for (TeamScoreInfo score : scores)
			if (!score.isTied())
				return false;

		return true;
	}
	@Nullable
	public PlayerScoreInfo getMyScoreInfo()
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
			return null;

		for(TeamScoreInfo teamScore : getTeamScores())
		{
			for(PlayerScoreInfo playerScore : teamScore.players)
			{
				if(playerScore.playerID.equals(player.getUUID()))
					return playerScore;
			}
		}

		return null;
	}
	@Nullable
	public TeamScoreInfo getWinningTeam()
	{
		TeamScoreInfo lowestRank = null;
		for (TeamScoreInfo score : scores)
			if (lowestRank == null || score.rank < lowestRank.rank)
				lowestRank = score;

		return lowestRank;
	}

	public int getScoreLimit()
	{
		return currentState.scoreLimit;
	}

	public int getMaxPlayerCount()
	{
		int max = 0;
		for (TeamScoreInfo score : scores)
			if (score.players.size() > max)
				max = score.players.size();

		return max;
	}
	@Nullable
	public IClientGamemode getCurrentClientGamemode()
	{
		return clientGamemodes.get(currentGamemode);
	}
	@Nullable
	public IClientGamemode getClientGamemode(@Nonnull String gamemodeID)
	{
		return clientGamemodes.get(gamemodeID);
	}

	public void receiveMapVoteOptions(@Nonnull MapVotingOptionsMessage msg)
	{
		votingOptions.clear();
		votingOptions.addAll(msg.votingOptions);
		if (msg.andOpenGUI)
			openVotingGUI();
	}

	public void receiveMapVoteUpdate(@Nonnull MapVotingUpdateMessage msg)
	{
		for (int i = 0; i < votingOptions.size(); i++)
		{
			if (i < msg.votesCast.size())
				votingOptions.get(i).numVotes = msg.votesCast.get(i);
		}
	}

	public void receiveTeamsOptions(@Nonnull TeamOptionsMessage msg)
	{
		teamOptions.clear();
		teamOptions.addAll(msg.teamOptions);
		if (msg.andOpenGUI)
			openTeamSelectGUI();
	}

	public void receiveLoadoutOptions(@Nonnull PresetLoadoutOptionsMessage msg)
	{
		loadoutOptions.clear();
		loadoutOptions.addAll(msg.loadoutOptions);
		if (msg.andOpenGUI)
			openLoadoutSelectGUI();
	}
	public void receiveKillsInfo(@Nonnull AddKillsMessage msg)
	{
		kills.addAll(msg.kills);
	}
	public void receivePhaseUpdate(@Nonnull PhaseUpdateMessage msg)
	{
		currentState.currentPhase = msg.currentPhase;
		currentState.ticksRemaining = (int) (msg.startedTick + msg.phaseLength - getCurrentTime());

		if(Minecraft.getInstance().screen instanceof AbstractTeamsScreen teamsScreen)
		{
			if(!teamsScreen.canBeOpenInPhase(currentState.currentPhase))
				Minecraft.getInstance().setScreen(null);
		}
	}
	public void receiveBuilderInfo(@Nonnull BuilderAdminMessage msg)
	{
		isBuilder = msg.isBuilder;
		builderInfo.clear();
		if(isBuilder)
			builderInfo.addAll(msg.mapInfo);
	}


	public void openBestGUI()
	{
		if(currentState.isBuilder)
		{
			openBuilderGUI();
			return;
		}
		switch(currentState.currentPhase)
		{
			case MapVote -> openVotingGUI();
			case Gameplay -> openTeamSelectGUI();
			default -> {}
		}
	}
	public void openBuilderGUI()
	{

	}
	public void openTeamSelectGUI()
	{
		if(teamOptions.size() > 0)
			Minecraft.getInstance().setScreen(new ChooseTeamScreen(Component.translatable("teams.gui.team_select")));
		else
			TeamsMod.LOGGER.warn("Tried to open TeamSelect GUI without any teams to select from");
	}
	public void openLoadoutSelectGUI()
	{
		if(loadoutOptions.size() > 0)
			Minecraft.getInstance().setScreen(new ChooseLoadoutScreen(Component.translatable("teams.gui.loadout_select")));
		else
			TeamsMod.LOGGER.warn("Tried to open LoadoutSelect GUI without any loadouts to select from");
	}
	public void openVotingGUI()
	{
		if(votingOptions.size() > 0)
			Minecraft.getInstance().setScreen(new VotingScreen(Component.translatable("teams.gui.voting")));
		else
			TeamsMod.LOGGER.warn("Tried to open Voting GUI without any options to vote on");
	}


	public void sendLoadoutChoice(int loadoutIndex)
	{
		TeamsModPacketHandler.INSTANCE.sendToServer(new SelectPresetLoadoutMessage(loadoutIndex));
	}
	public void sendTeamChoice(int teamIndex)
	{
		TeamsModPacketHandler.INSTANCE.sendToServer(new SelectTeamMessage(teamIndex));
	}
	public void placeVote(int voteIndex)
	{
		TeamsModPacketHandler.INSTANCE.sendToServer(new PlaceVoteMessage(voteIndex));
	}
	private long getCurrentTime()
	{
		if(Minecraft.getInstance().level != null)
			return Minecraft.getInstance().level.getGameTime();
		return 0L;
	}
}
