package com.flansmod.teams.common.info;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.teams.LoadoutPoolDefinition;
import com.flansmod.common.types.teams.elements.LoadoutAttachmentModifierDefinition;
import com.flansmod.common.types.teams.elements.LoadoutItemModifierDefinition;
import com.flansmod.common.types.teams.elements.LoadoutOptionDefinition;
import com.flansmod.common.types.teams.elements.LoadoutSkinModifierDefinition;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomLoadout implements IPlayerLoadout
{
	public String name;
	public ResourceLocation loadoutPoolDef;
	public Map<Integer, Integer> hashedLoadoutChoices = new HashMap<>();
	public Map<String, Integer> loadoutChoices = new HashMap<>();

	public CustomLoadout(@Nonnull ResourceLocation def)
	{
		loadoutPoolDef = def;
	}

	@Override @Nonnull
	public Component getName() { return Component.literal(name); }
	@Nonnull
	public LoadoutPoolDefinition getDef()
	{
		return FlansMod.LOADOUT_POOLS.Get(loadoutPoolDef);
	}
	public void forEachChoice(@Nonnull Consumer<LoadoutOptionDefinition> func)
	{
		LoadoutPoolDefinition pool = getDef();
		for(var kvp : pool.getSortedChoices().entrySet())
		{
			int mySelection = loadoutChoices.getOrDefault(kvp.getKey(), -1);
			if(mySelection != -1)
				func.accept(kvp.getValue().options[mySelection]);
		}
	}
	@Nullable
	public LoadoutOptionDefinition firstMatchingChoice(@Nonnull Function<LoadoutOptionDefinition, Boolean> matchFunc)
	{
		LoadoutPoolDefinition pool = getDef();
		for(var kvp : pool.getSortedChoices().entrySet())
		{
			int mySelection = loadoutChoices.getOrDefault(kvp.getKey(), -1);
			if(mySelection != -1)
				if(matchFunc.apply(kvp.getValue().options[mySelection]))
					return kvp.getValue().options[mySelection];
		}
		return null;
	}
	@Nullable
	public <T> T tryGetMatch(@Nonnull Function<LoadoutOptionDefinition, T> getFunc)
	{
		LoadoutPoolDefinition pool = getDef();
		for(var kvp : pool.getSortedChoices().entrySet())
		{
			int mySelection = loadoutChoices.getOrDefault(kvp.getKey(), -1);
			if(mySelection != -1)
			{
				T get = getFunc.apply(kvp.getValue().options[mySelection]);
				if(get != null)
					return get;
			}
		}
		return null;
	}
	@Nonnull
	public <T> T tryGetMatch(@Nonnull Function<LoadoutOptionDefinition, T> getFunc, @Nonnull T defaultValue)
	{
		T value = tryGetMatch(getFunc);
		return value != null ? value : defaultValue;
	}

	@Override @Nullable
	public ResourceLocation getSkinOverride()
	{
		LoadoutSkinModifierDefinition skinSelection = tryGetMatch(
			(option) -> {
				for(LoadoutSkinModifierDefinition skinMod : option.changeSkins)
					if(skinMod.applyToPlayer)
						return skinMod;
				return null;
			}
		);
		if(skinSelection != null)
			return skinSelection.skinID;
		return JsonDefinition.InvalidLocation;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slotIndex)
	{
		ItemStack slotStack = tryGetMatch((option) -> {
			for(LoadoutItemModifierDefinition addItem : option.addItems)
				if(addItem.inventorySlot == slotIndex)
					return addItem.item.CreateStack();
			return null;
		}, ItemStack.EMPTY);

		forEachChoice((option) -> {
			for(LoadoutAttachmentModifierDefinition attachment : option.attachItems)
				if(attachment.inventorySlot == slotIndex)
				{
					ItemStack attachmentStack = attachment.attachmentItem.CreateStack();
					tryAttach(attachmentStack, slotStack);
				}
			for(LoadoutSkinModifierDefinition skin : option.changeSkins)
				if(!skin.applyToPlayer && skin.applyToSlot == slotIndex)
				{
					trySkin(skin.skinID, slotStack);
				}
		});
		return slotStack;
	}
	private void tryAttach(@Nonnull ItemStack attachment, @Nonnull ItemStack onto)
	{
		if(onto.getItem() instanceof FlanItem flanItem)
		{
			FlanItem.TryAttach(attachment, onto);
		}
	}
	private void trySkin(@Nonnull ResourceLocation skin, @Nonnull ItemStack onto)
	{
		if(onto.getItem() instanceof FlanItem flanItem)
		{
			PaintableDefinition paintDef = flanItem.GetPaintDef();
			if(paintDef.IsValid())
			{
				for(PaintjobDefinition paint : paintDef.paintjobs)
				{
					if(paint.textureName.equals(skin.toString()))
						FlanItem.SetPaintjobName(onto, paint.textureName);
				}
			}
		}
	}

	@Override @Nonnull
	public ItemStack getEquipmentStack(@Nonnull EquipmentSlot equipSlot)
	{
		return switch(equipSlot)
		{
			case HEAD, CHEST, LEGS, FEET -> getStackInSlot(Inventory.INVENTORY_SIZE + Inventory.ALL_ARMOR_SLOTS[equipSlot.getIndex()]);
			case MAINHAND -> getStackInSlot(0);
			case OFFHAND -> getStackInSlot(Inventory.SLOT_OFFHAND);
		};
	}
}
