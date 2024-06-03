package screret.robotarm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorSlope;

public class FilterConveyorBeltBlockEntity extends ConveyorBeltBlockEntity {

    public int lastRoundRobinOutDir = 0;

    public int outputMode = 0;
    public static int OUTPUT_MODE_NORMAL = 0;
    public static int OUTPUT_MODE_LEFT_FRONT = 1;
    public static int OUTPUT_MODE_RIGHT_FRONT = 2;

    public FilterConveyorBeltBlockEntity(BlockEntityType<? extends FilterConveyorBeltBlockEntity> blockEntityType, BlockPos pos, BlockState state, int tier) {
        super(blockEntityType, pos, state, tier);
        transferCooldownCounter = new int[4];
        transferSidewaysOffset = new int[4];
    }

    @Override
    public int getSize() {
        return 6 * tier + 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("LastRoundRobinOutDir", lastRoundRobinOutDir);
        tag.putInt("OutputMode", outputMode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        lastRoundRobinOutDir = tag.getInt("LastRoundRobinOutDir");
        outputMode = tag.getInt("OutputMode");
    }

    public static int CONTAINER_DATA_COUNT = 1;

    protected final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) {
                return outputMode;
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                if (level != null) {
                    outputMode = value;
                    level.setBlock(getPos(), getBlockState().setValue(ConveyorBeltBlock.OUTPUT_MODE, outputMode), 3);
                }
            }
        }

        @Override
        public int getCount() {
            return CONTAINER_DATA_COUNT;
        }
    };

    @Override
    public boolean canMoveHereFromSide(Level level, int sideIndex) {
        return false;
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, ConveyorBeltBlockEntity blockEntity) {
        if (!state.getValue(ConveyorBeltBlock.ENABLED)) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            blockEntity.updateCooldowns(i);
        }
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, FilterConveyorBeltBlockEntity blockEntity) {
        if (!state.getValue(ConveyorBeltBlock.ENABLED)) {
            blockEntity.updateSlotActuallyEmptyHack();
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (blockEntity.items.getStackInSlot(i).isEmpty()) {
                //if empty slot, reset cool-downs
                blockEntity.resetCooldowns(i);
                continue;
            }

            blockEntity.updateCooldowns(i);

            if (blockEntity.transferCooldownCounter[i] <= 0) {
                blockEntity.transferCooldownCounter[i] = 0;
                boolean moved = false;

                if (i == 2 || i == 3) {//the last slots for left and right directions
                    Direction direction = state.getValue(ConveyorBeltBlock.FACING);
                    if (i == 2 && blockEntity.outputMode != OUTPUT_MODE_LEFT_FRONT) {
                        direction = direction.getCounterClockWise(Direction.Axis.Y);
                    }
                    if (i == 3 && blockEntity.outputMode != OUTPUT_MODE_RIGHT_FRONT) {
                        direction = direction.getClockWise(Direction.Axis.Y);
                    }

                    ConveyorSlope slope = state.getValue(ConveyorBeltBlock.SLOPE);
                    blockEntity.moveToNextBelt(direction, slope, i);
                } else {
                    int toSlot;

                    if (i == 0) {
                        //input slot, another slot after this before filter
                        toSlot = i + 1;
                    } else {
                        //slot to filter
                        toSlot = blockEntity.getToSlotForFilter();
                    }

                    if (toSlot > -1) {
                        if (transferToEmpty(blockEntity.items, i, blockEntity.items, toSlot)) {
                            blockEntity.transferCooldownCounter[toSlot] = blockEntity.transferCooldown;
                            blockEntity.transferSidewaysOffset[toSlot] = blockEntity.transferSidewaysOffset[i];

                            if (toSlot > 1) {
                                blockEntity.lastRoundRobinOutDir = toSlot;
                            }

                            blockEntity.setChanged();
                        }
                    } else if (toSlot == -1) {
                        //can't go either way due to filters, is jammed
                        if (world.getGameTime() % 20 == 0) {
                            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }

        blockEntity.updateSlotActuallyEmptyHack();
    }

    public boolean slotStacksMatch(int inputSlot, int filterSlot) {
        return ItemStack.isSameItemSameTags(items.getStackInSlot(inputSlot), items.getStackInSlot(filterSlot));
    }

    public int getToSlotForFilter() {
        if (items.getStackInSlot(4).isEmpty() && items.getStackInSlot(5).isEmpty() ||
                (slotStacksMatch(1, 4) && slotStacksMatch(1, 5))) {
            //both filters are empty or both match input, could go either way

            if (items.getStackInSlot(2).isEmpty() && items.getStackInSlot(3).isEmpty()) {
                //both target slots are empty

                //pick whichever one was not used last
                if (lastRoundRobinOutDir != 2) {
                    return 2;
                }
                return 3;
            }

            //if only one target is empty, go there
            if (items.getStackInSlot(2).isEmpty()) {
                return 2;
            }

            if (items.getStackInSlot(3).isEmpty()) {
                return 3;
            }

            //no target slots are empty
            return -2;//not clogged, just backed up
        }

        if (slotStacksMatch(1, 4)) {
            return 2;
        }

        if (slotStacksMatch(1, 5)) {
            return 3;
        }

        if (items.getStackInSlot(4).isEmpty()) {
            return 2;
        }

        if (items.getStackInSlot(5).isEmpty()) {
            return 3;
        }

        return -1;
    }
}
