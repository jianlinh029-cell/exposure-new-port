package io.github.mortuusars.exposure.event;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mortuusars.exposure.commands.ExposureCommand;
import io.github.mortuusars.exposure.commands.ShaderCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommonEvents {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                        CommandBuildContext context, Commands.CommandSelection environment) {
        ExposureCommand.register(dispatcher);
        ShaderCommand.register(dispatcher);
    }
}
