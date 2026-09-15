package io.github.mortuusars.exposure.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class FlashBlock extends Block implements /*EntityBlock,*/ SimpleWaterloggedBlock {
    public static final int LIFETIME_TICKS = 8;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FlashBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState blockState) {
        return true;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, CollisionContext pContext) {
        return pContext.isHoldingItem(Items.LIGHT) ? Shapes.block() : Shapes.empty();
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, @NotNull LevelReader pLevel, @NotNull ScheduledTickAccess pScheduledTickAccess, @NotNull BlockPos pCurrentPos, @NotNull Direction pDirection, @NotNull BlockPos pNeighborPos, @NotNull BlockState pNeighborState, RandomSource randomSource) {
        if (pState.getValue(WATERLOGGED)) {
            pScheduledTickAccess.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pLevel, pScheduledTickAccess, pCurrentPos, pDirection, pNeighborPos, pNeighborState, randomSource);
    }

    public @NotNull FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, LIFETIME_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState newState = state.getValue(FlashBlock.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        level.setBlock(pos, newState, Block.UPDATE_ALL);
    }
}
