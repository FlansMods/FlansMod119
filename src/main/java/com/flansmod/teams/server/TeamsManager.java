package com.flansmod.teams.server;

import com.flansmod.teams.api.*;

import javax.annotation.Nonnull;
import java.util.*;

public class TeamsManager implements
		ITeamsAdmin,
		ITeamsRuntime
{
	private final Map<String, MapInfo> maps = new HashMap<>();
	private final List<String> mapRotation = new ArrayList<>();
	private String currentMap = TeamsAPI.INVALID_MAP_NAME;
	private String nextMap = TeamsAPI.INVALID_MAP_NAME;
	private boolean isRotationEnabled = false;

	private final Map<String, Settings> settings = new HashMap<>();
	private final Settings defaultMapSettings;

	public TeamsManager()
	{
		defaultMapSettings = new Settings();
		settings.put(ISettings.DEFAULT_KEY, defaultMapSettings);
	}

	@Override @Nonnull
	public Collection<String> getMapNames() { return maps.keySet(); }
	@Override
	public boolean hasMap(@Nonnull String mapName) { return maps.containsKey(mapName); }

	@Override @Nonnull
	public String getCurrentMap() { return currentMap; }
	@Override @Nonnull
	public IMap getMapData(@Nonnull String mapName) { return maps.get(mapName); }
	@Override @Nonnull
	public ISettings getDefaultSettings() { return defaultMapSettings; }
	@Override @Nonnull
	public Collection<String> getMapRotation() { return mapRotation; }
	@Override @Nonnull
	public String getNextMap()
	{
		if(isRotationEnabled && !mapRotation.isEmpty())
		{
			int index = mapRotation.indexOf(currentMap);
			return mapRotation.get(index + 1);
		}
		return nextMap;
	}


	@Override @Nonnull
	public OpResult createMap(@Nonnull String mapName)
	{
		if(maps.containsKey(mapName))
			return OpResult.FAILURE_GENERIC;
		OpResult nameCheck = TeamsAPI.isValidMapName(mapName);
		if(nameCheck.failure())
			return nameCheck;

		MapInfo newMap = new MapInfo();
		maps.put(mapName, newMap);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult deleteMap(@Nonnull String mapName)
	{
		return OpResult.SUCCESS;
	}

	@Override @Nonnull
	public OpResult enableMapRotation()
	{
		isRotationEnabled = true;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult disableMapRotation()
	{
		isRotationEnabled = false;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult setMapRotation(@Nonnull List<String> mapNames)
	{
		mapRotation.clear();
		mapRotation.addAll(mapNames);
		return OpResult.SUCCESS;
	}

	@Override @Nonnull
	public OpResult addMapToRotation(@Nonnull String mapName, int positionHint) {
		if(positionHint >= 0)
		{
			if(mapRotation.size() >= positionHint)
			{
				mapRotation.add(positionHint, mapName);
				return OpResult.SUCCESS;
			}
			else return OpResult.FAILURE_INVALID_MAP_INDEX;
		}
		mapRotation.add(mapName);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult removeMapFromRotation(@Nonnull String mapName)
	{
		if(mapRotation.contains(mapName)) {
			mapRotation.remove(mapName);
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_INVALID_MAP_NAME;
	}
	@Override @Nonnull
	public OpResult removeMapFromRotation(int inPosition)
	{
		if(mapRotation.size() > inPosition) {
			mapRotation.remove(inPosition);
			return OpResult.SUCCESS;
		}
		return OpResult.FAILURE_INVALID_MAP_INDEX;
	}

	@Override @Nonnull
	public OpResult createNewSettings(@Nonnull String settingsName)
	{
		if(settings.containsKey(settingsName))
			return OpResult.FAILURE_GENERIC;
		OpResult nameCheck = TeamsAPI.isValidMapName(settingsName);
		if(nameCheck.failure())
			return nameCheck;

		Settings newSettings = new Settings(defaultMapSettings);
		settings.put(settingsName, newSettings);
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult registerIntParameter(@Nonnull String parameterName, int defaultValue)
	{
		return defaultMapSettings.setIntegerParameter(parameterName, defaultValue);
	}
	@Override @Nonnull
	public OpResult registerBooleanParameter(@Nonnull String parameterName, boolean defaultValue)
	{
		return defaultMapSettings.setBooleanParameter(parameterName, defaultValue);
	}
	@Override @Nonnull
	public OpResult setNextMap(@Nonnull String mapName)
	{
		if(isRotationEnabled)
			return OpResult.FAILURE_GENERIC;
		nextMap = mapName;
		return OpResult.SUCCESS;
	}
	@Override @Nonnull
	public OpResult goToNextMap()
	{
		return OpResult.SUCCESS;
	}
}
