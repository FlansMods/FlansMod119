package com.flansmod.physics.common.collision.obb;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record FullSeparationResult(@Nonnull ImmutableList<SeparationResult> options)
{
	@Nonnull
	public static FullSeparationResult of(@Nonnull ImmutableList<SeparationResult> results) { return new FullSeparationResult(results); }
	@Nonnull
	public static FullSeparationResult of(@Nonnull SeparationResult singleResult) { return new FullSeparationResult(ImmutableList.of(singleResult)); }


	public boolean success() { return options.size() == 1 && options.get(0).success(); }

	@Nullable
	public SeparationResult getSuccessfulResult() { return options.size() == 1 ? options.get(0) : null; }


}
