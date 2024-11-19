package com.flansmod.client.render.armour;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.baked.BakedTurboRig;
import com.flansmod.common.item.ArmourItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArmourItemRenderer extends FlanItemModelRenderer
{
	public ArmourItemRenderer(@Nullable ArmourItem armourItem)
	{
		super(armourItem, false);
	}

	@Override
	protected void doRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
	{
		// Find our skin
		ResourceLocation skin = getSkin(stack);

		if(hasSection(BakedTurboRig.AP_CORE))
		{
			renderSectionIteratively(
				renderContext,
				BakedTurboRig.AP_CORE,
				(partName) -> skin, // Texture-Func
				(partName, innerRenderContext) -> true, // Pre-Func
				(partName, innerRenderContext) -> {} // Post-Func
			);
		}
		else
		{

		}
	}
}
