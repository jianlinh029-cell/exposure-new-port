package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ModifiedImage;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;

public interface PixelEffect extends ImageEffect {
    int modify(int colorARGB);

    default Image modify(Image image) {
        return new ModifiedImage(image, this::modify);
    }
}
