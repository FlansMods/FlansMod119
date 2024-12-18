package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.info.BuilderMapInfo;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BuilderAdminMessage extends TeamsModMessage
{
	public boolean isBuilder;
	public final List<BuilderMapInfo> mapInfo;

	public BuilderAdminMessage()
	{
		mapInfo = new ArrayList<>();
		isBuilder = false;
	}
	public BuilderAdminMessage(boolean builder, @Nonnull List<BuilderMapInfo> list)
	{
		isBuilder = builder;
		mapInfo = list;
	}

	@Override
	public void encode(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeInt(mapInfo.size());
		for(int i = 0; i < mapInfo.size(); i++)
		{
			buf.writeUtf(mapInfo.get(i).mapID());
			buf.writeBoolean(mapInfo.get(i).isConstruct());
			buf.writeInt(mapInfo.get(i).numPlayers());
		}
	}

	@Override
	public void decode(@Nonnull FriendlyByteBuf buf)
	{
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			String mapID = buf.readUtf();
			boolean construct = buf.readBoolean();
			int numPlayers = buf.readInt();
			mapInfo.add(new BuilderMapInfo(mapID, construct, numPlayers));
		}
	}
}
