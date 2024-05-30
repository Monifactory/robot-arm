package screret.robotarm.blockentity;

import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import screret.robotarm.entity.FOUPCartEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote CableBlockEntity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FOUPRailBlockEntity extends BlockEntity {

    @Setter
    public FOUPCartEntity awaitedCart = null;

    public FOUPRailBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Nullable
    public FOUPCartEntity getAwaitedCart() {
        if (awaitedCart == null) return null;
        if (!awaitedCart.isAwaiting() || !getBlockPos().equals(awaitedCart.getAwaitedPos().orElse(null))) {
            awaitedCart = null;
        }
        return awaitedCart;
    }

}
