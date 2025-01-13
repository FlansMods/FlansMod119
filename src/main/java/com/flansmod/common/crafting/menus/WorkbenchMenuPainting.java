package com.flansmod.common.crafting.menus;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.slots.RestrictedSlot;
import com.flansmod.common.item.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class WorkbenchMenuPainting extends WorkbenchMenu
{
	public final Container GunContainer;
	public final Container PaintCanContainer;

	private RestrictedSlot GunSlot;
	private RestrictedSlot PaintCanSlot;

	public static final int BUTTON_SELECT_SKIN_0 					= 0x00; // 128 possible basic skins
	public static final int BUTTON_SELECT_SKIN_MAX 					= 0x7f;
	public static final int BUTTON_SELECT_PREMIUM_SKIN_0 			= 0x80; // 128 possible premium skins
	public static final int BUTTON_SELECT_PREMIUM_SKIN_MAX 			= 0xff;


	public WorkbenchMenuPainting(int containerID,
								 @Nonnull Inventory inventory,
								 @Nonnull AbstractWorkbench workbench)
	{
		super(FlansMod.WORKBENCH_MENU_PAINTING.get(), containerID, inventory, workbench);
		GunContainer = Workbench.GunContainer;
		PaintCanContainer = Workbench.PaintCanContainer;
		CreateSlots(inventory, 0);
	}

	public WorkbenchMenuPainting(int containerID,
								 @Nonnull Inventory inventory,
								 @Nonnull FriendlyByteBuf data)
	{
		super(FlansMod.WORKBENCH_MENU_PAINTING.get(), containerID, inventory, data);

		GunContainer = Workbench.GunContainer;
		PaintCanContainer = Workbench.PaintCanContainer;
		CreateSlots(inventory, 0);
	}

	@Override
	public boolean clickMenuButton(@Nonnull Player player, int buttonID)
	{
		// Signed byte please
		if (buttonID < 0)
			buttonID += 256;
		if (BUTTON_SELECT_SKIN_0 <= buttonID && buttonID <= BUTTON_SELECT_SKIN_MAX)
		{
			int skinIndex = buttonID - BUTTON_SELECT_SKIN_0;
			if (!player.level().isClientSide)
				AbstractWorkbench.PaintGun(player, GunContainer, PaintCanContainer, skinIndex);
			return true;
		}

		return false;
	}

	@Override
	protected void CreateSlots(@Nonnull Inventory playerInventory, int inventorySlotOffsetX)
	{
		super.CreateSlots(playerInventory, inventorySlotOffsetX + 4);
		if (GunContainer.getContainerSize() > 0)
		{
			addSlot(GunSlot = new RestrictedSlot(GunContainer, 0, 13, 18));
			addSlot(PaintCanSlot = new RestrictedSlot(PaintCanContainer, 0, 150, 18));
		}
	}

	@Override @Nonnull
	public ItemStack quickMoveStack(@Nonnull Player player, int slot)
	{
		if(GunSlot != null && slot == GunSlot.index)
			return QuickStackIntoInventory(player, GunSlot);
		else if(PaintCanSlot != null && slot == PaintCanSlot.index)
			return QuickStackIntoInventory(player, PaintCanSlot);

		else
		{
			// We are shifting from the player into the inventory
			ItemStack stack = slots.get(slot).getItem();
			if (GunSlot != null && GunSlot.getItem().isEmpty() && stack.getItem() instanceof GunItem)
			{
				GunSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			if (PaintCanSlot != null && PaintCanSlot.getItem().isEmpty() && stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get())
			{
				PaintCanSlot.set(stack);
				slots.get(slot).set(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
		}

		return ItemStack.EMPTY;
	}
}
