package com.flansmod.teams.server;

import com.flansmod.teams.api.OpResult;
import com.flansmod.teams.api.admin.ISettings;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Settings implements ISettings
{
	public final Map<String, Boolean> booleanParameters = new HashMap<>();
	public final Map<String, Integer> intParameters = new HashMap<>();
	public final Map<String, Float> floatParameters = new HashMap<>();

	public Settings()
	{

	}
	public Settings(@Nonnull Settings other)
	{
		booleanParameters.putAll(other.booleanParameters);
		intParameters.putAll(other.intParameters);
		floatParameters.putAll(other.floatParameters);
	}

	@Override
	public boolean getBooleanParameter(@Nonnull String parameterName)
	{
		return booleanParameters.get(parameterName);
	}
	@Override @Nonnull
	public OpResult setBooleanParameter(@Nonnull String parameterName, boolean set)
	{
		booleanParameters.put(parameterName, set);
		return OpResult.SUCCESS;
	}
	@Override
	public int getIntegerParameter(@Nonnull String parameterName)
	{
		return intParameters.get(parameterName);
	}
	@Override @Nonnull
	public OpResult setIntegerParameter(@Nonnull String parameterName, int set)
	{
		intParameters.put(parameterName, set);
		return OpResult.SUCCESS;
	}
	@Override
	public float getFloatParameter(@Nonnull String parameterName)
	{
		return floatParameters.get(parameterName);
	}
	@Override @Nonnull
	public OpResult setFloatParameter(@Nonnull String parameterName, float set)
	{
		floatParameters.put(parameterName, set);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult trySetParameter(@Nonnull String parameterName, @Nonnull String parse)
	{
		try {
			if(floatParameters.containsKey(parameterName)) {
				floatParameters.put(parameterName, Float.parseFloat(parse));
				return OpResult.SUCCESS;
			}
		}
		catch(Exception ignored) {}

		try {
			if(intParameters.containsKey(parameterName)) {
				intParameters.put(parameterName, Integer.parseInt(parse));
				return OpResult.SUCCESS;
			}
		}
		catch(Exception ignored) {}

		try {
			if(booleanParameters.containsKey(parameterName)) {
				booleanParameters.put(parameterName, Boolean.parseBoolean(parse));
				return OpResult.SUCCESS;
			}
		}
		catch(Exception ignored) {}

		return OpResult.FAILURE_GENERIC;
	}

}
