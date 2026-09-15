package io.github.mortuusars.exposure.core.camera;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FocalRangeTests {
    @Test
    void codecEncodeRange() {
        FocalRange focalRange = new FocalRange(12, 250);

        JsonObject jsonObject = FocalRange.CODEC.encodeStart(JsonOps.INSTANCE, focalRange).getOrThrow().getAsJsonObject();

        assertEquals(12, jsonObject.get("min").getAsInt());
        assertEquals(250, jsonObject.get("max").getAsInt());
    }

    @Test
    void codecDecodeRange() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("min", 24);
        jsonObject.addProperty("max", 180);

        FocalRange focalRange = FocalRange.CODEC.decode(JsonOps.INSTANCE, jsonObject).getOrThrow().getFirst();

        assertEquals(24, focalRange.min());
        assertEquals(180, focalRange.max());
    }

    @Test
    void codecEncodePrime() {
        FocalRange focalRange = new FocalRange(50);

        JsonPrimitive jsonPrimitive = FocalRange.CODEC.encodeStart(JsonOps.INSTANCE, focalRange).getOrThrow().getAsJsonPrimitive();

        assertTrue(jsonPrimitive.isNumber());
        assertEquals(50, jsonPrimitive.getAsInt());
    }

    @Test
    void codecDecodePrime() {
        JsonPrimitive jsonPrimitive = new JsonPrimitive(35);
        FocalRange focalRange = FocalRange.CODEC.decode(JsonOps.INSTANCE, jsonPrimitive).getOrThrow().getFirst();

        assertTrue(focalRange.isPrime());
        assertEquals(35, focalRange.min());
        assertEquals(35, focalRange.max());
    }
}
