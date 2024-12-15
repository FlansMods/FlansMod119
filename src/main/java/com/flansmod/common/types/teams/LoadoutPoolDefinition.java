package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.teams.elements.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class LoadoutPoolDefinition extends JsonDefinition
{
	public static final LoadoutPoolDefinition INVALID = new LoadoutPoolDefinition(new ResourceLocation(FlansMod.MODID, "loadout_pools/null"));
	public static final String TYPE = "loadout_pool";
	public static final String FOLDER = "loadout_pools";
	@Override
	public String GetTypeName() { return TYPE; }

	public LoadoutPoolDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public int maxLevel = 20;
	@JsonField
	public int xpForKill = 10;
	@JsonField
	public int xpForDeath = 5;
	@JsonField
	public int xpForKillstreakBonus = 10;
	@JsonField
	public int xpForAssist = 5;
	@JsonField
	public int xpForMultikill = 10;

	@JsonField
	public LoadoutDefinition[] defaultLoadouts = new LoadoutDefinition[0];
	@JsonField
	public String[] availableRewardBoxes = new String[0];
	@JsonField(Docs = "Level 0 will be unlocked automatically. Put starter gear in there.")
	public LevelUpDefinition[] levelUps = new LevelUpDefinition[0];

	@JsonField
	public LoadoutChoiceDefinition[] choices = new LoadoutChoiceDefinition[0];






	public void addDefaultChoices(@Nonnull Map<String, Integer> choiceMap)
	{
		for(LoadoutChoiceDefinition choice : choices)
		{
			if(choice.selectionMandatory)
				choiceMap.put(choice.choiceName, 0);
		}
	}
	private Map<String, LoadoutChoiceDefinition> SortedChoices = null;
	@Nonnull
	public Map<String, LoadoutChoiceDefinition> getSortedChoices()
	{
		if(SortedChoices == null)
		{
			SortedChoices = new HashMap<>();
			for(LoadoutChoiceDefinition choice : choices)
			{
				SortedChoices.put(choice.choiceName, choice);
			}
		}
		return SortedChoices;
	}
}

