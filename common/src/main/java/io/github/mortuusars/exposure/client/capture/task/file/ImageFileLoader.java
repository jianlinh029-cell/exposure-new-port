package io.github.mortuusars.exposure.client.capture.task.file;

import com.google.common.io.Files;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.client.image.Image;

import java.io.File;

public interface ImageFileLoader {
    Result<Image> load(File file);

    static ImageFileLoader chooseFitting(File file) {
        return !Files.getFileExtension(file.toString()).equals("png")
                ? new BufferedImageFileLoader()
                : fallback(new NativeImagePngFileLoader(), new BufferedImageFileLoader());
    }

    static ImageFileLoader fallback(ImageFileLoader main, ImageFileLoader fallback) {
        return file -> {
            Result<Image> result = main.load(file);

            if (result.isError()) {
                Exposure.LOGGER.info("Loading image with main loader failed. Using fallback...");
                return fallback.load(file);
            }

            return result;
        };
    }
}
