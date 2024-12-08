package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.info.LoadoutInfo;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PresetLoadoutOptionsMessage extends TeamsModMessage
{
	public boolean andOpenGUI;
	public List<LoadoutInfo> loadoutOptions;

	public PresetLoadoutOptionsMessage() { loadoutOptions = new ArrayList<>(); }
	public PresetLoadoutOptionsMessage(@Nonnull List<LoadoutInfo> options, boolean andOpen)
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
			LoadoutInfo loadout = loadoutOptions.get(i);
			buf.writeUtf(loadout.name());
			buf.writeInt(loadout.stacks().size());
			for(int j = 0; j < loadout.stacks().size(); j++)
			{
				buf.writeItemStack(loadout.stacks().get(j), true);
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
			String name = buf.readUtf();
			int numItems = buf.readInt();
			List<ItemStack> stacks = new ArrayList<>(numItems);
			for(int j = 0; j < numItems; j++)
			{
				stacks.add(buf.readItem());
			}

			loadoutOptions.add(new LoadoutInfo(name, stacks));
		}
	}
}
