package com.flansmod.teams.common.network.toserver;

import com.flansmod.teams.api.TeamInfo;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public class SelectTeamMessage extends TeamsModMessage
{
	private TeamInfo selection;
	@Nonnull
	public TeamInfo getSelection() { return selection; }

	public SelectTeamMessage() { selection = TeamInfo.invalid; }
	public SelectTeamMessage(@Nonnull TeamInfo teamInfo) { selection = teamInfo; }

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeResourceLocation(selection.teamID());
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		selection = new TeamInfo(buf.readResourceLocation());
	}
}
