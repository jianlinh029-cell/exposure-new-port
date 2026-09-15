package io.github.mortuusars.exposure.client.image;

import com.google.common.base.Preconditions;

public interface Image extends AutoCloseable {
    Image EMPTY = new EmptyImage();
    Image MISSING = new MissingImage();

    int width();
    int height();
    int getPixelARGB(int x, int y);
    default void close() {}

    default boolean isEmpty() {
        return this.equals(EMPTY) || (width() <= 1 && height() <= 1 && getPixelARGB(0, 0) == 0x00000000);
    }

    static void validate(int width, int height, int pixelCount) {
        Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", width);
        Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", height);
        Preconditions.checkArgument(pixelCount == width * height,
                "Pixel count '%s' is not correct for image dimensions of '%sx%s'. " +
                        "Count should be '%s'.", pixelCount, width, height, width * height);
    }

    abstract class Wrapped implements Image {
        private final Image image;

        public Wrapped(Image image) {
            this.image = image;
        }

        public Image getImage() {
            return image;
        }

        @Override
        public int width() {
            return image.width();
        }

        @Override
        public int height() {
            return image.height();
        }

        @Override
        public int getPixelARGB(int x, int y) {
            return image.getPixelARGB(x, y);
        }

        @Override
        public void close() {
            image.close();
        }

        @Override
        public boolean isEmpty() {
            return getImage().isEmpty();
        }
    }
}
