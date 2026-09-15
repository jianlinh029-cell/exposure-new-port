package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.data.export.ExportSize;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SizeMultiplierArgument implements ArgumentType<ExportSize> {
    @Override
    public ExportSize parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readString();
        @Nullable ExportSize size = ExportSize.byName(string);

        if (size == null)
            throw new SimpleCommandExceptionType(Component.translatable("argument.enum.invalid", string)).create();

        return size;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(ExportSize.values())
                .map(ExportSize::getSerializedName), builder);
    }

    public static ExportSize getSize(final CommandContext<?> context, final String name) {
        return context.getArgument(name, ExportSize.class);
    }
}
