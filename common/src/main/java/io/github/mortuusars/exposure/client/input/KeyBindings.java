package io.github.mortuusars.exposure.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class KeyBindings {
    public static final KeyBindings EMPTY = new KeyBindings();

    protected final ArrayList<KeyBinding> bindings = new ArrayList<>();

    public void add(KeyBinding... bindings) {
        this.bindings.addAll(Arrays.asList(bindings));
    }

    public void add(int index, KeyBinding binding) {
        this.bindings.add(index, binding);
    }

    public boolean remove(KeyBinding binding) {
        return this.bindings.remove(binding);
    }

    public void clear() {
        this.bindings.clear();
    }

    // --

    public boolean keyPressed(KeyEvent keyEvent) {
        for (KeyBinding binding : bindings) {
            if (binding.matches(keyEvent.input(), 0, InputConstants.PRESS, keyEvent.modifiers()) && binding.handler().get()) {
                return true;
            }
        }
        return false;
    }

    public boolean keyReleased(KeyEvent keyEvent) {
        for (KeyBinding binding : bindings) {
            if (binding.matches(keyEvent.input(), 0, InputConstants.RELEASE, keyEvent.modifiers()) && binding.handler().get()) {
                return true;
            }
        }
        return false;
    }

    // --

    public static KeyBindings of(KeyBinding... bindings) {
        KeyBindings list = new KeyBindings();
        list.add(bindings);
        return list;
    }
}
