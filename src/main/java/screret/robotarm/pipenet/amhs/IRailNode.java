package screret.robotarm.pipenet.amhs;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.lowdragmc.lowdraglib.LDLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import screret.robotarm.block.AMHSRailBlock;

import javax.annotation.Nullable;

public interface IRailNode extends ITickSubscription, IPaintable {

    long getOffsetTimer();

    /**
     * Get Cover Container.
     */
    ICoverable getCoverContainer();

    /**
     * should be called when neighbours / inner changed or the first time placing this pipe.
     */
    default void updateConnections() {
        var net = getRailNet();
        if (net != null) {
            var pos = getRailPos();
            net.onNeighbourUpdate(pos);
        }
    }

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Level getRailLevel() {
        return self().getLevel();
    }

    default BlockPos getRailPos() {
        return self().getBlockPos();
    }

    default void markAsDirty() {
        self().setChanged();
    }

    default boolean isInValid() {
        return self().isRemoved();
    }

    default boolean isRemote() {
        var level = getRailLevel();
        if (level == null) {
            return LDLib.isRemote();
        }
        return level.isClientSide;
    }

    default AMHSRailBlock getRailBlock() {
        return (AMHSRailBlock) self().getBlockState().getBlock();
    }

    @Nullable
    default AMHSRailNet getRailNet() {
        if (getRailLevel() instanceof ServerLevel serverLevel) {
            return getRailBlock().getWorldRailNet(serverLevel).getNetFromPos(getRailPos());
        }
        return null;
    }

    default Direction getRailDirection() {
        return getRailBlock().getRailDirection(self().getBlockState());
    }

    default RailConnection getRailConnection() {
        return getRailBlock().getRailConnection(self().getBlockState());
    }

    default void setRailDirection(Direction direction) {
        var oldState = self().getBlockState();
        var newState = getRailBlock().setRailDirection(oldState, direction);
        if (oldState != newState) {
            getRailLevel().setBlockAndUpdate(getRailPos(), newState);
        }
    }

    default void setRailConnection(RailConnection connection) {
        var oldState = self().getBlockState();
        var newState = getRailBlock().setRailConnection(oldState, connection);
        if (oldState != newState) {
            getRailLevel().setBlockAndUpdate(getRailPos(), newState);
        }
    }

    default AMHSRailType getRailType() {
        return getRailBlock().railType;
    }

    default void notifyBlockUpdate() {
        var level = getRailLevel();
        if (level != null) {
            level.updateNeighborsAt(getRailPos(), level.getBlockState(getRailPos()).getBlock());
        }
    }

    default void scheduleRenderUpdate() {
        var pos = getRailPos();
        var level = getRailLevel();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, 1 << 3);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default void serverTick() {

    }

    default void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        if (!isRemote()) {
            updateConnections();
        }
        getCoverContainer().onNeighborChanged(block, fromPos, isMoving);
    }

    default void scheduleNeighborShapeUpdate() {
        Level level = getRailLevel();
        BlockPos pos = getRailPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    @Override
    default int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

}
