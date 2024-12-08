package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.api.MapInfo;
import com.flansmod.teams.common.info.MapVotingOption;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MapVotingOptionsMessage extends TeamsModMessage
{
	public List<MapVotingOption> votingOptions;
	public boolean andOpenGUI;


	public MapVotingOptionsMessage() { votingOptions = new ArrayList<>(); }
	public MapVotingOptionsMessage(@Nonnull List<MapVotingOption> list, boolean andOpen)
	{
		votingOptions = list;
		andOpenGUI = andOpen;
	}

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(andOpenGUI);
		buf.writeByte(votingOptions.size());
		for(int i = 0; i < votingOptions.size(); i++)
		{
			buf.writeUtf(votingOptions.get(i).mapID);
			buf.writeByte(votingOptions.get(i).numVotes);
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		andOpenGUI = buf.readBoolean();
		int numOptions = buf.readByte();
		for(int i = 0; i < numOptions; i++)
		{
			String mapName = buf.readUtf();
			int numVotes = buf.readByte();

			MapInfo mapInfo = new MapInfo(mapName, null);
			votingOptions.add(new MapVotingOption(mapInfo, numVotes));
		}
	}
}
