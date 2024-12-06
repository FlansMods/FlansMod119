package com.flansmod.teams.common.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract class TeamsModMessage
{
	public abstract void encode(FriendlyByteBuf buf);
	public abstract void decode(FriendlyByteBuf buf);
}
