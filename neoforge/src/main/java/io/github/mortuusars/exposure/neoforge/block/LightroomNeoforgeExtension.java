package io.github.mortuusars.exposure.neoforge.block;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class LightroomNeoforgeExtension {
    public static final BlockCapability<IItemHandler, @Nullable Direction> ITEM_HANDLER_BLOCK_CAPABILITY =
            BlockCapability.createSided(Exposure.resource("lightroom_item_handler"), IItemHandler.class);

    public BlockCapabilityCache<IItemHandler, @Nullable Direction> CAPABILITY_CACHE;
}
