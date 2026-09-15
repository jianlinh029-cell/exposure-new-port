package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ViewfinderShader implements AutoCloseable {
    private final Minecraft minecraft;
    private final Camera camera;
    private final Viewfinder viewfinder;

    @Nullable
    private Identifier shader;
    private boolean active;

    public ViewfinderShader(Camera camera, Viewfinder viewfinder) {
        this.minecraft = Minecrft.get();
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.update();
    }

    public void apply(Identifier shaderLocation) {
        if (shader != null && shader.equals(shaderLocation)) {
            return;
        }

        shader = shaderLocation;
        active = true;
    }

    /**
     * Processes current viewfinder shader (if it is present and active).
     */
    public void process() {
        if (shader != null && active) {
            PostChain postChain = minecraft.getShaderManager().getPostChain(shader, LevelTargetBundle.MAIN_TARGETS);
            if (postChain != null) {
                try (CrossFrameResourcePool allocator = new CrossFrameResourcePool(3)) {
                    postChain.process(minecraft.getMainRenderTarget(), allocator);
                }
            }
        }
    }

    public void update() {
        setActive(viewfinder.isLookingThrough());
        if (active) {
            ItemStack filterStack = Attachment.FILTER.get(camera.getItemStack()).getForReading();
            java.util.Optional<Filter> matched = Filters.of(Minecrft.registryAccess(), filterStack);
            matched.ifPresentOrElse(filter -> {
                // [FilterDebug] log only when the matched filter changes (glass block / tint / shader).
                if (this.shader == null || !this.shader.equals(filter.shader())) {
                    Exposure.LOGGER.info("[FilterDebug] glassBlock={} glassColor={} viewfinderShader={}",
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(filterStack.getItem()),
                            filter.attachmentTintColor(), filter.shader());
                }
                apply(filter.shader());
            }, this::remove);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void remove() {
        shader = null;
    }

    @Override
    public void close() {
        remove();
    }
}
