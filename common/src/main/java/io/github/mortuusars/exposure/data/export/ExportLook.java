package io.github.mortuusars.exposure.data.export;

import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExportLook implements StringRepresentable {
    REGULAR("regular", ImageEffect.EMPTY),
    AGED("aged", ImageEffect.AGED),
    NEGATIVE("negative", ImageEffect.NEGATIVE),
    NEGATIVE_FILM("negative_film", ImageEffect.NEGATIVE_FILM);

    private final String name;
    private final ImageEffect modifier;

    ExportLook(String name, ImageEffect modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public static @Nullable ExportLook byName(String name) {
        for (ExportLook value : values()) {
            if (value.getSerializedName().equals(name))
                return value;
        }

        return null;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ImageEffect getModifier() {
        return modifier;
    }

    public String getIdSuffix() {
        return this != REGULAR ? "_" + getSerializedName() : "";
    }
}