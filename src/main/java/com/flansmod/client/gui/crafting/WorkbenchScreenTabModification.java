package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.menus.WorkbenchMenuModification;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.EFilterType;
import com.flansmod.common.types.elements.LocationFilterDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.guns.elements.MagazineSlotSettingsDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreenTabModification extends WorkbenchScreenTab<WorkbenchMenuModification>
{
	private static final ResourceLocation MOD_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int MOD_W = 256;
	private static final int MOD_H = 256;

	private static final int ATTACHMENT_SLOTS_ORIGIN_X = 49;
	private static final int ATTACHMENT_SLOTS_ORIGIN_Y = 17;

	private static final int MAG_UPGRADE_SLOT_ORIGIN_X = 7;
	private static final int MAG_UPGRADE_SLOT_ORIGIN_Y = 93;

	private static final int MAGAZINE_SELECTOR_ORIGIN_X = 29;
	private static final int MAGAZINE_SELECTOR_ORIGIN_Y = 93;
	private static final int MAGAZINES_PER_ROW = 7;
	private static final int MAGAZINE_ROWS = 1;
	private final Button[] MagazineButtons = new Button[MAGAZINES_PER_ROW * MAGAZINE_ROWS];

	private float GunAngle = 2.0f;
	private float GunAngularVelocity = 5.0f;

	public WorkbenchScreenTabModification(@Nonnull WorkbenchMenuModification menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
	}

	@Override
	protected boolean IsTabPresent() { return Workbench.Def.gunModifying.isActive; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_modification"); }
	@Override
	protected void InitTab()
	{
		if(IsTabPresent())
		{
			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					MagazineButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenuModification.BUTTON_SELECT_MAGAZINE_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(MagazineButtons[index]);
				}
			}
		}
	}
	@Override
	protected void OnTabSelected(boolean selected) { UpdateTab(selected); }
	@Override
	protected void UpdateTab(boolean selected)
	{
		GunAngularVelocity *= Maths.expF(-FlansModClient.FrameDeltaSeconds() * 0.25f);
		GunAngle += FlansModClient.FrameDeltaSeconds() * GunAngularVelocity;

		if(IsTabPresent())
		{
			int numMagButtons = 0;
			if (selected && Workbench.GunContainer.getContainerSize() > 0 && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				if(flanItem.Def() instanceof GunDefinition gunDefinition)
				{
					numMagButtons = gunDefinition.GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines().size();
				}
			}

			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					if (MagazineButtons[index] != null)
						MagazineButtons[index].active = index < numMagButtons;
				}
			}
		}
	}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll)
	{
		if(scroll != 0 && xMouse >= xOrigin + imageWidth)
		{
			GunAngularVelocity += scroll * 2.0f;
			return true;
		}
		return false;
	}

	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if(InBox(xMouse, yMouse, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, 18, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, 18))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.mag_upgrade"), xMouse, yMouse);
			return true;
		}
		if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, 24))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.gun"), xMouse, yMouse);
			return true;
		}

		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);

				for (WorkbenchMenuModification.ModSlot modSlot : WorkbenchMenuModification.ModSlot.values())
				{
					boolean hasSlot = flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex);
					if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + modSlot.x * 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + modSlot.y * 26, 24))
					{
						graphics.renderTooltip(font,
							Component.translatable("workbench.slot.attachments." + modSlot.attachType.toString().toLowerCase() + (hasSlot ? "" : ".missing")),
							xMouse, yMouse);
						return true;
					}
				}

				if (flanItem.Def() instanceof GunDefinition gunDef)
				{
					// TODO: Multiple mag settings
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					for (int j = 0; j < MAGAZINE_ROWS; j++)
					{
						for (int i = 0; i < MAGAZINES_PER_ROW; i++)
						{
							final int index = j * MAGAZINES_PER_ROW + i;
							if(index < matchingMags.size())
							{
								if(InBox(xMouse, yMouse, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18))
								{
									List<FormattedCharSequence> lines = new ArrayList<>();
									lines.add(Component.translatable("magazine." + matchingMags.get(i).Location.getNamespace() + "." + matchingMags.get(i).Location.getPath()).getVisualOrderText());
									lines.add(Component.translatable("magazine.num_rounds", matchingMags.get(i).numRounds).getVisualOrderText());
									for(LocationFilterDefinition idFilter : matchingMags.get(i).matchingBullets.itemIDFilters)
									{
										if(idFilter.filterType == EFilterType.Allow)
											for(ResourceLocation resLoc : idFilter.matchResourceLocations)
												lines.add(Component.translatable("magazine.match_bullet_name", resLoc).getVisualOrderText());
									}
									for(LocationFilterDefinition tagFilter : matchingMags.get(i).matchingBullets.itemTagFilters)
									{
										for(ResourceLocation resLoc : tagFilter.matchResourceLocations)
										{
											String line = tagFilter.filterType == EFilterType.Allow ? "magazine.required_bullet_tag" : "magazine.disallowed_bullet_tag";
											lines.add(Component.translatable(line, resLoc.toString()).getVisualOrderText());
										}
									}
									int magCost = AbstractWorkbench.GetMagUpgradeCost(Workbench.GunContainer, index);
									if(magCost == 1)
										lines.add(Component.translatable("magazine.cost.1").getVisualOrderText());
									else lines.add(Component.translatable("magazine.cost", magCost).getVisualOrderText());


									for(ModifierDefinition modifier : matchingMags.get(i).modifiers)
									{
										for(Component modString : modifier.GetModifierStrings())
											lines.add(modString.getVisualOrderText());
									}

									graphics.renderTooltip(font, lines, xMouse, yMouse);
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		// Render the gun before the background so it ends up behind
		if(Workbench.GunContainer.getContainerSize() > 0)
		{
			Render3DGun(graphics, xOrigin + imageWidth + 64, yOrigin + 64, GunAngle, -45f, Workbench.GunContainer.getItem(0));
		}

		graphics.blit(MOD_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, MOD_W, MOD_H);
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// Render the slot BG for the gun slot
			graphics.blit(MOD_BG, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, 198, 26, 22, 22, MOD_W, MOD_H);
			// Mag Slot
			graphics.blit(MOD_BG, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, 190, 201, 18, 18, MOD_W, MOD_H);
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);
				for (WorkbenchMenuModification.ModSlot modSlot : WorkbenchMenuModification.ModSlot.values())
				{


					// If this item has this slot, blit the slot BG in
					if (flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex))
					{
						int srcX = 172 + 26 * modSlot.x;
						int srcY = 26 * modSlot.y;

						if(modSlot.attachType == EAttachmentType.Charm)
						{
							srcX = 44;
							srcY = 218;
						}
						else if(modSlot.attachType == EAttachmentType.Generic)
						{
							srcX = 66;
							srcY = 218;
						}

						graphics.blit(MOD_BG,
							xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26 * modSlot.x,
							yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26 * modSlot.y,
							srcX, srcY,
							22, 22,
							MOD_W, MOD_H);
					}
				}

				// Magazine selector
				if(flanItem instanceof GunItem gunItem)
				{
					MagazineSlotSettingsDefinition magSettings = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					MagazineDefinition currentMagType = gunItem.GetMagazineType(gunStack, Actions.DefaultPrimaryActionKey, 0);
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % MAGAZINES_PER_ROW;
						int yIndex = i / MAGAZINES_PER_ROW;

						if(matchingMags.get(i) == currentMagType)
						{
							graphics.blit(MOD_BG, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, 172, 201, 18, 18, MOD_W, MOD_H);
						}
						else graphics.blit(MOD_BG, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, 172, 165, 18, 18, MOD_W, MOD_H);
					}

				}
			}
		}
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);
				if(flanItem.Def() instanceof GunDefinition gunDef)
				{
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % MAGAZINES_PER_ROW;
						int yIndex = i / MAGAZINES_PER_ROW;

						// RENDER MAG
						TextureAtlasSprite sprite = FlansModClient.MAGAZINE_ATLAS.GetIcon(matchingMags.get(i).Location);
						graphics.blit(MAGAZINE_SELECTOR_ORIGIN_X + 1 + xIndex * 18, MAGAZINE_SELECTOR_ORIGIN_Y + 1 + yIndex * 18, 0, 16, 16, sprite);
					}

				}
			}
		}
	}
}
