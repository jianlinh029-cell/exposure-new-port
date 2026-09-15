package io.github.mortuusars.exposure.world.level.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public record ExpectedExposure(String id, long timeoutAt, BiConsumer<ServerPlayer, String> onReceived) {
    public boolean isTimedOut(long currentUnixTimeSeconds) {
        return currentUnixTimeSeconds > timeoutAt;
    }
}
