package screret.robotarm.block;

import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.block.properties.ConveyorSlope;
import screret.robotarm.blockentity.ConveyorBeltBlockEntity;
import screret.robotarm.data.blockentity.RobotArmBlockEntities;
import screret.robotarm.util.ConveyorBeltState;
import screret.robotarm.util.RobotArmTags;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorBeltBlock extends BaseEntityBlock {
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ConveyorSlope> SLOPE = EnumProperty.create("slope", ConveyorSlope.class);
    public static final IntegerProperty OUTPUT_MODE = IntegerProperty.create("output_mode", 0, 2);

    private final int tier;

    public ConveyorBeltBlock(BlockBehaviour.Properties properties, int tier) {
        super(properties);
        this.tier = tier;

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(ENABLED, true)
                .setValue(FACING, Direction.NORTH)
                .setValue(SLOPE, ConveyorSlope.NONE)
                .setValue(OUTPUT_MODE, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED, FACING, SLOPE, OUTPUT_MODE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        Player player = context.getPlayer();

        if (player == null) {
            return this.defaultBlockState();
        }

        if (player.isShiftKeyDown()) {
            facing = facing.getOpposite();
        }

        return this.defaultBlockState()
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(FACING, facing);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 6, 16);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ConveyorBeltBlockEntity conveyorBeltBlockEntity) {
                for (int i = 0; i < conveyorBeltBlockEntity.items.getSlots(); ++i) {
                    ItemStack stack = conveyorBeltBlockEntity.items.getStackInSlot(i);
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                }

                level.updateNeighborsAt(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        IItemTransfer transfer = ItemTransferHelper.getItemTransfer(level, pos, null);
        if (transfer == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for (int j = 0; j < transfer.getSlots(); ++j) {
                ItemStack itemstack = transfer.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    f += (float) itemstack.getCount() / (float) Math.min(transfer.getSlotLimit(j), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float) transfer.getSlots();
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(RobotArmTags.CONVEYORS)) {
            //Holding a belt
            player.displayClientMessage(Component.translatable("text.robot_arm.belt_place_on_belt.message")
                    .withStyle(ChatFormatting.GRAY), true);

            return InteractionResult.PASS;//Allow placing of belt on this without shifting
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, RobotArmBlockEntities.CONVEYOR_BELT.get(),
                level.isClientSide ? ConveyorBeltBlockEntity::clientTick : ConveyorBeltBlockEntity::serverTick);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            this.updateState(level, pos, state, movedByPiston);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        this.updateState(level, pos, state, movedByPiston);
    }

    private void updateState(Level level, BlockPos pos, BlockState state, boolean movedByPiston) {
        //state = this.updateDir(level, pos, state);

        boolean bl = !level.hasNeighborSignal(pos);
        if (bl != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, bl), 2);
        }
    }

    protected BlockState updateDir(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return state;
        } else {
            Direction direction = state.getValue(FACING);
            ConveyorSlope slope = state.getValue(SLOPE);
            return new ConveyorBeltState(level, pos, state).place(direction, slope).getState();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorBeltBlockEntity(RobotArmBlockEntities.CONVEYOR_BELT.get(), pos, state, this.tier);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!state.getValue(ConveyorBeltBlock.ENABLED)) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            if (!living.isShiftKeyDown()) {
                Direction direction = state.getValue(ConveyorBeltBlock.FACING);
                Vec3i vectorI = direction.getNormal();
                Vec3 vector = new Vec3(vectorI.getX() * 0.1 * tier, vectorI.getY() * 0.1 * tier, vectorI.getZ() * 0.1 * tier);
                moveEntityOn(vector, living);
            }
        } else if (entity instanceof ItemEntity item && !level.isClientSide) {
            ItemStack entityStack = item.getItem().copy();
            ConveyorBeltBlockEntity be = (ConveyorBeltBlockEntity) level.getBlockEntity(pos);
            for (int i = 0; i < be.items.getSlots(); ++i) {
                ItemStack test = be.items.insertItem(i, entityStack, true);
                if (test.getCount() < item.getItem().getCount()) {
                    ItemStack push = be.items.insertItem(i, entityStack, false);
                    item.getItem().shrink(item.getItem().getCount() - push.getCount());
                    if (item.getItem().isEmpty()) {
                        break;
                    }
                }
            }
        }

        super.stepOn(level, pos, state, entity);
    }

    public void moveEntityOn(Vec3 vector, LivingEntity livingEntity) {
        vector = vector.multiply(1.5f, 1.5f, 1.5f);
        livingEntity.addDeltaMovement(vector);
    }

    @Nullable
    public ResourceLocation getNextCycleBeltType() {
        return null;
    }
}
