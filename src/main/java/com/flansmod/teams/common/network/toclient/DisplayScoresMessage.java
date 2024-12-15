package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.api.admin.TeamInfo;
import com.flansmod.teams.common.info.TeamScoreInfo;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DisplayScoresMessage extends TeamsModMessage
{
	public List<TeamScoreInfo> teamScores;

	public DisplayScoresMessage() { teamScores = new ArrayList<>(); }
	public DisplayScoresMessage(@Nonnull List<TeamScoreInfo> scores)
	{
		teamScores = scores;
	}

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(teamScores.size());
		for(int i = 0; i < teamScores.size(); i++)
		{
			TeamScoreInfo teamScore = teamScores.get(i);
			buf.writeInt(teamScore.score);
			buf.writeInt(teamScore.rank);
			buf.writeResourceLocation(teamScore.teamID.teamID());
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			TeamScoreInfo teamScore = new TeamScoreInfo();
			teamScore.score = buf.readInt();
			teamScore.rank = buf.readInt();
			teamScore.teamID = new TeamInfo(buf.readResourceLocation());
			teamScores.add(teamScore);
		}
	}
}
