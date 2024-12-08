package com.flansmod.teams.common.network.toserver;

import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

public class SelectPresetLoadoutMessage extends TeamsModMessage
{
	public int loadoutIndex;

	public SelectPresetLoadoutMessage() { }
	public SelectPresetLoadoutMessage(int index) { loadoutIndex = index; }

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(loadoutIndex);
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		loadoutIndex = buf.readInt();
	}
}
