package io.github.mortuusars.exposure.client.gui.screen.test;


import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class ShutterSpeedSlider extends Slider {
    private final List<ShutterSpeed> shutterSpeeds;

    public ShutterSpeedSlider(int x, int y, int width, int height, String name, List<ShutterSpeed> shutterSpeeds, ShutterSpeed defaultShutterSpeed, Consumer<Double> onChanged) {
        super(x, y, width, height, Math.max(shutterSpeeds.indexOf(defaultShutterSpeed), 0),
                0, shutterSpeeds.size() - 1, 0, name, onChanged);
        this.shutterSpeeds = shutterSpeeds;
    }

    public ShutterSpeed getShutterSpeed() {
        if (shutterSpeeds == null) return ShutterSpeed.DEFAULT;
        int index = getValue().intValue();
        if (index < 0 || index >= shutterSpeeds.size()) {
            return ShutterSpeed.DEFAULT;
        }
        return shutterSpeeds.get(index);
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(name + ": " + getShutterSpeed().getNotation()));
    }
}
