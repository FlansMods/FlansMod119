package com.flansmod.packs.tinkers.client;

import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.packs.tinkers.FMTinkerResources;
import com.flansmod.packs.tinkers.ModifiableGunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class TinkerGunItemRenderer extends GunItemRenderer
{
	protected final ModifiableGunItem item;
	public TinkerGunItemRenderer(@Nonnull ModifiableGunItem gunItem)
	{
		super(gunItem);
		item = gunItem;
	}

	@Override @Nonnull
	protected Function<String, ResourceLocation> getSkinFunc(@Nonnull ItemStack stack)
	{
		ResourceLocation skin = getSkin(stack);
		ResourceLocation barrelSkin = skin;
		ResourceLocation upperSkin = skin;
		ResourceLocation lowerSkin = skin;
		ResourceLocation stockSkin = skin;
		ResourceLocation gripSkin = skin;

		List<IToolPart> parts = ToolPartsHook.parts(item.getToolDefinition());
		for(int i = 0; i < parts.size(); i++)
		{
			MaterialVariantId variantId = ToolStack.from(stack).getMaterials().get(i).getVariant();

			IToolPart part = parts.get(i);
			ResourceLocation skinOverride;
			String[] pathExt = skin.getPath().split("\\.");
			if(pathExt.length == 2)
			{
				skinOverride = new ResourceLocation(skin.getNamespace(), pathExt[0]+"_"+variantId.getSuffix()+"."+pathExt[1]);
			}
			else
			{
				skinOverride = new ResourceLocation(skin.getNamespace(), skin.getPath()+"_"+variantId.getSuffix());
			}

			if(FMTinkerResources.BARREL.get().equals(part))
				barrelSkin = skinOverride;
			else if(FMTinkerResources.UPPER_RECEIVER.get().equals(part))
				upperSkin = skinOverride;
			else if(FMTinkerResources.LOWER_RECEIVER.get().equals(part))
				lowerSkin = skinOverride;
			else if(FMTinkerResources.STOCK.get().equals(part))
				stockSkin = skinOverride;
			else if(FMTinkerResources.GRIP.get().equals(part))
				gripSkin = skinOverride;
		}

		final ResourceLocation barrelSkinFinal = barrelSkin;
		final ResourceLocation upperSkinFinal = upperSkin;
		final ResourceLocation lowerSkinFinal = lowerSkin;
		final ResourceLocation stockSkinFinal = stockSkin;
		final ResourceLocation gripSkinFinal = gripSkin;
		return (partName) -> switch(partName)
		{
			case "body", "slide" -> upperSkinFinal;
			case "stock" -> stockSkinFinal;
			case "grip" -> gripSkinFinal;
			case "barrel" -> barrelSkinFinal;
			case "ammo_0" -> lowerSkinFinal;
			default -> skin;
		};
	}

}
