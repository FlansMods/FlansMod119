package com.flansmod.client.render;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlansModRenderCore
{
	public FlansModRenderCore()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnReigsterModels(ModelEvent.RegisterAdditional event)
	{
		/*
		for(var kvp : CustomRenderers.entrySet())
		{
			//event.register();

			ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(kvp.getKey());
			FlanItemModel model = kvp.getValue().createModel(null, itemID.getNamespace(), itemID.getPath());

			if(model != null)
			{
				for(var location : model.getModelLocations())
					event.register(new ModelResourceLocation(location, "inventory"));
			}
		}
		 */
	}

	@SubscribeEvent
	public void OnModelBake(ModelEvent.ModifyBakingResult event)
	{

	}


}
