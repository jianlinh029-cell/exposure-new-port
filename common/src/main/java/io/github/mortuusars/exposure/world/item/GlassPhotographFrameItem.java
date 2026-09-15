package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.world.entity.GlassPhotographFrameEntity;
import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GlassPhotographFrameItem extends PhotographFrameItem {
    public GlassPhotographFrameItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull PhotographFrameEntity createEntity(Level level, BlockPos pos, Direction direction) {
        return new GlassPhotographFrameEntity(level, pos, direction);
    }
}