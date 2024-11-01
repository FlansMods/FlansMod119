package com.flansmod.common.types.bullets;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemCollectionDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BulletBagDefinition extends JsonDefinition
{
	@Nonnull
	public static final BulletBagDefinition INVALID = new BulletBagDefinition(new ResourceLocation(FlansMod.MODID, "bullet_bags/null"));
	public static final String TYPE = "bullet_bag";
	public static final String FOLDER = "bullet_bags";
	@Override
	public String GetTypeName() { return TYPE; }

	public BulletBagDefinition(@Nonnull ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public ItemCollectionDefinition bulletFilters = new ItemCollectionDefinition();
	@JsonField
	public int slotCount = 1;
	@JsonField
	public int maxStackSize = 64;

}
