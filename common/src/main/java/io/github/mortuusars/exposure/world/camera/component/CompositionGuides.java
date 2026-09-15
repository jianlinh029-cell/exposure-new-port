package io.github.mortuusars.exposure.world.camera.component;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class CompositionGuides {
    private static final List<CompositionGuide> GUIDES = new ArrayList<>();

    public static final CompositionGuide NONE = register(new CompositionGuide("none"));
    public static final CompositionGuide CROSSHAIR = register(new CompositionGuide("crosshair"));
    public static final CompositionGuide QUADS = register(new CompositionGuide("quads"));
    public static final CompositionGuide RULE_OF_THIRDS = register(new CompositionGuide("rule_of_thirds"));

    public static List<CompositionGuide> getGuides() {
        return ImmutableList.copyOf(GUIDES);
    }

    public static CompositionGuide byNameOrNone(String id) {
        for (CompositionGuide guide : GUIDES) {
            if (guide.name().equals(id))
                return guide;
        }

        return NONE;
    }

    public static CompositionGuide register(CompositionGuide guide) {
        GUIDES.add(guide);
        return guide;
    }

    public static CompositionGuide register(CompositionGuide guide, int index) {
        GUIDES.add(index, guide);
        return guide;
    }
}
