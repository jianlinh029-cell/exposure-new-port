package io.github.mortuusars.exposure.world.sound;

import net.minecraft.sounds.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public record SoundEffect(Supplier<SoundEvent> sound, float volume, float pitch, float pitchVariability) {
    public SoundEffect(Supplier<SoundEvent> sound, float volume, float pitch) {
        this(sound, volume, pitch, 0F);
    }

    public SoundEffect(Supplier<SoundEvent> sound, float volume) {
        this(sound, volume, 1F, 0F);
    }

    public SoundEffect(Supplier<SoundEvent> sound) {
        this(sound, 1F, 1F, 0F);
    }

    public SoundEvent get() {
        return sound().get();
    }

    public float getFinalPitch() {
        return pitch - (pitchVariability / 2) + ThreadLocalRandom.current().nextFloat() * pitchVariability;
    }
}
