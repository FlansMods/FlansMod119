package com.flansmod.teams.server;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.teams.TeamDefinition;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import com.flansmod.teams.api.runtime.ITeamInstance;
import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.common.info.PresetLoadout;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class TeamInstance implements ITeamInstance
{
	private final ResourceLocation teamID;
	private final LazyDefinition<TeamDefinition> teamDef;
	private final Map<UUID, Player> teamMembers = new HashMap<>();
	private final Map<String, Integer> scores = new HashMap<>();
	public final List<PresetLoadout> presetLoadouts = new ArrayList<>();

	public TeamInstance(@Nonnull ResourceLocation id)
	{
		teamID = id;
		teamDef = LazyDefinition.of(id, FlansMod.TEAMS);
	}

	@Override @Nonnull
	public ResourceLocation getTeamID() { return teamID; }
	@Nonnull
	public TeamDefinition getDef() { return teamDef.DefGetter().get(); }
	@Override
	public int getNumPresetLoadouts() { return getDef().classes.length; }
	@Override @Nonnull
	public IPlayerLoadout getPresetLoadout(int index)
	{
		if(presetLoadouts.size() == 0)
		{
			for(int i = 0; i < getDef().classes.length; i++)
			{
				presetLoadouts.add(new PresetLoadout(getDef().classes[i]));
			}
		}
		return presetLoadouts.get(index);
	}

	@Override
	public int getScore(@Nonnull String scoreType) { return scores.getOrDefault(scoreType, 0); }
	@Override @Nonnull
	public OpResult resetScore(@Nonnull String scoreType)
	{
		scores.remove(scoreType);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult resetAllScores()
	{
		scores.clear();
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult addScore(@Nonnull String scoreType, int add)
	{
		scores.put(scoreType, getScore(scoreType) + add);
		return OpResult.SUCCESS;
	}

	@Override @Nonnull
	public Set<UUID> getMemberUUIDs() { return teamMembers.keySet(); }
	@Override @Nonnull
	public Collection<Player> getMemberPlayers() { return teamMembers.values(); }
	@Override
	public boolean canAdd(@Nonnull Player player) { return true; }
	@Override
	public boolean add(@Nonnull Player player)
	{
		teamMembers.put(player.getUUID(), player);
		return true;
	}
	@Override
	public boolean remove(@Nonnull Player player)
	{
		teamMembers.remove(player.getUUID());
		return true;
	}
	@Override
	public boolean owns(@Nonnull Entity entity)
	{
		if(entity instanceof Player player)
			return teamMembers.containsValue(player);

		return false;
	}
}
