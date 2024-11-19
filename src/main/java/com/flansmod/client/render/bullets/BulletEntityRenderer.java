package com.flansmod.client.render.bullets;

import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.projectiles.BulletEntity;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class BulletEntityRenderer extends EntityRenderer<BulletEntity>
{
	private final HashMap<ResourceLocation, ModelPart> LoadedModels = new HashMap<>();

	public BulletEntityRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull BulletEntity bullet)
	{
		return bullet.GetBulletDef().Location;
	}

	@Override
	public void render(@Nonnull BulletEntity bullet, float yaw, float partialTick, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffers, int light)
	{
		BulletDefinition def = bullet.GetBulletDef();
		ResourceLocation loc = def.Location;
		ITurboRenderer bulletRenderer = FlansModelRegistry.GetItemRenderer(loc);
		if(bulletRenderer != null)
		{
			pose.pushPose();
			pose.translate(0f, 0f, 0f);
			pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, bullet.yRotO, bullet.getYRot()) - 90.0F));
			pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, bullet.xRotO, bullet.getXRot())));
			bulletRenderer.renderDirect(
				bullet,
				ItemStack.EMPTY,
				new RenderContext(
					buffers,
					ItemDisplayContext.FIXED,
					pose,
					light,
						655360));
			pose.popPose();
		}
		else FlansMod.LOGGER.warn("Could not find bullet renderer for " + bullet);
	}
}
