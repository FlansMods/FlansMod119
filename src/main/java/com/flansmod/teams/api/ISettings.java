package com.flansmod.teams.api;

import javax.annotation.Nonnull;

public interface ISettings
{
	String DEFAULT_KEY = "default";

	boolean getBooleanParameter(@Nonnull String parameterName);
	@Nonnull
	OpResult setBooleanParameter(@Nonnull String parameterName, boolean set);
	int getIntegerParameter(@Nonnull String parameterName);
	@Nonnull
	OpResult setIntegerParameter(@Nonnull String parameterName, int set);
	float getFloatParameter(@Nonnull String parameterName);
	@Nonnull
	OpResult setFloatParameter(@Nonnull String parameterName, float set);

	@Nonnull
	OpResult trySetParameter(@Nonnull String parameterName, @Nonnull String parse);
}
