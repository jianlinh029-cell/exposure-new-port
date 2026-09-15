package io.github.mortuusars.exposure.core.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.data.ColorPalette;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorPaletteTests {
    @Test
    void smallerPaletteIsCorrectedProperly() {
        ColorPalette palette = new ColorPalette(new int[]{0xFF112233, 0xFF223344});
        assertEquals(256, palette.colors().length);
        assertEquals(0x00000000, palette.colors()[255]);
        assertEquals(0xFF000000, palette.colors()[2]);
        assertEquals(0xFF000000, palette.colors()[24]);
        assertEquals(0xFF000000, palette.colors()[254]);
    }

    @Test
    void byIdReturnsCorrectColor() {
        ColorPalette palette = new ColorPalette(new int[]{0xFF112233, 0xFF223344});
        assertEquals(0xFF112233, palette.byId(0));
        assertEquals(0xFF223344, palette.byId(1));
        assertEquals(0x00000000, palette.byId(255));
        assertEquals(0xFF000000, palette.byId(2));
        assertEquals(0xFF000000, palette.byId(24));
        assertEquals(0xFF000000, palette.byId(254));
    }

    @Test
    void hexCodec() {
        ColorPalette palette = new ColorPalette(new int[] { 0x7F7F7F7F, 0xFF112233});
        JsonObject jsonObject = ColorPalette.CODEC.encodeStart(JsonOps.INSTANCE, palette).getOrThrow().getAsJsonObject();
        JsonArray jsonArray = jsonObject.get("colors").getAsJsonArray();

        assertEquals("7F7F7F7F", jsonArray.get(0).getAsString());
        assertEquals("FF112233", jsonArray.get(1).getAsString());
        assertEquals("FF000000", jsonArray.get(254).getAsString());
        assertEquals("00000000", jsonArray.get(255).getAsString());

        String json = """
                {
                    "colors": [
                        "7F7F7F7F",
                        "33001122",
                        "FFAABBCC"
                    ]
                }
                """;

        ColorPalette decodedPalette = ColorPalette.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json)).getOrThrow().getFirst();

        assertEquals(0x7F7F7F7F, decodedPalette.byId(0));
        assertEquals(0x33001122, decodedPalette.byId(1));
        assertEquals(0xFFAABBCC, decodedPalette.byId(2));
        assertEquals(0xFF000000, decodedPalette.byId(254));
        assertEquals(0x00000000, decodedPalette.byId(255));
    }
}
