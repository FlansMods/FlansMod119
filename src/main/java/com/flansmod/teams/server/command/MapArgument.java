package com.flansmod.teams.server.command;

import com.flansmod.teams.common.TeamsMod;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapArgument implements ArgumentType<String>
{
	private static final List<String> EXAMPLES = Arrays.asList("myMapName", "anotherMapName");

	public static MapArgument mapArgument() { return new MapArgument(); }
	@Override
	public Collection<String> getExamples() { return EXAMPLES; }

	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException
	{
		return reader.readUnquotedString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(TeamsMod.MANAGER.getAllMaps(), builder);
	}
}

