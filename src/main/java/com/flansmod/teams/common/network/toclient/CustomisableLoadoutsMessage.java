package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.info.CustomLoadout;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CustomisableLoadoutsMessage extends TeamsModMessage
{
	public boolean andOpenGUI;
	public List<CustomLoadout> loadoutOptions;

	public CustomisableLoadoutsMessage() { loadoutOptions = new ArrayList<>(); }
	public CustomisableLoadoutsMessage(@Nonnull List<CustomLoadout> options, boolean andOpen)
	{
		loadoutOptions = options;
		andOpenGUI = andOpen;
	}

	@Override
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeBoolean(andOpenGUI);
		buf.writeInt(loadoutOptions.size());
		for(int i = 0; i < loadoutOptions.size(); i++)
		{
			CustomLoadout loadout = loadoutOptions.get(i);
			buf.writeResourceLocation(loadout.loadoutPoolDef);
			buf.writeInt(loadout.loadoutChoices.size());
			for(var kvp : loadout.loadoutChoices.entrySet())
			{
				buf.writeInt(kvp.getKey().hashCode());
				buf.writeInt(kvp.getValue());
			}
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		andOpenGUI = buf.readBoolean();
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			CustomLoadout loadout = new CustomLoadout(buf.readResourceLocation());
			int numItems = buf.readInt();
			for(int j = 0; j < numItems; j++)
			{
				int choiceHash = buf.readInt();
				int selection = buf.readInt();
				loadout.hashedLoadoutChoices.put(choiceHash, selection);
			}

			loadoutOptions.add(loadout);
		}
	}
}
