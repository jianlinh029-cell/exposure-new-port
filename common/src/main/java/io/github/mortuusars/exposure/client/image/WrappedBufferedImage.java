package io.github.mortuusars.exposure.client.image;

import java.awt.image.BufferedImage;

public class WrappedBufferedImage implements Image {
    private final BufferedImage bufferedImage;

    public WrappedBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    @Override
    public int width() {
        return bufferedImage.getWidth();
    }

    @Override
    public int height() {
        return bufferedImage.getHeight();
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return bufferedImage.getRGB(x, y);
    }
}
