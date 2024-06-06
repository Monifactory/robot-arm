package screret.robotarm.block;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.block.properties.ConveyorOutputMode;
import screret.robotarm.blockentity.ConveyorBeltBlockEntity;
import screret.robotarm.blockentity.FilterConveyorBeltBlockEntity;
import screret.robotarm.data.block.RobotArmBlocks;
import screret.robotarm.data.blockentity.RobotArmBlockEntities;

public class FilterConveyorBeltBlock extends ConveyorBeltBlock {

    public static final EnumProperty<ConveyorOutputMode> OUTPUT_MODE = EnumProperty.create("output_mode", ConveyorOutputMode.class);


    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(2, 6, 2, 14, 10, 14),
            Block.box(0, 6, 0, 2, 10, 2),
            Block.box(0, 6, 14, 2, 10, 16),
            Block.box(14, 6, 0, 16, 10, 2),
            Block.box(14, 6, 14, 16, 10, 16),
            Block.box(0, 10, 0, 16, 13, 16)
    );

    public FilterConveyorBeltBlock(Properties properties, int tier) {
        super(properties, tier);

        this.registerDefaultState(this.defaultBlockState()
                .setValue(OUTPUT_MODE, ConveyorOutputMode.NORMAL)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED, FACING, OUTPUT_MODE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (ToolHelper.getToolTypes(itemStack).contains(GTToolType.CROWBAR)) {
            FilterConveyorBeltBlockEntity be = (FilterConveyorBeltBlockEntity) level.getBlockEntity(pos);
            if (be != null) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.items.getStackInSlot(4));
            }
            BlockState newState = RobotArmBlocks.CONVEYOR_BELTS[this.tier].getDefaultState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(ENABLED, state.getValue(ENABLED));
            level.setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RobotArmBlockEntities.FILTER_CONVEYOR_BELT.get(),
                level.isClientSide ? ConveyorBeltBlockEntity::clientTick : FilterConveyorBeltBlockEntity::serverTick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FilterConveyorBeltBlockEntity(RobotArmBlockEntities.FILTER_CONVEYOR_BELT.get(), pos, state, this.tier);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {

    }
}
