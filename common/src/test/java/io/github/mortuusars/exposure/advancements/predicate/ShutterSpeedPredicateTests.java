package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ShutterSpeedPredicateTests {
    @Test
    void decodesFromString() {
        ShutterSpeedPredicate decodedPredicate = ShutterSpeedPredicate.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive("1/125")).getOrThrow().getFirst();

        assertEquals(ShutterSpeedPredicate.exact(new ShutterSpeed("1/125")), decodedPredicate);
    }

    @Test
    void decodesFromFullMinMax() {
        String json = """
                {
                    "min": "1/125",
                    "max": "1/2"
                }
                """;

        ShutterSpeedPredicate decodedPredicate = ShutterSpeedPredicate.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json))
                .getOrThrow().getFirst();

        assertEquals(ShutterSpeedPredicate.between(new ShutterSpeed("1/125"), new ShutterSpeed("1/2")), decodedPredicate);
    }

    @Test
    void decodeWithMinLargerThanMaxThrows() {
        String json = """
                {
                    "min": "1/2",
                    "max": "1/60"
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> {
            ShutterSpeedPredicate decodedPredicate = ShutterSpeedPredicate.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json))
                    .getOrThrow().getFirst();
        });
    }

    @Test
    void encodeToSimpleIfExact() {
        ShutterSpeedPredicate predicate = ShutterSpeedPredicate.exact(new ShutterSpeed("1/4"));
        JsonPrimitive primitive = ShutterSpeedPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).getOrThrow().getAsJsonPrimitive();

        assertEquals("1/4", primitive.getAsJsonPrimitive().getAsString());
    }

    @Test
    void encodeToFullIfFull() {
        ShutterSpeedPredicate predicate = ShutterSpeedPredicate.between(new ShutterSpeed("1/4"), new ShutterSpeed("1\""));
        JsonObject jsonObject = ShutterSpeedPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).getOrThrow().getAsJsonObject();

        String expectedJson = """
        {"min":"1/4","max":"1\\""}""";

        assertEquals(expectedJson, jsonObject.toString());
    }

    @Test
    void minMatches() {
        ShutterSpeedPredicate predicate = ShutterSpeedPredicate.atLeast(new ShutterSpeed("1/4"));

        assertFalse(predicate.matches(new ShutterSpeed("1/60")));
        assertFalse(predicate.matches(new ShutterSpeed("1/8")));
        assertTrue(predicate.matches(new ShutterSpeed("1/4")));
        assertTrue(predicate.matches(new ShutterSpeed("1/2")));
        assertTrue(predicate.matches(new ShutterSpeed("2\"")));
        assertTrue(predicate.matches(new ShutterSpeed("15\"")));
    }

    @Test
    void maxMatches() {
        ShutterSpeedPredicate predicate = ShutterSpeedPredicate.atMost(new ShutterSpeed("1/4"));

        assertTrue(predicate.matches(new ShutterSpeed("1/60")));
        assertTrue(predicate.matches(new ShutterSpeed("1/8")));
        assertTrue(predicate.matches(new ShutterSpeed("1/4")));
        assertFalse(predicate.matches(new ShutterSpeed("1/2")));
        assertFalse(predicate.matches(new ShutterSpeed("2\"")));
        assertFalse(predicate.matches(new ShutterSpeed("15\"")));
    }

    @Test
    void anyMatches() {
        ShutterSpeedPredicate predicate = new ShutterSpeedPredicate(Optional.empty(), Optional.empty());

        assertTrue(predicate.matches(new ShutterSpeed("1/60")));
        assertTrue(predicate.matches(new ShutterSpeed("1/8")));
        assertTrue(predicate.matches(new ShutterSpeed("1/4")));
        assertTrue(predicate.matches(new ShutterSpeed("1/2")));
        assertTrue(predicate.matches(new ShutterSpeed("2\"")));
        assertTrue(predicate.matches(new ShutterSpeed("15\"")));
    }

    @Test
    void exactMatches() {
        ShutterSpeedPredicate predicate = ShutterSpeedPredicate.exact(new ShutterSpeed("1/4"));

        assertFalse(predicate.matches(new ShutterSpeed("1/60")));
        assertFalse(predicate.matches(new ShutterSpeed("1/8")));
        assertTrue(predicate.matches(new ShutterSpeed("1/4")));
        assertFalse(predicate.matches(new ShutterSpeed("1/2")));
        assertFalse(predicate.matches(new ShutterSpeed("2\"")));
        assertFalse(predicate.matches(new ShutterSpeed("15\"")));
    }
}
