package com.flansmod.teams.common.network.toserver;

import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class SelectTeamMessage extends TeamsModMessage
{
	private int selection;
	public int getSelection() { return selection; }

	public SelectTeamMessage() { selection = 0; }
	public SelectTeamMessage(int teamIndex) { selection = teamIndex; }

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(selection);
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		selection = buf.readInt();
	}
}
