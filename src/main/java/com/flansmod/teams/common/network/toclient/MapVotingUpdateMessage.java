package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MapVotingUpdateMessage extends TeamsModMessage
{
	public List<Integer> votesCast = new ArrayList<>();

	public MapVotingUpdateMessage() {}
	public MapVotingUpdateMessage(@Nonnull List<Integer> votes) { votesCast = votes; }

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeByte(votesCast.size());
		for(int i = 0; i < votesCast.size(); i++)
			buf.writeByte(votesCast.get(i));
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		int numOptions = buf.readByte();
		for(int i = 0; i < numOptions; i++)
			votesCast.add((int) buf.readByte());
	}
}
