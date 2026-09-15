package io.github.mortuusars.exposure.client.capture.task.file;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.client.capture.task.FileCaptureTask;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.client.image.Image;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BufferedImageFileLoader implements ImageFileLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public Result<Image> load(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            BufferedImage image = ImageIO.read(inputStream);

            if (image.getWidth() > 10_000 || image.getHeight() > 10_000) {
                LOGGER.error("Cannot load image from file '{}': image is too large.", file);
                return Result.error(FileCaptureTask.ERROR_CANNOT_READ);
            }

            return Result.success(new WrappedBufferedImage(image));
        } catch (IOException e) {
            Exposure.LOGGER.error("Loading image from file path '{}' failed:", file, e);
            return Result.error(FileCaptureTask.ERROR_CANNOT_READ);
        }
    }
}
