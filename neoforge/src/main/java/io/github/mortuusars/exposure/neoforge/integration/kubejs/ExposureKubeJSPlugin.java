package io.github.mortuusars.exposure.neoforge.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyEntityInFrameDataEvent;
import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.neoforge.api.event.ModifyFrameExtraDataEvent;
import io.github.mortuusars.exposure.neoforge.integration.kubejs.event.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

public class ExposureKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(ExposureJSEvents.GROUP);
    }

    @Override
    public void init() {
        subscribeToNeoForgeEvents();
    }

    private void subscribeToNeoForgeEvents() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, this::postAddEntityInFrameDataEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, this::postModifyFrameDataEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, this::postFrameAddedEvent);
    }

    // --

    private void postAddEntityInFrameDataEvent(ModifyEntityInFrameDataEvent event) {
        ExposureJSEvents.ADD_ENTITY_IN_FRAME_DATA.post(ScriptType.SERVER,
                new ModifyEntityInFrameExtraDataEventJS(event.getCameraHolder(), event.getCamera(), event.getEntityInFrame(), event.getData()));
    }

    private void postModifyFrameDataEvent(ModifyFrameExtraDataEvent event) {
        ExposureJSEvents.MODIFY_FRAME_DATA.post(ScriptType.SERVER,
                new ModifyFrameExtraDataEventJS(event.getCameraHolder(), event.getCamera(), event.getCaptureProperties(),
                        event.getPositionsInFrame(), event.getEntitiesInFrame(), event.getData()));
    }

    private void postFrameAddedEvent(FrameAddedEvent event) {
        ExposureJSEvents.FRAME_ADDED.post(ScriptType.SERVER,
                new FrameAddedEventJS(event.getCameraHolder(), event.getCamera(), event.getFrame(), event.getPositionsInFrame(), event.getEntitiesInFrame()));
    }
}