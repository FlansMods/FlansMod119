package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.menus.WorkbenchMenuPainting;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreenTabPainting extends WorkbenchScreenTab<WorkbenchMenuPainting>
{
	private static final ResourceLocation MOD_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/painting_table.png");
	private static final int MOD_W = 256;
	private static final int MOD_H = 256;

	private static final int PAINT_BUCKET_SLOT_ORIGIN_X = 150;
	private static final int PAINT_BUCKET_SLOT_ORIGIN_Y = 18;

	private static final int GUN_SLOT_ORIGIN_X = 13;
	private static final int GUN_SLOT_ORIGIN_Y = 18;


	private static final int SKIN_SELECTOR_ORIGIN_X = 18;
	private static final int SKIN_SELECTOR_ORIGIN_Y = 40;
	private static final int SKINS_PER_ROW = 8;
	private static final int SKIN_ROWS = 2;
	private final Button[] SkinButtons = new Button[SKINS_PER_ROW * SKIN_ROWS];

	private static final int PREMIUM_SKIN_SELECTOR_ORIGIN_X = 35;
	private static final int PREMIUM_SKIN_SELECTOR_ORIGIN_Y = 91;
	private static final int PREMIUM_SKINS_PER_ROW = 4;
	private static final int PREMIUM_SKIN_ROWS = 1;
	private final Button[] PremiumSkinButtons = new Button[SKINS_PER_ROW * SKIN_ROWS];
	private static final int PREMIUM_DETAILS_BUTTON_X = 6;
	private static final int PREMIUM_DETAILS_BUTTON_Y = 88;
	private Button PremiumDetailsButton;

	private float GunAngle = 2.0f;
	private float GunAngularVelocity = 5.0f;

	public WorkbenchScreenTabPainting(@Nonnull WorkbenchMenuPainting menu,
									  @Nonnull Inventory inventory,
									  @Nonnull Component title)
	{
		super(menu, inventory, title);

		imageWidth = 180;
		titleLabelX += 4;
		inventoryLabelX += 4;
	}

	@Override
	protected boolean IsTabPresent() { return Workbench.Def.painting.isActive; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_painting"); }
	@Override
	protected void InitTab()
	{
		if(IsTabPresent())
		{
			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					SkinButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenuPainting.BUTTON_SELECT_SKIN_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * i, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(SkinButtons[index]);
				}
			}
			for (int i = 0; i < PREMIUM_SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < PREMIUM_SKIN_ROWS; j++)
				{
					final int index = i + j * PREMIUM_SKINS_PER_ROW;
					PremiumSkinButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenuPainting.BUTTON_SELECT_PREMIUM_SKIN_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + PREMIUM_SKIN_SELECTOR_ORIGIN_X + 18 * i, yOrigin + PREMIUM_SKIN_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(PremiumSkinButtons[index]);
				}
			}
			PremiumDetailsButton = Button.builder(
				Component.empty(),
				(t) ->
				{
					try
					{
						Util.getPlatform().openUrl(new URL("https://www.patreon.com/c/FlansGames"));
					}
					catch(Exception e)
					{
						FlansMod.LOGGER.error("Failed to open Patreon page");
					}
				})
				.bounds(xOrigin + PREMIUM_DETAILS_BUTTON_X, yOrigin + PREMIUM_DETAILS_BUTTON_Y, 24, 24)
				.build();
			addRenderableWidget(PremiumDetailsButton);

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
			int numSkinButtons = 0;
			int numPremiumSkinButtons = 0;

			if (selected && Workbench.GunContainer.getContainerSize() > 0
				&& Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				PaintableDefinition paintableDef = flanItem.GetPaintDef();
				if (paintableDef.IsValid())
				{
					numSkinButtons = paintableDef.getBasicPaintjobs().size() + 1;
					numPremiumSkinButtons = paintableDef.getPremiumPaintjobs().size();
				}
			}

			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					if (SkinButtons[index] != null)
						SkinButtons[index].active = index < numSkinButtons;
				}
			}
			for (int i = 0; i < PREMIUM_SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < PREMIUM_SKIN_ROWS; j++)
				{
					final int index = i + j * PREMIUM_SKINS_PER_ROW;
					if (PremiumSkinButtons[index] != null)
						PremiumSkinButtons[index].active = index < numPremiumSkinButtons;
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
		if(InBox(xMouse, yMouse, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, 18, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, 18))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.paint_can"), xMouse, yMouse);
			return true;
		}
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 18))
					{
						List<FormattedCharSequence> lines = new ArrayList<>();
						lines.add(Component.translatable("paintjob.default").getVisualOrderText());
						lines.add(Component.translatable("paintjob.free_to_swap").getVisualOrderText());
						graphics.renderTooltip(font, lines, xMouse, yMouse);
						return true;
					}
					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 18))
						{
							List<FormattedCharSequence> lines = new ArrayList<>();
							lines.add(Component.translatable("paintjob." + flanItem.DefinitionLocation.getNamespace() + "." + paintableDefinition.paintjobs[p].textureName).getVisualOrderText());
							int paintCost = AbstractWorkbench.GetPaintUpgradeCost(Workbench.GunContainer, p + 1);
							if(paintCost == 1)
								lines.add(Component.translatable("paintjob.cost.1").getVisualOrderText());
							else lines.add(Component.translatable("paintjob.cost", paintCost).getVisualOrderText());

							graphics.renderTooltip(font, lines, xMouse, yMouse);
							return true;
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
			//graphics.blit(MOD_BG, xOrigin + GUN_SLOT_ORIGIN_X, yOrigin + GUN_SLOT_ORIGIN_Y, 198, 26, 22, 22, MOD_W, MOD_H);
			// Paint Can Slot
			//graphics.blit(MOD_BG, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, 208, 201, 18, 18, MOD_W, MOD_H);

			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);
				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					if(FlanItem.GetPaintjobName(gunStack).equals("default"))
					{
						graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 180, 201, 18, 18, MOD_W, MOD_H);
					}
					else graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 180, 165, 18, 18, MOD_W, MOD_H);

					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(FlanItem.GetPaintjobName(gunStack).equals(paintableDefinition.paintjobs[p].textureName))
						{
							graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 180, 201, 18, 18, MOD_W, MOD_H);
						}
						else graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 180, 165, 18, 18, MOD_W, MOD_H);
					}
				}
			}
		}
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);
				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				List<PaintjobDefinition> basic = paintableDefinition.getBasicPaintjobs();
				List<PaintjobDefinition> premium = paintableDefinition.getPremiumPaintjobs();
				if (basic.size() > 0)
				{
					// Render default skin
					{
						ItemStack paintedStack = gunStack.copy();
						FlanItem.SetPaintjobName(paintedStack, "default");
						RenderGUIItem(graphics, SKIN_SELECTOR_ORIGIN_X + 1,  SKIN_SELECTOR_ORIGIN_Y + 1, paintedStack, false);
					}

					// And other skins
					for (int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						ItemStack paintedStack = gunStack.copy();
						FlanItem.SetPaintjobName(paintedStack, paintableDefinition.paintjobs[p].textureName);
						RenderGUIItem(graphics,SKIN_SELECTOR_ORIGIN_X + 1 + 18 * xIndex, SKIN_SELECTOR_ORIGIN_Y + 1 + 18 * yIndex, paintedStack, false);
					}
				}
				if(premium.size() > 0)
				{

				}
				else
				{
					graphics.drawString(font, Component.translatable("workbench.paint.premium.header"),
						PREMIUM_SKIN_SELECTOR_ORIGIN_X, PREMIUM_SKIN_SELECTOR_ORIGIN_Y, 0x000000, false);
					graphics.drawString(font, Component.translatable("workbench.paint.premium.none_available"),
						PREMIUM_SKIN_SELECTOR_ORIGIN_X, PREMIUM_SKIN_SELECTOR_ORIGIN_Y + 10, 0x808080, false);
				}

				if(Workbench.PaintCanContainer.isEmpty())
				{
					Component insertHint = Component.translatable("workbench.paint.hint.insert_paintcan");
					graphics.drawString(font, insertHint,
						PAINT_BUCKET_SLOT_ORIGIN_X - 4 - font.width(insertHint), PAINT_BUCKET_SLOT_ORIGIN_Y + 5, 0x808080, false);
				}
			}
			else
			{
				graphics.drawString(font, Component.translatable("workbench.paint.premium.header"),
					PREMIUM_SKIN_SELECTOR_ORIGIN_X, PREMIUM_SKIN_SELECTOR_ORIGIN_Y, 0x000000, false);
				graphics.drawString(font, Component.translatable("workbench.paint.premium.no_item"),
					PREMIUM_SKIN_SELECTOR_ORIGIN_X, PREMIUM_SKIN_SELECTOR_ORIGIN_Y + 10, 0x808080, false);

				Component insertHint = Component.translatable("workbench.paint.hint.insert_gun");
				graphics.drawString(font, insertHint,
					GUN_SLOT_ORIGIN_X + 24, GUN_SLOT_ORIGIN_Y + 5, 0x808080, false);
			}
		}

		graphics.blit(MOD_BG, PREMIUM_DETAILS_BUTTON_X+3, PREMIUM_DETAILS_BUTTON_Y+3, 188, 4, 16, 16);
	}

}
