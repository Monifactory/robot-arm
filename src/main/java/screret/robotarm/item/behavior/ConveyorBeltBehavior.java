package screret.robotarm.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.HitResult;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.data.machine.RobotArmMachines;

import java.util.function.Supplier;

public class ConveyorBeltBehavior implements IInteractionItem {
    private Supplier<ConveyorBeltBlock> block;

    public ConveyorBeltBehavior(Supplier<ConveyorBeltBlock> block) {
        this.block = block;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var hitResult = context.getHitResult();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockPos = hitResult.getBlockPos();
            var direction = hitResult.getDirection();
            var world = context.getLevel();
            var pos = blockPos.relative(direction);
            if (world.getBlockState(pos).isAir()) {
                if (!world.isClientSide()) {
                    world.setBlockAndUpdate(pos, block.get().getStateForPlacement(new BlockPlaceContext(context)));
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
