package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class Timer {
    public void set(CameraHolder holder, ItemStack stack, int ticks) {
        long gameTime = holder.asHolderEntity().level().getGameTime();
        setStartTick(stack, gameTime);
        setEndTick(stack, gameTime + ticks);
    }

    public void stop(ItemStack stack) {
        setStartTick(stack, -1L);
        setEndTick(stack, -1L);
    }

    public boolean isTicking(CameraHolder holder, ItemStack stack) {
        return getEndTick(stack) > holder.asHolderEntity().level().getGameTime();
    }

    // --

    public int getRemainingTicks(CameraHolder holder, ItemStack stack) {
        return (int)Math.max(-1, getEndTick(stack) - holder.asHolderEntity().level().getGameTime());
    }

    public long getTicksSinceLastRelease(CameraHolder holder, ItemStack stack) {
        return holder.asHolderEntity().level().getGameTime() - getLastReleaseTick(stack);
    }

    // --

    public long getStartTick(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.TIMER_START_TICK, -1L);
    }

    public void setStartTick(ItemStack stack, long tick) {
        stack.set(Exposure.DataComponents.TIMER_START_TICK, tick);
    }

    public long getEndTick(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.TIMER_END_TICK, -1L);
    }

    public void setEndTick(ItemStack stack, long tick) {
        stack.set(Exposure.DataComponents.TIMER_END_TICK, tick);
    }

    public long getLastReleaseTick(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.TIMER_LAST_RELEASE_TICK, -1L);
    }

    public void setLastReleaseTick(ItemStack stack, long tick) {
        stack.set(Exposure.DataComponents.TIMER_LAST_RELEASE_TICK, tick);
    }

    // --

    /**
     * @return true if state has changed.
     */
    public boolean tick(CameraHolder holder, ServerLevel level, ItemStack stack) {
        long releaseTick = getEndTick(stack);
        if (releaseTick <= -1L) return false;
        long currentTick = level.getGameTime();
        long remainingTicks = releaseTick - currentTick;

        if (remainingTicks < -5) {
            // Ignore if release tick was passed some time ago.
            // To not release when player drops or puts camera in chest and then picks up after some time.
            stop(stack);
            return true;
        }

        if (remainingTicks == 0) {
            setEndTick(stack, currentTick);
            setLastReleaseTick(stack, currentTick);
            if (stack.getItem() instanceof CameraItem cameraItem) {
                cameraItem.release(holder, stack);
            }
            stop(stack);
            return true;
        }

        if (remainingTicks % getTickingInterval(remainingTicks) == 0) {
            playTickSound(holder);
        }

        return false;
    }

    // --

    protected void playTickSound(CameraHolder holder) {
        Sound.play(holder.asHolderEntity(), Exposure.SoundEvents.CAMERA_TIMER_TICK.get(), SoundSource.PLAYERS, 1, 0.8f);
    }

    protected int getTickingInterval(long remainingTicks) {
        if (remainingTicks > 100) return 10;
        if (remainingTicks > 50) return 6;
        if (remainingTicks > 25) return 4;
        return 2;
    }
}
