package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.animation.CameraPoses;
import io.github.mortuusars.exposure.client.capture.template.*;
import io.github.mortuusars.exposure.client.task.ClearStaleRenderedImagesIndefiniteTask;
import io.github.mortuusars.exposure.client.RenderedExposures;
import io.github.mortuusars.exposure.client.camera.viewfinder.*;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.render.image.ImageRenderer;
import io.github.mortuusars.exposure.client.render.model.ExposureItemModelProperties;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.render.photograph.PhotographRenderer;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyles;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.photograph.PhotographType;
import io.github.mortuusars.exposure.util.cycles.Cycles;
import io.github.mortuusars.exposure.client.ExposureStore;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.item.ChromaticSheetItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class ExposureClient {
    private static final Cycles CYCLES = new Cycles();
    private static final ExposureStore EXPOSURE_STORE = new ExposureStore();
    private static final RenderedExposures RENDERED_EXPOSURES = new RenderedExposures();
    private static final ImageRenderer IMAGE_RENDERER = new ImageRenderer();
    private static final PhotographRenderer PHOTOGRAPH_RENDERER = new PhotographRenderer();

    public static void init() {
        CameraModelPoses.register(Exposure.Items.CAMERA.get(), new CameraPoses());

        ViewfinderRegistry.register(Exposure.Items.CAMERA.get(), Viewfinder::new);

        CaptureTemplates.register(CaptureType.CAMERA, new CameraCaptureTemplate());
        CaptureTemplates.register(CaptureType.EXPOSE_COMMAND, new ExposeCaptureTemplate());
        CaptureTemplates.register(CaptureType.LOAD_COMMAND, new PathCaptureTemplate());
        CaptureTemplates.register(CaptureType.DEBUG_RGB, new SingleChannelCaptureTemplate());

        PhotographStyles.register(PhotographType.REGULAR, PhotographStyle.REGULAR);
        PhotographStyles.register(PhotographType.AGED, new PhotographStyle(
                ExposureClient.Textures.Photograph.AGED_PAPER,
                ExposureClient.Textures.Photograph.AGED_OVERLAY,
                ExposureClient.Textures.Photograph.AGED_ALBUM_PAPER,
                ExposureClient.Textures.Photograph.AGED_ALBUM_OVERLAY,
                ImageEffect.AGED));

        cycles().addParallelTask(new ClearStaleRenderedImagesIndefiniteTask());

        ExposureItemModelProperties.register();
    }

    public static Cycles cycles() {
        return CYCLES;
    }

    public static ExposureStore exposureStore() {
        return EXPOSURE_STORE;
    }

    public static RenderedExposures renderedExposures() {
        return RENDERED_EXPOSURES;
    }

    public static ImageRenderer imageRenderer() {
        return IMAGE_RENDERER;
    }

    public static PhotographRenderer photographRenderer() {
        return PHOTOGRAPH_RENDERER;
    }

    // --

    public static boolean shouldUseDirectCapture() {
        //TODO: maybe check if neoforge is ok and then enable it only on fabric?
        if (PlatformHelper.isModLoaded("distanthorizons")
                && (PlatformHelper.isModLoaded("oculus") || PlatformHelper.isModLoaded("iris"))) {
            return true;
        }

        return Config.Client.FORCE_DIRECT_CAPTURE.isTrue()
                || Config.Client.FORCE_DIRECT_CAPTURE_MODS.get().stream().anyMatch(PlatformHelper::isModLoaded);
    }

    // --

    public static class Models {
        // Item models used by the custom camera item model (and by resource packs via select properties).
        public static final Set<Identifier> CAMERA_MODELS = new HashSet<>();

        public static final Identifier CAMERA_DEFAULT = registerCamera("item/camera");
        public static final Identifier CAMERA_GUI = registerCamera("item/camera_gui");
        public static final Identifier CAMERA_GOLD = registerCamera("item/camera_gold");
        public static final Identifier CAMERA_GOLD_GUI = registerCamera("item/camera_gold_gui");
        public static final Identifier CAMERA_ACTIVE = registerCamera("item/camera_active");
        public static final Identifier CAMERA_GOLD_ACTIVE = registerCamera("item/camera_gold_active");
        public static final Identifier CAMERA_SELFIE = registerCamera("item/camera_selfie");
        public static final Identifier CAMERA_GOLD_SELFIE = registerCamera("item/camera_gold_selfie");
        public static final Identifier CAMERA_VIEWFINDER = registerCamera("item/camera_parts/viewfinder");
        public static final Identifier CAMERA_FLASH = registerCamera("item/camera_parts/flash");
        public static final Identifier CAMERA_LENS = registerCamera("item/camera_parts/lens");
        public static final Identifier CAMERA_SELFIE_STICK = registerCamera("item/camera_parts/selfie_stick");
        public static final Identifier SELFIE_STICK = registerCamera("item/selfie_stick");

        // Block models registered as extra models (Fabric: ModelLoadingPlugin + ExtraModelKey).
        public static final Set<Identifier> BLOCK_MODELS = new HashSet<>();

        public static final Identifier PHOTOGRAPH_FRAME_SMALL = registerBlock("block/photograph_frame_small");
        public static final Identifier PHOTOGRAPH_FRAME_MEDIUM = registerBlock("block/photograph_frame_medium");
        public static final Identifier PHOTOGRAPH_FRAME_LARGE = registerBlock("block/photograph_frame_large");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_SMALL = registerBlock("block/glass_photograph_frame_small");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_MEDIUM = registerBlock("block/glass_photograph_frame_medium");
        public static final Identifier CLEAR_PHOTOGRAPH_FRAME_LARGE = registerBlock("block/glass_photograph_frame_large");
        public static final Identifier CAMERA_STAND = registerBlock("block/camera_stand");
        public static final Identifier CAMERA_STAND_MOUNT = registerBlock("block/camera_stand_mount");

        public static Identifier registerCamera(String path) {
            Identifier location = Exposure.resource(path);
            CAMERA_MODELS.add(location);
            return location;
        }

        public static Identifier registerBlock(String path) {
            Identifier location = Exposure.resource(path);
            BLOCK_MODELS.add(location);
            return location;
        }
    }

    public static class Textures {
        public static final Identifier EMPTY = Exposure.resource("textures/empty.png");

        public static class Photograph {
            public static final Identifier REGULAR_PAPER = Exposure.resource("textures/photograph/photograph.png");
            public static final Identifier REGULAR_ALBUM_PAPER = Exposure.resource("textures/photograph/photograph_album.png");

            public static final Identifier AGED_PAPER = Exposure.resource("textures/photograph/aged_photograph.png");
            public static final Identifier AGED_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_overlay.png");
            public static final Identifier AGED_ALBUM_PAPER = Exposure.resource("textures/photograph/aged_photograph_album.png");
            public static final Identifier AGED_ALBUM_OVERLAY = Exposure.resource("textures/photograph/aged_photograph_album_overlay.png");
        }
    }
}
