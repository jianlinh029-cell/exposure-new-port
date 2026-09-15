package io.github.mortuusars.exposure.neoforge.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ExposureJSEvents {
    EventGroup GROUP = EventGroup.of("ExposureEvents");

    EventHandler ADD_ENTITY_IN_FRAME_DATA = GROUP.common("addEntityInFrameExtraData", () -> ModifyEntityInFrameExtraDataEventJS.class);
    EventHandler MODIFY_FRAME_DATA = GROUP.server("modifyFrameExtraData", () -> ModifyFrameExtraDataEventJS.class);
    EventHandler FRAME_ADDED = GROUP.server("frameAdded", () -> FrameAddedEventJS.class);
}
