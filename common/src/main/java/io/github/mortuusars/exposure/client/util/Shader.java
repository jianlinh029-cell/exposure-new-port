package io.github.mortuusars.exposure.client.util;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;


public class Shader {
    private static boolean suppressViewfinder = false;

    public static void setSuppressViewfinder(boolean suppress) {
        suppressViewfinder = suppress;
    }

    public static void processForGameRenderer() {
        if (!suppressViewfinder && CameraClient.viewfinder() != null) {
            CameraClient.viewfinder().shader().process();
        }

        if (CaptureShader.hasShader()) {
            CaptureShader.process();
        }
    }

    public static void resize(int width, int height) {
        // 1.21.11: viewfinder shader no longer owns a resizable PostChain; no-op.
    }
}
