package io.github.mortuusars.exposure.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImageIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.21.11: AbstractTexture/TextureManager were reworked around GpuTexture and no longer expose
 * pixel access. ResourceImage now loads the resource pack image directly into a NativeImage and
 * does not register with the texture manager.
 */
public class ResourceImage implements RenderableImage {
    private static final Map<Identifier, ResourceImage> CACHE = new HashMap<>();

    @Nullable
    protected NativeImage image;
    protected final Identifier location;

    public ResourceImage(Identifier location) {
        this.location = location;
    }

    public static @NotNull RenderableImage getOrCreate(Identifier location) {
        ResourceImage cached = CACHE.get(location);
        if (cached != null && cached.image != null) {
            return cached;
        }

        try {
            ResourceImage texture = new ResourceImage(location);
            CACHE.put(location, texture);
            return texture;
        }
        catch (Exception e) {
            Exposure.LOGGER.error("Cannot load texture [{}]. {}", location, e);
            return RenderableImage.MISSING;
        }
    }

    @Override
    public int width() {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? image.getWidth() : 1;
    }

    @Override
    public int height() {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? image.getHeight() : 1;
    }

    @Override
    public int getPixelARGB(int x, int y) {
        @Nullable NativeImage image = getNativeImage();
        return image != null ? image.getPixel(x, y) : 0x00000000;
    }

    public @Nullable NativeImage getNativeImage() {
        if (this.image != null)
            return image;

        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location)) {
            this.image = NativeImage.read(stream);
            return image;
        } catch (IOException e) {
            Exposure.LOGGER.error("Cannot load texture: {}", e.toString());
            return null;
        }
    }

    @Override
    public void close() {
        if (this.image != null) {
            image.close();
            image = null;
        }
    }

    @Override
    public Image getImage() {
        return this;
    }

    @Override
    public RenderableImageIdentifier getIdentifier() {
        return new RenderableImageIdentifier(location.toString());
    }
}
