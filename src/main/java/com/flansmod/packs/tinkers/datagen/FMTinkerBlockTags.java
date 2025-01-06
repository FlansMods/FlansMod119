package com.flansmod.packs.tinkers.datagen;

import com.flansmod.packs.tinkers.FlansTinkersMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class FMTinkerBlockTags extends BlockTagsProvider
{
	public FMTinkerBlockTags(@Nonnull PackOutput output,
							 @Nonnull CompletableFuture<HolderLookup.Provider> lookupProvider,
							 @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, FlansTinkersMod.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(@Nonnull HolderLookup.Provider provider)
	{

	}
}
