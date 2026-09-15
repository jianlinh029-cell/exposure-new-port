package io.github.mortuusars.exposure.util;

import net.minecraft.util.Mth;

public enum PagingDirection {
    PREVIOUS(-1),
    NEXT(1);

    private final int value;

    PagingDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PagingDirection fromChange(int oldPage, int newPage) {
        int value = Mth.sign(newPage - oldPage);
        for (PagingDirection direction : values()) {
            if (value == direction.getValue()) return direction;
        }
        return NEXT;
    }
}
