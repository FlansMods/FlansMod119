package com.flansmod.common.network.toclient;

import com.flansmod.common.network.FlansModMessage;
import com.flansmod.common.types.JsonDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class DefinitionDatapackMessage<TDefinitionType extends JsonDefinition> extends FlansModMessage
{
	private final Map<ResourceLocation, TDefinitionType> definitions;

	public DefinitionDatapackMessage()
	{
		definitions = new HashMap<>();
	}
	public DefinitionDatapackMessage(@Nonnull Map<ResourceLocation, TDefinitionType> defs)
	{
		definitions = defs;
	}

	@Override
	public void Encode(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeInt(definitions.size());
		for(var kvp : definitions.entrySet())
		{
			buf.writeResourceLocation(kvp.getKey());

			//getCodec().encode(kvp.getValue(), buf, );
		}
	}

	@Override
	public void Decode(@Nonnull FriendlyByteBuf buf)
	{
		int count = buf.readInt();


	}
}
