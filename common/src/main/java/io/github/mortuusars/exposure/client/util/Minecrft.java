package io.github.mortuusars.exposure.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class Minecrft {
    public static Minecraft get() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return Objects.requireNonNull(get().player, "Player is not available.");
    }

    public static ClientLevel level() {
        return Objects.requireNonNull(get().level, "Level is not available.");
    }

    public static RegistryAccess registryAccess() {
        return level().registryAccess();
    }

    public static boolean hasControlDown() {
        long window = get().getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public static boolean hasShiftDown() {
        long window = get().getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    public static MultiPlayerGameMode gameMode() {
        return Objects.requireNonNull(get().gameMode, "GameMode is not available.");
    }

    public static Options options() {
        return get().options;
    }

    public static void execute(Runnable runnable) {
        get().execute(runnable);
    }

    public static void releaseUseButton() {
        options().keyUse.setDown(false);
    }

    public static void stopPlayerMovement() {
        // Stop player moving if movement key is held
        Minecrft.player().xxa = 0;
        Minecrft.player().yya = 0;
        Minecrft.player().zza = 0;
        Minecrft.player().setJumping(false);
        options().keyUp.setDown(false);
        options().keyDown.setDown(false);
        options().keyLeft.setDown(false);
        options().keyRight.setDown(false);
        options().keyJump.setDown(false);
    }
}
