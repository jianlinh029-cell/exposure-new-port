package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;

import java.util.function.Function;

public interface RenderableImage extends Image {
    RenderableImage EMPTY = new RenderableImage.Instance(Image.EMPTY, new RenderableImageIdentifier("-empty"));
    RenderableImage MISSING = new RenderableImage.Instance(Image.MISSING, new RenderableImageIdentifier("-missing"));

    Image getImage();
    RenderableImageIdentifier getIdentifier();

    default boolean isEmpty() {
        return this.equals(EMPTY) || (!getImage().equals(this) && getImage().isEmpty());
    }

    default RenderableImage modifyWith(Function<Image, Image> transformFunction, String variant) {
        Image image = transformFunction.apply(getImage());
        RenderableImageIdentifier identifier = getIdentifier().appendVariant(variant);
        return new Instance(image, identifier);
    }

    default RenderableImage modifyWith(ImageEffect modifier) {
        return modifyWith(modifier::modify, modifier.getIdentifier());
    }

    static RenderableImage of(String id, Image image) {
        return new Instance(image, new RenderableImageIdentifier(id));
    }

    class Instance extends Image.Wrapped implements RenderableImage {
        private final RenderableImageIdentifier identifier;

        public Instance(Image image, RenderableImageIdentifier identifier) {
            super(image);
            this.identifier = identifier;
        }

        @Override
        public RenderableImageIdentifier getIdentifier() {
            return identifier;
        }
    }
}
