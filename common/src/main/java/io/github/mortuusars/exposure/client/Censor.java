package io.github.mortuusars.exposure.client;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class Censor {
    public static boolean isAllowedToRender(Frame frame) {
        if (frame.identifier().isTexture()) {
            return true;
        }

        @Nullable Player player = Minecraft.getInstance().player;

        if (Config.Client.HIDE_ALL_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && (player == null || !frame.isTakenBy(player))) {
            return false;
        }

        if (Config.Client.HIDE_PROJECTED_PHOTOGRAPHS_MADE_BY_OTHERS.get()
                && frame.isProjected()
                && (player == null || !frame.isTakenBy(player))) {
            return false;
        }

        return true;
    }
}
