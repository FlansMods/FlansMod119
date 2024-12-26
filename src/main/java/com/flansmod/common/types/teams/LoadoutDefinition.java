package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;
import net.minecraft.resources.ResourceLocation;

public class LoadoutDefinition extends JsonDefinition
{
	public static final LoadoutDefinition INVALID = new LoadoutDefinition(new ResourceLocation(FlansMod.MODID, "loadouts/null"));
	public static final String TYPE = "loadout";
	public static final String FOLDER = "loadouts";
	@Override
	public String GetTypeName() { return TYPE; }

	public LoadoutDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField(Docs = "These will be inserted into the inventory in order. You could add stacks of air if you want to space things out")
	public ItemStackDefinition[] startingItems = new ItemStackDefinition[0];

	@JsonField(Docs = "Leave empty for no spawning. Use standard minecraft entity tag formatting")
	public String spawnOnEntity = "";

	@JsonField(Docs = "Leave blank to disable. Renders the player with this skin instead of their own")
	public ResourceLocation playerSkinOverride = InvalidLocation;

	@JsonField(Docs = "Leave this as empty to take from the team settings")
	public ItemStackDefinition hat = new ItemStackDefinition();
	@JsonField(Docs = "Leave this as empty to take from the team settings")
	public ItemStackDefinition chest = new ItemStackDefinition();
	@JsonField(Docs = "Leave this as empty to take from the team settings")
	public ItemStackDefinition legs = new ItemStackDefinition();
	@JsonField(Docs = "Leave this as empty to take from the team settings")
	public ItemStackDefinition shoes = new ItemStackDefinition();

}
