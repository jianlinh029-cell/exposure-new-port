package io.github.mortuusars.exposure.client.task;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.PalettedImage;
import io.github.mortuusars.exposure.client.image.ResourceImage;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import io.github.mortuusars.exposure.util.TranslatableError;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ExposureRetrieveTask extends Task<Result<List<Image>>> {
    public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.exposure_retrieve.timeout", "ERR_RETRIEVE_TIMED_OUT");
    public static final TranslatableError ERROR_FAILED = new TranslatableError("error.exposure.exposure_retrieve.failed", "ERR_RETRIEVE_FAILED");

    protected final List<ExposureIdentifier> identifiers;
    protected final int timeoutMs;

    protected final CompletableFuture<Result<List<Image>>> future = new CompletableFuture<>();
    protected final Image[] images;

    protected long startedAtMs = -1;

    public ExposureRetrieveTask(List<ExposureIdentifier> identifiers, int timeoutMs) {
        Preconditions.checkArgument(timeoutMs > 0,
                "Timeout should probably be larger than 0. What's the point of it then?");
        this.identifiers = identifiers;
        this.timeoutMs = timeoutMs;
        this.images = new Image[identifiers.size()];
    }

    @Override
    public CompletableFuture<Result<List<Image>>> execute() {
        if (!isStarted()) {
            setStarted();
            startedAtMs = System.currentTimeMillis();
        }
        return future;
    }

    @SuppressWarnings({"DataFlowIssue", "resource"})
    @Override
    public void tick() {
        if (System.currentTimeMillis() - startedAtMs > timeoutMs) {
            Exposure.LOGGER.error("Failed to retrieve exposures [{}]: Timed out. {}ms were not enough.",
                    String.join(",", identifiers.stream().map(ExposureIdentifier::toString).toList()), timeoutMs);
            setDone();
            future.complete(Result.error(ERROR_TIMED_OUT));
        }

        for (int i = 0; i < identifiers.size(); i++) {
            @Nullable Image image = images[i];
            if (image != null) continue;

            ExposureIdentifier identifier = identifiers.get(i);

            if (identifier.isId()) {
                RequestedPalettedExposure request = ExposureClient.exposureStore().getOrRequest(identifier.id());
                if (request.isError()) {
                    Exposure.LOGGER.error("Failed to retrieve exposures [{}]: unable to get exposure '{}' - {}.",
                            String.join(",", identifiers.stream().map(ExposureIdentifier::toString).toList()),
                            identifier.id(), request.getStatus());
                    setDone();
                    future.complete(Result.error(ERROR_FAILED));
                    return;
                }

                images[i] = request.getData().map(PalettedImage::fromExposure).orElse(null);
            }

            if (identifier.isTexture()) {
                images[i] = new ResourceImage(identifier.texture());
            }
        }

        if (Arrays.stream(images).allMatch(Objects::nonNull)) {
            setDone();
            future.complete(Result.success(Arrays.stream(images).toList()));
        }
    }
}
