package com.flansmod.teams.common.network.toserver;

import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class PlaceVoteMessage extends TeamsModMessage
{
	public int voteIndex;

	public PlaceVoteMessage() {}
	public PlaceVoteMessage(int vote) { voteIndex = vote; }

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeByte(voteIndex);
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		voteIndex = buf.readByte();
	}
}
