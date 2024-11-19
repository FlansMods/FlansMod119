package com.flansmod.client.render.effects;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectRenderer extends FlanItemModelRenderer
{
	public EffectRenderer()
	{
		super(null, true);
	}

	@Override
	protected void doRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
	{
		ResourceLocation skin = getSkin(stack);
		renderContext.Transforms.push();
		renderSectionIteratively(renderContext,
			"body",
			(partName) -> skin, 						// Texture func
			(partName, innerRenderContext) -> true, 	// Pre-func
			(partName, innerRenderContext) -> { } 		// Post-func
		);
		renderContext.Transforms.pop();
	}
}
