package com.flansmod.teams.client.gui;

import com.flansmod.teams.api.ERoundPhase;
import com.flansmod.teams.api.admin.IPlayerLoadout;
import com.flansmod.teams.client.TeamsModClient;
import com.flansmod.teams.common.TeamsMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class ChooseLoadoutScreen extends AbstractTeamsScreen
{
	private static final ResourceLocation texture = new ResourceLocation(TeamsMod.MODID, "textures/gui/teams.png");
	private static int getGuiHeight() { return 29 + 38 * getNumLines(); }
	private static int getNumLines() { return TeamsModClient.MANAGER.getNumLoadoutOptions(); }

	private Button[] selectButtons;

	@Override
	public boolean canBeOpenInPhase(@Nonnull ERoundPhase phase) { return phase == ERoundPhase.Gameplay; }

	public ChooseLoadoutScreen(@Nonnull Component title)
	{
		super(title, 256, getGuiHeight());
	}

	@Override
	protected void init()
	{
		super.init();

		xOrigin = width / 2 - 256/2;
		yOrigin = height / 2 - getGuiHeight()/2;

		selectButtons = new Button[getNumLines()];
		for(int i = 0; i < getNumLines(); i++)
		{
			final int index = i;
			selectButtons[i] = Button.builder(
				Component.translatable("teams.select_loadout.button"),
				(t) -> {
					TeamsModClient.MANAGER.sendLoadoutChoice(index);
				})
				.bounds(xOrigin + 9, yOrigin + 38 + 24 * i, 73, 20)
				.build();
			addRenderableWidget(selectButtons[i]);
		}

		Button button = Button.builder(
			Component.translatable("teams.select_loadout.back"),
			(t) -> {
				TeamsModClient.MANAGER.openTeamSelectGUI();
			})
			.bounds(xOrigin + 256 - 57, yOrigin + 5, 50, 15)
			.build();
		addRenderableWidget(button);
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int x, int y, float f)
	{

		xOrigin = width / 2 - 256/2;
		yOrigin = height / 2 - getGuiHeight()/2;

		graphics.drawString(font, Component.translatable("teams.select_loadout.title"), xOrigin + 8, yOrigin + 8, 0x404040, true);
		graphics.drawString(font, Component.translatable("teams.select_loadout.title"), xOrigin + 7, yOrigin + 7, 0xffffff, true);

		graphics.blit(texture, xOrigin, yOrigin, 0, 0, imageWidth, 22);
		graphics.blit(texture, xOrigin, yOrigin + getGuiHeight() - 7, 0, 73, imageWidth, 7);

		int count = TeamsModClient.MANAGER.getNumLoadoutOptions();
		for(int i = 0; i < count; i++)
		{
			graphics.blit(texture, xOrigin, yOrigin + 22 + 38 * i, 0, 48, imageWidth, 16);
			graphics.blit(texture, xOrigin, yOrigin + 38 + 38 * i, 0, 25, imageWidth, 22);
			IPlayerLoadout loadout = TeamsModClient.MANAGER.loadoutOptions.get(i);
			graphics.drawString(font, loadout.getName(), xOrigin+10, yOrigin+26+24 * i, 0xffffff, true);

			for(int slot = 0; slot < 9; slot++)
			{
				ItemStack stack = loadout.getStackInSlot(slot);
				if(!stack.isEmpty())
				{
					graphics.renderItem(stack, xOrigin + 85 + 18 * slot, yOrigin + 40 + 24 * i);
				}
			}
		}

		super.render(graphics, x, y, f);
	}

}
