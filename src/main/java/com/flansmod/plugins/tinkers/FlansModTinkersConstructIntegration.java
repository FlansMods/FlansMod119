package com.flansmod.plugins.tinkers;

import com.flansmod.common.FlansMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

public class FlansModTinkersConstructIntegration
{
	private static boolean _init = false;
	private static ITinkersIntegration _instance = null;
	public static ITinkersIntegration get()
	{
		if(!_init)
		{
			try
			{
				_instance = new FlansModTinkersPlugin();
			} catch (Exception e)
			{
				FlansMod.LOGGER.info("Tinker's Construct not found.");
			}
			_init = true;
		}
		return _instance;
	}

	public interface ITinkersIntegration
	{
		@Nonnull Item createPartItem(@Nonnull ResourceLocation loc);
	}

}
