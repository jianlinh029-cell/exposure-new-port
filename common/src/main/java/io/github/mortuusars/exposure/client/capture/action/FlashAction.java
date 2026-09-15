package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.block.FlashBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlashAction implements CaptureAction {
    private final Entity cameraHolder;
    private long initializedAt;

    public FlashAction(Entity cameraHolder) {
        this.cameraHolder = cameraHolder;
    }

    @Override
    public int requiredDelayTicks() {
        return Config.Client.FLASH_CAPTURE_DELAY_TICKS.get(); // This is important. Without a delay flash effect might not apply in time.
    }

    @Override
    public void initialize() {
        initializedAt = cameraHolder.level().getGameTime();
    }

    @Override
    public void beforeCapture() {
        long ticksDelay = cameraHolder.level().getGameTime() - initializedAt;
        if (ticksDelay > FlashBlock.LIFETIME_TICKS) {
            Exposure.LOGGER.warn("Capturing with delay of '{}' ticks can be too long for a flash to have an effect. " +
                    "The flash might disappear in that time.", ticksDelay);
        }
    }

    @Override
    public void afterCapture() {
        // Spawning particles after capture because they will be visible on a frame otherwise.
        Level level = cameraHolder.level();
        Vec3 pos = cameraHolder.position();
        Vec3 lookAngle = cameraHolder.getLookAngle();
        pos = pos.add(0, 1.1, 0).add(lookAngle.scale(0.8));

        RandomSource r = level.getRandom();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.x + r.nextFloat() - 0.5f,
                    pos.y + r.nextFloat() + 0.15f,
                    pos.z + r.nextFloat() - 0.5f,
                    lookAngle.x * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.y * 0.025f + r.nextFloat() * 0.025f,
                    lookAngle.z * 0.025f + r.nextFloat() * 0.025f);
        }
    }
}
