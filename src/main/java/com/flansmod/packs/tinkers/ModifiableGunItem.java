package com.flansmod.packs.tinkers;

import com.flansmod.common.item.GunItem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.definition.module.mining.MiningSpeedToolHook;
import slimeknights.tconstruct.library.tools.helper.*;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerToolActions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModifiableGunItem extends GunItem implements IModifiableDisplay
{
	private final ToolDefinition toolDefinition;  /** Cached tool for rendering on UIs */
	private ItemStack toolForRendering;

	public ModifiableGunItem(@Nonnull ResourceLocation gunDef,
							 @Nonnull Properties properties,
							 @Nonnull ToolDefinition toolDef)
	{
		super(gunDef, properties);
		toolDefinition = toolDef;
	}

	@Override @Nonnull
	public ToolDefinition getToolDefinition() { return toolDefinition; }

	/* Enchanting */

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment.isCurse() && super.canApplyAtEnchantingTable(stack, enchantment);
	}
	@Override
	public int getEnchantmentValue() {
		return 0;
	}
	@Override
	public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		return EnchantmentModifierHook.getEnchantmentLevel(stack, enchantment);
	}
	@Override
	public Map<Enchantment,Integer> getAllEnchantments(ItemStack stack) {
		return EnchantmentModifierHook.getAllEnchantments(stack);
	}

	/* Loading */

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ToolCapabilityProvider(stack);
	}
	@Override
	public void verifyTagAfterLoad(CompoundTag nbt) {
		ToolStack.verifyTag(this, nbt, getToolDefinition());
	}
	@Override
	public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn) {
		ToolStack.ensureInitialized(stack, getToolDefinition());
	}

	/* Display */

	@Override
	public boolean isFoil(ItemStack stack) {
		// we use enchantments to handle some modifiers, so don't glow from them
		// however, if a modifier wants to glow let them
		return ModifierUtil.checkVolatileFlag(stack, SHINY);
	}
	@Override
	public Rarity getRarity(ItemStack stack) {
		int rarity = ModifierUtil.getVolatileInt(stack, RARITY);
		return Rarity.values()[Mth.clamp(rarity, 0, 3)];
	}

	/* Indestructible items */

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return IndestructibleItemEntity.hasCustomEntity(stack);
	}
	@Override
	public Entity createEntity(Level world, Entity original, ItemStack stack) {
		return IndestructibleItemEntity.createFrom(world, original, stack);
	}


	/* Damage/Durability */

	@Override
	public boolean isRepairable(ItemStack stack) {
		// handle in the tinker station
		return false;
	}
	@Override
	public boolean canBeDepleted() {
		return true;
	}
	@Override
	public int getMaxDamage(ItemStack stack) {
		if (!canBeDepleted()) {
			return 0;
		}
		ToolStack tool = ToolStack.from(stack);
		int durability = tool.getStats().getInt(ToolStats.DURABILITY);
		// vanilla deletes tools if max damage == getDamage, so tell vanilla our max is one higher when broken
		return tool.isBroken() ? durability + 1 : durability;
	}
	@Override
	public int getDamage(ItemStack stack) {
		if (!canBeDepleted()) {
			return 0;
		}
		return ToolStack.from(stack).getDamage();
	}
	@Override
	public void setDamage(ItemStack stack, int damage) {
		if (canBeDepleted()) {
			ToolStack.from(stack).setDamage(damage);
		}
	}
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T damager, Consumer<T> onBroken) {
		ToolDamageUtil.handleDamageItem(stack, amount, damager, onBroken);
		return 0;
	}


	/* Durability display */

	@Override
	public boolean isBarVisible(ItemStack pStack) {
		return DurabilityDisplayModifierHook.showDurabilityBar(pStack);
	}

	@Override
	public int getBarColor(ItemStack pStack) {
		return DurabilityDisplayModifierHook.getDurabilityRGB(pStack);
	}

	@Override
	public int getBarWidth(ItemStack pStack) {
		return DurabilityDisplayModifierHook.getDurabilityWidth(pStack);
	}


	/* Modifier interactions */

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		InventoryTickModifierHook.heldInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}


	/* Attacking */

	//@Override
	//public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
	//	return EntityInteractionModifierHook.leftClickEntity(stack, player, target);
	//}
	//@Override
	//public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
	//	return ModifierUtil.canPerformAction(ToolStack.from(stack), toolAction);
	//}
	//@Override
	//public Multimap<Attribute, AttributeModifier> getAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
	//	return AttributesModifierHook.getHeldAttributeModifiers(tool, slot);
	//}
	//@Override
	//public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
	//	CompoundTag nbt = stack.getTag();
	//	if (nbt == null || slot.getType() != EquipmentSlot.Type.HAND) {
	//		return ImmutableMultimap.of();
	//	}
	//	return getAttributeModifiers(ToolStack.from(stack), slot);
	//}
	//@Override
	//public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
	//	return canPerformAction(stack, TinkerToolActions.SHIELD_DISABLE);
	//}

	/* Tooltips */

	@Override
	public Component getName(ItemStack stack) {
		return TooltipUtil.getDisplayName(stack, getToolDefinition());
	}
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		TooltipUtil.addInformation(this, stack, level, tooltip, SafeClientAccess.getTooltipKey(), flag);
	}
	@Override
	public int getDefaultTooltipHideFlags(ItemStack stack) {
		return TooltipUtil.getModifierHideFlags(getToolDefinition());
	}

	/* Display items */

	@Override
	public ItemStack getRenderTool() {
		if (toolForRendering == null) {
			toolForRendering = ToolBuildHandler.buildToolForRendering(this, this.getToolDefinition());
		}
		return toolForRendering;
	}


	/* Misc */

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
		return shouldCauseReequipAnimation(oldStack, newStack, false);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return ModifiableItem.shouldCauseReequip(oldStack, newStack, slotChanged);
	}


	/* Harvest logic, mostly used by modifiers but technically would let you make a pickaxe bow */

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return IsEffectiveToolHook.isEffective(ToolStack.from(stack), state);
	}
	@Override
	public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		return ToolHarvestLogic.mineBlock(stack, worldIn, state, pos, entityLiving);
	}
	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return MiningSpeedToolHook.getDestroySpeed(stack, state);
	}
	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		return ToolHarvestLogic.handleBlockBreak(stack, pos, player);
	}
}
