package com.flansmod.packs.tinkers.client;

import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.item.GunItem;
import com.flansmod.packs.tinkers.ModifiableGunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TinkerGunItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final GunItem Item;
	@Nonnull
	public TinkerGunItemRenderer ItemRenderer;

	protected TinkerGunItemClientExtension(@Nonnull ModifiableGunItem item)
	{
		Item = item;
		ItemRenderer = new TinkerGunItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public TinkerGunItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static TinkerGunItemClientExtension of(@Nonnull ModifiableGunItem item)
	{
		TinkerGunItemClientExtension clientExt = new TinkerGunItemClientExtension(item);
		FlansModelRegistry.PreRegisterModel(clientExt::GetLocation);
		return clientExt;
	}

	@Override
	@Nullable
	public HumanoidModel.ArmPose getArmPose(@Nonnull LivingEntity entityLiving,
											@Nonnull InteractionHand hand,
											@Nonnull ItemStack itemStack)
	{
		if(itemStack.getItem() instanceof ModifiableGunItem)
			return HumanoidModel.ArmPose.BOW_AND_ARROW;
		return HumanoidModel.ArmPose.ITEM;
	}

}