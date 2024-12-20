package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamOptionsMessage extends TeamsModMessage
{
	public List<ResourceLocation> teamOptions;
	public boolean andOpenGUI;

	public TeamOptionsMessage() { teamOptions = new ArrayList<>(); }
	public TeamOptionsMessage(@Nonnull List<ResourceLocation> options, boolean andOpen)
	{
		teamOptions = options;
		andOpenGUI = andOpen;
	}

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(andOpenGUI);
		buf.writeInt(teamOptions.size());
		for(int i = 0; i < teamOptions.size(); i++)
		{
			buf.writeResourceLocation(teamOptions.get(i));
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		andOpenGUI = buf.readBoolean();
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			teamOptions.add(buf.readResourceLocation());
		}
	}
}
