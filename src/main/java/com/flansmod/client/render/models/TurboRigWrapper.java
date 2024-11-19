package com.flansmod.client.render.models;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.models.baked.BakedTurboRig;
import com.flansmod.common.item.FlanItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TurboRigWrapper
{
	@Nonnull
	public final Supplier<ResourceLocation> modelLocationGetter;
	@Nullable
	public BakedModel bakedRef;
	@Nullable
	public BakedTurboRig bakedTurboRef;

	public TurboRigWrapper(@Nonnull Supplier<ResourceLocation> modelLoc)
	{
		modelLocationGetter = modelLoc;
		bakedRef = null;
		bakedTurboRef = null;
	}

	private void checkRef()
	{
		if(bakedRef == null)
		{
			BakedTurboRig turbo = FlansModClient.MODEL_REGISTRATION.getBakedRig(modelLocationGetter.get());
			if(turbo != null)
			{
				bakedRef = bakedTurboRef = turbo;
			}
			else
			{
				BakedModel regular = Minecraft.getInstance().getModelManager().getModel(modelLocationGetter.get());
				BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
				if(regular != missing)
					bakedRef = regular;
			}
		}
	}

	@Nullable
	public <T> T getOrDefault(@Nonnull Function<BakedTurboRig, T> getFunc, @Nullable T defaultValue)
	{
		checkRef();
		if(bakedTurboRef != null)
			return getFunc.apply(bakedTurboRef);
		return defaultValue;
	}

	public void ifRigFound(@Nonnull Consumer<BakedTurboRig> func)
	{
		checkRef();
		if(bakedTurboRef != null)
			func.accept(bakedTurboRef);
	}

	public void ifAnyModelFound(@Nonnull Consumer<BakedModel> func)
	{
		checkRef();
		if(bakedRef != null)
			func.accept(bakedRef);
	}

	@Nullable
	public <T> T getRigOrOtherwise(@Nonnull Function<BakedTurboRig, T> rigGetFunc, @Nonnull Function<BakedModel, T> backupGetFunc, @Nullable T defaultValue)
	{
		checkRef();
		if(bakedTurboRef != null)
			return rigGetFunc.apply(bakedTurboRef);
		else if(bakedRef != null)
			return backupGetFunc.apply(bakedRef);
		else
			return defaultValue;
	}

	public void ifRigOrOtherwise(@Nonnull Consumer<BakedTurboRig> rigFunc, @Nonnull Consumer<BakedModel> backupFunc)
	{
		checkRef();
		if(bakedTurboRef != null)
			rigFunc.accept(bakedTurboRef);
		else if(bakedRef != null)
			backupFunc.accept(bakedRef);
	}



}
