package io.github.mortuusars.exposure.integration;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.integration.compat.RealCameraCompat;

public class ModCompatibilityClient {
    private static final String REAL_CAMERA = "realcamera";

    public static void handle() {
        if (PlatformHelper.isModLoaded(REAL_CAMERA)) {
            try {
                RealCameraCompat.init();
            }
            catch (Exception e) {
                Exposure.LOGGER.error("Failed to apply Real Camera compatibility.", e);
            }
        }
    }
}
