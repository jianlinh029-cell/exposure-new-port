package io.github.mortuusars.exposure.neoforge;

import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ExposureNeoForgeClient {
    public static void init(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ModCompatibilityClient.handle();
    }
}
