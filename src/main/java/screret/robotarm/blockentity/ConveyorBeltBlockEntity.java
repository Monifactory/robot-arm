package screret.robotarm.blockentity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorSlope;
import screret.robotarm.util.SidedItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorBeltBlockEntity extends BlockEntity {

    public Boolean copiedItemsFromBelowIfTop = false;

    public final ItemStackHandler items;
    public final int tier;

    public int transferCooldown = 30;
    public int moveToCenterSpeed = 2;
    public int[] transferCooldownCounter;
    public int[] transferCooldownCounterLastTick = new int[10];
    public int[] transferSidewaysOffset;
    public int[] slotActuallyHasItem;

    public int[] sideTransferAttempts = new int[2];
    public long[] sideTransferLatestAttempt = new long[3];

    public ConveyorBeltBlockEntity(BlockEntityType<? extends ConveyorBeltBlockEntity> blockEntityType, BlockPos pos, BlockState state, int tier) {
        super(blockEntityType, pos, state);
        this.tier = tier;
        this.items = new ItemStackHandler(getSize()) {
            @Override
            protected void onContentsChanged(int slot) {
                ConveyorBeltBlockEntity.this.setChanged();
            }
        };
        transferCooldownCounter = new int[getSize()];
        transferSidewaysOffset = new int[getSize()];
        slotActuallyHasItem = new int[getSize()];
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            Direction facing = getBlockState().getValue(ConveyorBeltBlock.FACING);

            return LazyOptional.of(() -> new SidedItemHandler(this.items, facing, this::isValidInsertionSide)).cast();
        }
        return super.getCapability(cap, side);
    }

    public int getSize() {
        return 3 * tier + 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("items", items.serializeNBT());

        tag.putIntArray("TransferCooldownCounter", transferCooldownCounter);
        tag.putIntArray("TransferSidewaysOffset", transferSidewaysOffset);
        tag.putIntArray("SlotActuallyHasItem", slotActuallyHasItem);

        tag.putIntArray("SideTransferAttempts", sideTransferAttempts);
        tag.putLongArray("SideTransferLatestAttempt", sideTransferLatestAttempt);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        items.deserializeNBT(tag.getCompound("items"));

        transferCooldownCounter = tag.getIntArray("TransferCooldownCounter");
        transferSidewaysOffset = tag.getIntArray("TransferSidewaysOffset");
        slotActuallyHasItem = tag.getIntArray("SlotActuallyHasItem");

        sideTransferAttempts = tag.getIntArray("SideTransferAttempts");
        sideTransferLatestAttempt = tag.getLongArray("SideTransferLatestAttempt");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean canMoveToSlot(int slot) {
        if (slot == 4 || slot == 5) {
            return false; //for filter
        }
        return items.getStackInSlot(slot).isEmpty() && (slot == 0 || items.getStackInSlot(slot - 1).isEmpty()) &&
                (transferCooldownCounter[slot] >= (transferCooldown / 2) || transferCooldown < 4);
    }

    public boolean canMoveHereFromSide(Level level, int sideIndex) {
        if (sideIndex == 2) {
            //sideTransferLatestAttempt[2] is top side
            sideTransferLatestAttempt[2] = level.getGameTime();
            return canMoveToSlot(1);
        }

        sideTransferAttempts[sideIndex] += 1;
        sideTransferLatestAttempt[sideIndex] = level.getGameTime();

        if (sideTransferLatestAttempt[1 - sideIndex] < (level.getGameTime() - 1)) {
            //if last attempt on other side was more than a second ago, assume no longer trying
            sideTransferAttempts[1 - sideIndex] = 0;
        }

        if (!(sideTransferLatestAttempt[2] < (level.getGameTime() - 1))) {
            //hopper above trying to get in - takes priority
            return false;
        }

        if (!canMoveToSlot(1)) {
            //slot behind target belt trying to get in - takes priority
            return false;
        }

        //can go if this side has been trying for longer then the other
        return sideTransferAttempts[sideIndex] > sideTransferAttempts[1 - sideIndex];
    }

    public boolean moveToNextBelt(Direction direction, int fromSlot, @Nullable ConveyorBeltBlockEntity conveyorBlockEntityInfront) {
        if (conveyorBlockEntityInfront == null) {
            return false;
        }

        if (!conveyorBlockEntityInfront.getBlockState().getValue(ConveyorBeltBlock.ENABLED)) {
            return false;
        }

        if (!conveyorBlockEntityInfront.copiedItemsFromBelowIfTop && conveyorBlockEntityInfront.getBlockState().getValue(ConveyorBeltBlock.SLOPE) == ConveyorSlope.UP) {
            return false;//Prevents any items going into the top inventory before it is overwritten by the bottom one
        }

        if (conveyorBlockEntityInfront.getBlockState().getValue(ConveyorBeltBlock.SLOPE) == ConveyorSlope.UP) {
            return false;
        }

        Direction directionOfBeltInFront = conveyorBlockEntityInfront.level
                .getBlockState(conveyorBlockEntityInfront.worldPosition).getValue(ConveyorBeltBlock.FACING);

        if (directionOfBeltInFront == direction &&
                conveyorBlockEntityInfront.getBlockState().getValue(ConveyorBeltBlock.SLOPE) == ConveyorSlope.DOWN) {
            return false;
        }

        if (directionOfBeltInFront.getOpposite() == direction) {
            //belts are facing towards each-over, item can move
            return false;
        }

        if (conveyorBlockEntityInfront instanceof FilterConveyorBeltBlockEntity && directionOfBeltInFront != direction) {
            //output of filter belt is facing towards this, can't move
            return false;
        }

        int newOffset = transferSidewaysOffset[fromSlot];
        int newSlot = 0;
        int sideIndex = -1;
        boolean canMove = true;

        if (direction.getClockWise(Direction.Axis.Y) == directionOfBeltInFront) {
            newOffset = 50;
            newSlot = 1;
            sideIndex = 0;
        }

        if (direction.getCounterClockWise(Direction.Axis.Y) == directionOfBeltInFront) {
            newOffset = -50;
            newSlot = 1;
            sideIndex = 1;
        }

        if (sideIndex != -1) {
            if (!conveyorBlockEntityInfront.canMoveHereFromSide(level, sideIndex)) {
                canMove = false;
            }
        }

        if (conveyorBlockEntityInfront.slotActuallyHasItem[newSlot] > 0) {
            //Adds a tiny gap between items which prevents some strange behaviour where the cool-downs aren't reset on downward sloping belts when items are in a constant stream.
            return false;
        }

        if (!canMove) {
            return false;
        }

        //move item there
        if (!transferToEmpty(this.items, fromSlot, conveyorBlockEntityInfront.items, newSlot)) {
            return false;
        }

        if (sideIndex == 0 || sideIndex == 1) {
            //going onto left or right side of next belt, cooldown is halved as will be joining halfway into the second slot
            conveyorBlockEntityInfront.transferCooldownCounter[newSlot] = (int) ((float) conveyorBlockEntityInfront.transferCooldown * 0.5);
        } else {
            //going onto beck of next belt
            conveyorBlockEntityInfront.transferCooldownCounter[newSlot] = conveyorBlockEntityInfront.transferCooldown;
        }

        conveyorBlockEntityInfront.transferSidewaysOffset[newSlot] = newOffset;

        setChanged();
        conveyorBlockEntityInfront.setChanged();

        return true;
    }

    public boolean moveToNextBelt(Direction direction, ConveyorSlope slope, int fromSlot) {
        BlockPos offset = slope.getOffsetPos(worldPosition.relative(direction));
        ConveyorBeltBlockEntity conveyorBlockEntityInfront = getConveyorBlockEntityAt(level, offset);

        return moveToNextBelt(direction, fromSlot, conveyorBlockEntityInfront);
    }

    public void resetCooldowns(int slot) {
        transferCooldownCounter[slot] = transferCooldown;
        transferSidewaysOffset[slot] = 0;
    }

    public void updateCooldowns(int slot) {
        transferCooldownCounter[slot] -= 1;

        if (transferCooldownCounter[slot] <= 0) {
            transferCooldownCounter[slot] = 0;
        }

        if (Math.abs(transferSidewaysOffset[slot]) < moveToCenterSpeed) {
            transferSidewaysOffset[slot] = 0;
        } else if (transferSidewaysOffset[slot] > 0) {
            transferSidewaysOffset[slot] -= moveToCenterSpeed;
        } else if (transferSidewaysOffset[slot] < 0) {
            transferSidewaysOffset[slot] += moveToCenterSpeed;
        }
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, ConveyorBeltBlockEntity blockEntity) {
        if (Minecraft.getInstance().isSingleplayer()) {
            return;
        }

        if (!state.getValue(ConveyorBeltBlock.ENABLED)) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            blockEntity.updateCooldowns(i);
        }
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, ConveyorBeltBlockEntity blockEntity) {
        if (!state.getValue(ConveyorBeltBlock.ENABLED)) {
            blockEntity.updateSlotActuallyEmptyHack();
            return;
        }

        for (int i = 0; i < 3; i++) {
            if (blockEntity.items.getStackInSlot(i).isEmpty()) {
                //if empty slot, reset cool-downs
                blockEntity.resetCooldowns(i);
                continue;
            }

            blockEntity.updateCooldowns(i);

            //if cool-down has run down, meaning item can goto next slot
            if (blockEntity.transferCooldownCounter[i] <= 0) {
                blockEntity.transferCooldownCounter[i] = 0;

                if (i == 2) {
                    //if last slot in belt, will goto next belt
                    Direction direction = state.getValue(ConveyorBeltBlock.FACING);
                    ConveyorSlope slope = state.getValue(ConveyorBeltBlock.SLOPE);
                    blockEntity.moveToNextBelt(direction, slope, i);
                } else {
                    //else, moving from one slot to another within this belt
                    if (transferToEmpty(blockEntity.items, i, blockEntity.items, i + 1)) {
                        blockEntity.transferCooldownCounter[i + 1] = blockEntity.transferCooldown;
                        blockEntity.transferSidewaysOffset[i + 1] = blockEntity.transferSidewaysOffset[i];
                        blockEntity.setChanged();
                    }
                }
            }
        }

        blockEntity.updateSlotActuallyEmptyHack();
    }

    public static boolean transferToEmpty(IItemHandler from, int fromSlot, IItemHandler to, int toSlot) {
        if (!from.getStackInSlot(fromSlot).isEmpty() && to.getStackInSlot(toSlot).isEmpty()) {
            ItemStack toMove = from.extractItem(fromSlot, from.getStackInSlot(fromSlot).getCount(), false);
            to.insertItem(toSlot, toMove, false);
            return true;
        }
        return false;
    }

    @Nullable
    public static ConveyorBeltBlockEntity getConveyorBlockEntityAt(Level world, BlockPos pos) {
        ConveyorBeltBlockEntity inventory = null;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.hasBlockEntity()) {
            Block block = blockState.getBlock();
            if (block instanceof ConveyorBeltBlock) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                inventory = (ConveyorBeltBlockEntity) blockEntity;
            }
        }

        return inventory;
    }

    public void updateSlotActuallyEmptyHack() {
        int[] slotActuallyHasItemBefore = slotActuallyHasItem.clone();

        boolean needToUpdate = false;
        for (int i = 0; i < Math.min(slotActuallyHasItem.length, 4); i++) {
            slotActuallyHasItem[i] = items.getStackInSlot(i).isEmpty() ? slotActuallyHasItem[i] - 1 : 2;
            if (slotActuallyHasItem[i] < 0) {
                slotActuallyHasItem[i] = 0;
            }
            if (slotActuallyHasItem[i] != slotActuallyHasItemBefore[i]/* ||transferCooldownCounter[i] != transferCooldownCounterLastTick[i]*/) {
                needToUpdate = true;
            }
        }

        if (needToUpdate) {
            setChanged();
        }

        transferCooldownCounterLastTick = transferCooldownCounter.clone();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (this.hasLevel() && !this.getLevel().isClientSide) {
            ((ServerLevel) level).getChunkSource().blockChanged(getPos());
        }
    }

    public BlockPos getPos() {
        return this.worldPosition;
    }

    public boolean isValidInsertionSide(Direction side, int slot) {
        Direction facing = getBlockState().getValue(ConveyorBeltBlock.FACING);

        if (!getBlockState().getValue(ConveyorBeltBlock.ENABLED)) {
            return false;
        }

        if (facing.getOpposite() == side) {
            return slot == 0 && canMoveToSlot(0);
        }

        if (slot != 1) {
            return false;
        }

        if (side == Direction.UP) {
            return canMoveHereFromSide(Objects.requireNonNull(getLevel()), 2);
        }

        int sideIndex = -1;

        if (facing.getCounterClockWise(Direction.Axis.Y) == side) {
            sideIndex = 0;
        }

        if (facing.getClockWise(Direction.Axis.Y) == side) {
            sideIndex = 1;
        }

        if (sideIndex != -1) {
            return canMoveHereFromSide(Objects.requireNonNull(getLevel()), sideIndex);
        }

        return false;
    }
}

