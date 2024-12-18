package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.info.BuilderMapInfo;
import com.flansmod.teams.common.info.KillInfo;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddKillsMessage extends TeamsModMessage
{
	public final List<KillInfo> kills;

	public AddKillsMessage() { kills = new ArrayList<>(); }
	public AddKillsMessage(@Nonnull List<KillInfo> list) { kills = list; }

	@Override
	public void encode(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeInt(kills.size());
		for(int i = 0; i < kills.size(); i++)
		{
			buf.writeUUID(kills.get(i).killer());
			buf.writeUUID(kills.get(i).killed());
			buf.writeUUID(kills.get(i).weaponID());
			buf.writeLong(kills.get(i).tick());
			buf.writeBoolean(kills.get(i).headshot());
		}
	}

	@Override
	public void decode(@Nonnull FriendlyByteBuf buf)
	{
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			UUID killer = buf.readUUID();
			UUID killed = buf.readUUID();
			UUID weapon = buf.readUUID();
			long tick = buf.readLong();
			boolean headshot = buf.readBoolean();
			kills.add(new KillInfo(killer, killed, weapon, tick, headshot));
		}
	}
}
