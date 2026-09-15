package io.github.mortuusars.exposure.util.supporter;

import com.google.common.io.CharStreams;
import com.google.gson.*;
import io.github.mortuusars.exposure.Exposure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Supporters {
    private static final Gilded gilded = new Gilded();
    private static final Patreon patreon = new Patreon();

    public static void query() {
        gilded.query();
        patreon.query();
    }

    public static Gilded gilded() {
        return gilded;
    }

    public static Patreon patreon() {
        return patreon;
    }

    // --

    public static boolean hasAccessToGoldenSkin(UUID uuid) {
        return gilded().hasAccessToGoldenSkin(uuid) || patreon().hasAccessToGoldenSkin(uuid);
    }

    public static class Loader {
        public @Nullable String readFileFromURL(URI uri) {
            try {
                URL url = uri.toURL();
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000); // 10 seconds
                @Nullable String encoding = connection.getContentEncoding();
                Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);

                try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
                    return CharStreams.toString(reader);
                }
            } catch (Exception e) {
                Exposure.LOGGER.warn("Cannot read file from '{}': {}", uri, e.getMessage());
            }
            return null;
        }

        public @NotNull List<Supporter> parseSupporters(String json) {
            try {
                Gson gson = new Gson();
                JsonArray array = JsonParser.parseString(json).getAsJsonArray();
                List<Supporter> supporters = new ArrayList<>();

                for (JsonElement element : array) {
                    try {
                        Supporter p = gson.fromJson(element, Supporter.class);
                        supporters.add(p);
                    } catch (Exception e) {
                        Exposure.LOGGER.warn("Cannot parse supporter from '{}': {}", element, e.getMessage());
                    }
                }

                return supporters;
            } catch (Exception e) {
                Exposure.LOGGER.warn("Cannot get list of supporters: {}", e.getMessage());
                return Collections.emptyList();
            }
        }
    }
}
