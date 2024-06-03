package screret.robotarm.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.HitResult;
import screret.robotarm.data.machine.RobotArmMachines;

public class RobotArmBehavior implements IInteractionItem {
    private final int tier;

    public RobotArmBehavior(int tier) {
        this.tier = tier;
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
                    world.setBlockAndUpdate(pos, RobotArmMachines.ROBOT_ARM[tier].defaultBlockState());
                }
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
