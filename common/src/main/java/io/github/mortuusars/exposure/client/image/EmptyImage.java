package io.github.mortuusars.exposure.client.image;

public class EmptyImage implements Image {
    @Override
    public int width() {
        return 1;
    }

    @Override
    public int height() {
        return 1;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return 0x00000000;
    }
}
