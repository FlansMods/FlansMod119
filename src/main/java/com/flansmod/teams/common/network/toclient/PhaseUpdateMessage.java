package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.api.ERoundPhase;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public class PhaseUpdateMessage extends TeamsModMessage
{
	public ERoundPhase currentPhase;
	public long startedTick;
	public long phaseLength;

	public PhaseUpdateMessage() {}
	public PhaseUpdateMessage(@Nonnull ERoundPhase phase, long tick, long length)
	{
		currentPhase = phase;
		startedTick = tick;
		phaseLength = length;
	}

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeEnum(currentPhase);
		buf.writeLong(startedTick);
		buf.writeLong(phaseLength);
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		currentPhase = buf.readEnum(ERoundPhase.class);
		startedTick = buf.readLong();
		phaseLength = buf.readLong();
	}
}
