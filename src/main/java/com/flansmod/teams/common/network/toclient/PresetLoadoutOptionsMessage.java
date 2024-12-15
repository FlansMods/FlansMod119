package com.flansmod.teams.common.network.toclient;

import com.flansmod.teams.common.info.LoadoutInfo;
import com.flansmod.teams.common.info.PresetLoadout;
import com.flansmod.teams.common.network.TeamsModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PresetLoadoutOptionsMessage extends TeamsModMessage
{
	public boolean andOpenGUI;
	public List<PresetLoadout> loadoutOptions;

	public PresetLoadoutOptionsMessage() { loadoutOptions = new ArrayList<>(); }
	public PresetLoadoutOptionsMessage(@Nonnull List<PresetLoadout> options, boolean andOpen)
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
			PresetLoadout loadout = loadoutOptions.get(i);
			buf.writeResourceLocation(loadout.classDef());
		}
	}

	@Override
	public void decode(FriendlyByteBuf buf)
	{
		andOpenGUI = buf.readBoolean();
		int count = buf.readInt();
		for(int i = 0; i < count; i++)
		{
			ResourceLocation classDef = buf.readResourceLocation();
			loadoutOptions.add(new PresetLoadout(classDef));
		}
	}
}
