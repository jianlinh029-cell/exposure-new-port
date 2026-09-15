package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;

import net.minecraft.core.Holder;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

public class ColorPaletteArgument extends IdentifierArgument {
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider provider = (SharedSuggestionProvider) context.getSource();
        Set<Identifier> keys = provider.registryAccess().lookup(Exposure.Registries.COLOR_PALETTE)
                .map(lookup -> lookup.listElements().map(holder -> holder.key().identifier()).collect(Collectors.toSet()))
                .orElse(Set.of());
        return SharedSuggestionProvider.suggestResource(keys, builder);
    }
}
