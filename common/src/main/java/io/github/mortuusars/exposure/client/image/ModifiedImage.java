package io.github.mortuusars.exposure.client.image;

import java.util.function.Function;

public class ModifiedImage extends Image.Wrapped {
    protected final Function<Integer, Integer> modifier;

    public ModifiedImage(Image image, Function<Integer, Integer> modifier) {
        super(image);
        this.modifier = modifier;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        return modifier.apply(getImage().getPixelARGB(x, y));
    }
}
