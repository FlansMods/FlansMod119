package com.flansmod.common.actions.contexts;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class ShooterContext
{
	public static ResourceKey<Level> DimensionUnknown = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(FlansMod.MODID, "null"));
	public static final UUID InvalidID = new UUID(0L, 0L);
	public static final ShooterContext INVALID = new ShooterContext()
	{
		@Override
		public int GetNumValidContexts() { return 0; }
		@Override
		@Nonnull
		public UUID[] GetAllGunIDs() { return new UUID[0]; }
		@Override
		@Nonnull
		public UUID GetGunIDForSlot(int gunSlotIndex) { return FlanItem.InvalidGunUUID; }
		@Override
		@Nonnull
		public GunContext CreateContext(UUID gunID) { return GunContext.INVALID; }
		@Override
		public Entity Entity() { return null; }
		@Override
		public Entity Owner() { return null; }
		@Override
		public Transform GetShootOrigin() { return null; }
		@Override
		public boolean IsValid() { return false; }
		@Override
		public boolean IsCreative() { return false; }
		@Override
		public int hashCode() { return 0; }
		@Override
		public Container GetAttachedInventory() { return null; }
		@Override
		public int HashModifierSources() { return 0; }
		@Override
		public void RecalculateModifierCache(BiConsumer<ModifierDefinition, StatCalculationContext> consumer) {}
	};

	@Nonnull
	public static ShooterContext of(@Nullable Entity shooter)
	{
		if(shooter != null)
		{
			if (shooter.level().isClientSide)
				return client(shooter);
			return server(shooter);
		}
		return INVALID;
	}
	@Nonnull
	public static ShooterContext of(@Nullable Entity shooter, @Nullable Entity owner)
	{
		if(shooter != null)
		{
			if (shooter.level().isClientSide)
				return client(shooter, owner);
			return server(shooter, owner);
		}
		return INVALID;
	}
	@Nonnull
	public static ShooterContext of(@Nonnull UUID shooterID, @Nonnull UUID ownerID, boolean isClient)
	{
		return isClient ? client(shooterID, ownerID) : server(shooterID, ownerID);
	}
	@Nonnull
	public static ShooterContext server(@Nonnull Entity shooter) { return FlansMod.CONTEXT_CACHE.GetShooter(shooter); }
	@Nonnull
	public static ShooterContext server(@Nonnull Entity shooter, @Nullable Entity owner) { return FlansMod.CONTEXT_CACHE.GetShooter(shooter, owner); }
	@Nonnull
	public static ShooterContext server(@Nonnull UUID shooterID, @Nonnull UUID ownerID) { return FlansMod.CONTEXT_CACHE.GetShooter(shooterID, ownerID, null); }

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull Entity shooter) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooter); }
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull Entity shooter, @Nullable Entity owner) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooter, owner); }
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ShooterContext client(@Nonnull UUID shooterID, @Nonnull UUID ownerID) { return FlansModClient.CONTEXT_CACHE.GetShooter(shooterID, ownerID, null); }


	// ---------------------------------------------------------------------------------------------------
	private boolean IsCurrent;

	public ShooterContext()
	{
		ModCache = new ModifierCacheCollection(
			this::HashModifierSources,
			(cache) -> {
				RecalculateModifierCache(cache::ApplyModifier);
			});
		IsCurrent = true;
	}
	public boolean IsPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player;
	}
	public boolean IsLocalPlayerOwner()
	{
		return Owner() != null && Owner() instanceof Player player && player.isLocalPlayer();
	}
	@Nonnull
	public EContextSide GetSide() { return EContextSide.of(Entity()); }
	@Nonnull
	public ResourceKey<Level> Dimension()
	{
		Level level = Level();
		return level != null ? level.dimension() : DimensionUnknown;
	}
	@Nullable
	public Level Level()
	{
		return Entity() != null ? Entity().level() : null;
	}
	@Nonnull
	public UUID EntityUUID() { return Entity() != null ? Entity().getUUID() : InvalidID; }
	@Nonnull
	public UUID OwnerUUID() { return Owner() != null ? Owner().getUUID() : InvalidID; }
	public void MarkAsOld() { IsCurrent = false; }

	// ---------------------------------------------------------------------------------------------------
	// MODIFIER CACHE
	// ---------------------------------------------------------------------------------------------------
	private final ModifierCacheCollection ModCache;

	@Nonnull
	public FloatModifier GetFloatModifier(@Nonnull String stat) { return ModCache.GetFloatModifier(stat); }
	@Nonnull
	public FloatModifier GetFloatModifier(@Nonnull String stat, @Nonnull String actionGroupPath) { return ModCache.GetFloatModifier(stat, actionGroupPath); }
	@Nullable
	public String GetModifiedString(@Nonnull String stat) { return ModCache.GetModifiedString(stat); }
	@Nullable
	public String GetModifiedString(@Nonnull String stat, @Nonnull String actionGroupPath) { return ModCache.GetModifiedString(stat, actionGroupPath); }



	public float GetFloat(@Nonnull String stat) { return GetFloatModifier(stat).GetValue(); }
	public float ModifyFloat(@Nonnull String stat, float baseValue) { return GetFloatModifier(stat).ModifyValue(baseValue); }
	@Nonnull
	public String GetString(@Nonnull String stat) { return Optional.ofNullable(GetModifiedString(stat)).orElse(""); }
	@Nonnull
	public String ModifyString(@Nonnull String stat, @Nonnull String defaultValue) { return Optional.ofNullable(GetModifiedString(stat)).orElse(defaultValue); }


	// ---------------------------------------------------------------------------------------------------


	public void Save(@Nonnull CompoundTag tags)
	{
		tags.putUUID("owner", OwnerUUID());
		tags.putUUID("entity", EntityUUID());
	}

	@Nonnull
	public static ShooterContext Load(@Nonnull CompoundTag tags, boolean client)
	{
		UUID ownerID = tags.getUUID("owner");
		UUID entityID = tags.getUUID("entity");
		return ShooterContext.of(ownerID, entityID, client);
	}

	@Nonnull
	public GunContext GetGunContextForSlot(int gunSlotIndex, boolean client)
	{
		UUID gunID = GetGunIDForSlot(gunSlotIndex);
		if(gunID != FlanItem.InvalidGunUUID)
			return GunContext.of(this, gunID);
		return GunContext.INVALID;
	}

	@Nonnull
	public GunContext[] GetAllGunContexts(boolean client)
	{
		UUID[] ids = GetAllGunIDs();
		GunContext[] contexts = new GunContext[ids.length];
		for(int i = 0; i < ids.length; i++)
		{
			contexts[i] = GunContext.of(this, ids[i]);
			contexts[i].UpdateFromItemStack();
		}
		return contexts;
	}

	// ---------------------------------------------------------------------------------------------------
	// INTERFACE
	// ---------------------------------------------------------------------------------------------------
	public abstract int GetNumValidContexts();
	@Nonnull
	public abstract UUID[] GetAllGunIDs();
	@Nonnull
	public abstract UUID GetGunIDForSlot(int gunSlotIndex);
	@Nonnull
	public abstract GunContext CreateContext(UUID gunID);
	public abstract Entity Entity();
	public abstract Entity Owner();
	public abstract Container GetAttachedInventory();
	public abstract Transform GetShootOrigin();
	public abstract boolean IsValid();
	public abstract boolean IsCreative();
	public abstract int HashModifierSources();
	public abstract void RecalculateModifierCache(@Nonnull BiConsumer<ModifierDefinition, StatCalculationContext> consumer);

	@Override
	public String toString()
	{
		return "ShooterContext (Shooter:" + EntityUUID() + ", Owner:" + OwnerUUID() + ")";
	}
}
