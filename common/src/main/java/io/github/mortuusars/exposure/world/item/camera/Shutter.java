package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.logging.log4j.util.TriConsumer;

public class Shutter {
    protected TriConsumer<CameraHolder, ServerLevel, ItemStack> onOpen = (entity, level, stack) -> { };
    protected TriConsumer<CameraHolder, ServerLevel, ItemStack> onClosed = (entity, level, stack) -> { };

    public void onOpen(TriConsumer<CameraHolder, ServerLevel, ItemStack> onOpen) {
        this.onOpen = onOpen;
    }

    /**
     * Will not be executed when closing time should've been "long" ago.
     * When camera wasn't in inventory at the time of closing, for example.
     */
    public void onClosed(TriConsumer<CameraHolder, ServerLevel, ItemStack> onClosed) {
        this.onClosed = onClosed;
    }

    public ShutterState getState(ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.SHUTTER_STATE, ShutterState.CLOSED);
    }

    public void setState(ItemStack stack, ShutterState shutterState) {
        stack.set(Exposure.DataComponents.SHUTTER_STATE, shutterState);
    }

    public boolean isOpen(ItemStack stack) {
        return getState(stack).isOpen();
    }

    public boolean shouldClose(ItemStack stack, long gameTime) {
        ShutterState state = getState(stack);
        boolean projecting = CameraInstances.getOptional(stack).map(CameraInstance::isWaitingForProjection).orElse(false);
        return state.isOpen() && !projecting && gameTime >= state.getCloseTick();
    }

    /**
     * @return true if shutter state has changed.
     */
    public boolean tick(CameraHolder holder, ServerLevel level, ItemStack stack) {
        long gameTime = holder.asHolderEntity().level().getGameTime();
        if (shouldClose(stack, gameTime)) {
            ShutterState state = getState(stack);
            if (gameTime - state.getCloseTick() > 200) {
                setState(stack, ShutterState.CLOSED);
            } else {
                close(holder, level, stack);
            }
            return true;
        }
        return false;
    }

    public void open(CameraHolder holder, ServerLevel level, ItemStack stack, ShutterSpeed shutterSpeed) {
        setState(stack, ShutterState.open(level.getGameTime(), shutterSpeed));
        holder.asHolderEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playOpenSound(holder);
        onOpen.accept(holder, level, stack);
    }

    public void close(CameraHolder holder, ServerLevel level, ItemStack stack) {
        setState(stack, ShutterState.closed());
        holder.asHolderEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        playCloseSound(holder);
        onClosed.accept(holder, level, stack);
    }

    public void playOpenSound(CameraHolder holder) {
        Entity entity = holder.asHolderEntity();
        Sound.play(entity, Exposure.SoundEvents.SHUTTER_OPEN.get(), entity.getSoundSource(), 0.7f, 1.1f, 0.2f);
    }

    public void playCloseSound(CameraHolder holder) {
        Entity entity = holder.asHolderEntity();
        Sound.play(entity, Exposure.SoundEvents.SHUTTER_CLOSE.get(), entity.getSoundSource(), 0.7f, 1.1f, 0.2f);
    }
}
