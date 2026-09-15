package io.github.mortuusars.exposure.world.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class AlbumPlayerInventorySlot extends Slot {
    protected boolean isActive;

    public AlbumPlayerInventorySlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean value) {
        isActive = value;
    }
}
