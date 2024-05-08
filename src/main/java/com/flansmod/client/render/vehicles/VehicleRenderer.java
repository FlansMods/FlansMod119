package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VehicleRenderer extends EntityRenderer<VehicleEntity> implements ITurboRenderer
{
	@Nonnull
	private final LazyDefinition<VehicleDefinition> Def;
	@Nullable
	private TurboRenderUtility TurboRenderHelper;
	@Nonnull
	public TurboRenderUtility GetTurboRigWrapper()
	{
		if(TurboRenderHelper == null)
		{
			TurboRenderHelper = FlansModelRegistry.GetRigWrapperFor(Def.Loc());
		}
		return TurboRenderHelper;
	}

	public VehicleRenderer(@Nonnull ResourceLocation defLoc,
						   @Nonnull EntityRendererProvider.Context context)
	{
		super(context);
		Def = LazyDefinition.of(defLoc, FlansMod.VEHICLES);
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull VehicleEntity vehicle)
	{
		return TextureManager.INTENTIONAL_MISSING_TEXTURE;
	}

	public void RenderDirect(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
	{
		ResourceLocation skin = heldByEntity instanceof VehicleEntity vehicle ? getTextureLocation(vehicle) : TextureManager.INTENTIONAL_MISSING_TEXTURE;
		GetTurboRigWrapper().RenderPartIteratively(renderContext,
			"body",
			(partName) -> skin,
			(partName, preRenderContext) -> {
				return true;
			},
			(partName, postRenderContext) -> {

			});
	}


	// ItemRenderer
	public void render(@Nonnull VehicleEntity vehicle,
					   float yaw,
					   float dt,
					   @Nonnull PoseStack poseStack,
					   @Nonnull MultiBufferSource buffers,
					   int light)
	{
		RenderContext renderContext = new RenderContext(buffers, ItemDisplayContext.FIXED, poseStack, light, 0);
		RenderDirect(vehicle, ItemStack.EMPTY, renderContext);
	}
}
