package com.flansmod.teams.api.admin;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.TeamsAPI;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public record RoundInfo(@Nonnull ResourceLocation gamemodeID,
						@Nonnull String mapName,
						@Nonnull List<ResourceLocation> teams,
						@Nonnull String settingsName)
{
	public static final RoundInfo invalid = new RoundInfo(
		TeamsAPI.invalidGamemode,
		TeamsAPI.invalidMapName,
		List.of(),
		TeamsAPI.defaultSettingsName);

	@Nonnull
	public static RoundInfo of(@Nonnull CompoundTag tags)
	{
		ResourceLocation gamemodeLoc = ResourceLocation.tryParse(tags.getString("gamemode"));
		if(gamemodeLoc == null)
			gamemodeLoc = TeamsAPI.invalidGamemode;

		String mapName = tags.getString("mapName");

		ImmutableList.Builder<ResourceLocation> teams = ImmutableList.builder();
		ListTag teamsTag = tags.getList("teams", 8);
		for(int i = 0; i < teamsTag.size(); i++)
		{
			ResourceLocation loc = ResourceLocation.tryParse(teamsTag.getString(i));
			if(loc != null)
				teams.add(loc);
			else
				teams.add(TeamsAPI.invalidTeam);
		}

		String settingsName = tags.getString("settings");

		return new RoundInfo(gamemodeLoc, mapName, teams.build(), settingsName);
	}

	public void saveTo(@Nonnull CompoundTag tags)
	{
		tags.putString("gamemode", gamemodeID.toString());
		tags.putString("mapName", mapName);
		ListTag teamsTag = new ListTag();
		for(ResourceLocation teamLoc : teams)
			teamsTag.add(StringTag.valueOf(teamLoc.toString()));
		tags.put("teams", teamsTag);
		tags.putString("settings", settingsName);
	}

	@Nonnull
	public OpResult validate()
	{
		if(!TeamsAPI.isValidMapName(mapName()))
			return OpResult.FAILURE_INVALID_MAP_NAME;
		if(!TeamsAPI.isValidGamemodeID(gamemodeID()))
			return OpResult.FAILURE_INVALID_GAMEMODE_ID;
		for(ResourceLocation team : teams)
			if(!TeamsAPI.isValidTeamID(team))
				return OpResult.FAILURE_INVALID_MAP_NAME;

		return OpResult.SUCCESS;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(gamemodeID).append(" @ ").append(mapName).append(" (");
		for(int i = 0; i < teams.size(); i++)
		{
			builder.append(teams.get(i));
			if(i != teams.size() - 1)
				builder.append(", ");
		}
		return builder.append(")").toString();
	}
}
