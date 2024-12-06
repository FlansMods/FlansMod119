package com.flansmod.teams.client;

import com.flansmod.teams.common.info.MapVotingOption;
import com.flansmod.teams.common.network.TeamsModPacketHandler;
import com.flansmod.teams.common.network.toclient.MapVotingOptionsMessage;
import com.flansmod.teams.common.network.toclient.MapVotingUpdateMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamsClientManager
{
	public final List<MapVotingOption> votingOptions = new ArrayList<>();

	public TeamsClientManager()
	{
		TeamsModPacketHandler.registerClientHandler(
			MapVotingUpdateMessage.class,
			MapVotingUpdateMessage::new,
			() -> this::receiveMapVoteUpdate
		);
		TeamsModPacketHandler.registerClientHandler(
			MapVotingOptionsMessage.class,
			MapVotingOptionsMessage::new,
			() -> this::receiveMapVoteOptions
		);
	}


	public void receiveMapVoteOptions(@Nonnull MapVotingOptionsMessage msg)
	{
		votingOptions.clear();

	}
	public void receiveMapVoteUpdate(@Nonnull MapVotingUpdateMessage msg)
	{

	}
}
