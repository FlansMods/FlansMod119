package com.flansmod.teams.server.command;

import com.flansmod.common.FlansMod;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TeamArgument implements ArgumentType<String>
{
	private static final List<String> EXAMPLES = Arrays.asList("flansvendersgame:venditos", "createloveandwar:engineers");

	public static TeamArgument teamArgument() { return new TeamArgument(); }
	@Override
	public Collection<String> getExamples() { return EXAMPLES; }

	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException
	{
		return ResourceLocation.read(reader).toString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(FlansMod.TEAMS.getIds().stream().map(ResourceLocation::toString), builder);
	}
}
