package io.github.mortuusars.exposure.world.lightroom;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.function.Supplier;

public enum PrintingProcess {
    BLACK_AND_WHITE(Config.Server.LIGHTROOM_BW_DYES,
            Config.Server.LIGHTROOM_BW_PRINT_TIME,
            Config.Server.LIGHTROOM_BW_EXPERIENCE),
    COLOR(Config.Server.LIGHTROOM_COLOR_DYES,
            Config.Server.LIGHTROOM_COLOR_PRINT_TIME,
            Config.Server.LIGHTROOM_COLOR_EXPERIENCE),
    CHROMATIC_R(Config.Server.LIGHTROOM_CHROMATIC_RED_DYES,
            Config.Server.LIGHTROOM_CHROMATIC_PRINT_TIME,
            () -> 0),
    CHROMATIC_G(Config.Server.LIGHTROOM_CHROMATIC_GREEN_DYES,
            Config.Server.LIGHTROOM_CHROMATIC_PRINT_TIME,
            () -> 0),
    CHROMATIC_B(Config.Server.LIGHTROOM_CHROMATIC_BLUE_DYES,
            Config.Server.LIGHTROOM_CHROMATIC_PRINT_TIME,
            Config.Server.LIGHTROOM_CHROMATIC_EXPERIENCE);

    private final Supplier<List<? extends String>> requiredDyes;
    private final Supplier<Integer> printTime;
    private final Supplier<Integer> xpPerPrint;

    PrintingProcess(Supplier<List<? extends String>> requiredDyes, Supplier<Integer> printTime, Supplier<Integer> xpPerPrint) {
        this.requiredDyes = requiredDyes;
        this.printTime = printTime;
        this.xpPerPrint = xpPerPrint;
    }

    public List<DyeColor> getRequiredDyes() {
        return requiredDyes.get().stream().map(name -> DyeColor.byName(name.toLowerCase(), DyeColor.BLACK)).toList();
    }

    public int getPrintTime() {
        return printTime.get();
    }

    public int getExperiencePerPrint() {
        return xpPerPrint.get();
    }

    public boolean isRegular() {
        return this == BLACK_AND_WHITE || this == COLOR;
    }

    public boolean isChromatic() {
        return this == CHROMATIC_R || this == CHROMATIC_G || this == CHROMATIC_B;
    }

    public static PrintingProcess fromExposureType(ExposureType type) {
        return type == ExposureType.COLOR ? COLOR : BLACK_AND_WHITE;
    }

    public static PrintingProcess fromChromaticStep(int step) {
        return switch (step) {
            case 0 -> CHROMATIC_R;
            case 1 -> CHROMATIC_G;
            case 2 -> CHROMATIC_B;
            default ->
                    throw new IllegalStateException("Unexpected step value: " + step + ", 0|1|2 corresponding to R|G|B is expected.");
        };
    }
}
