package screret.robotarm.item.behavior;

import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import screret.robotarm.block.AMHSRailBlock;
import screret.robotarm.client.renderer.block.FOUPCartRenderer;
import screret.robotarm.entity.FOUPCartEntity;

/**
 * @author KilaBash
 * @date 2023/8/10
 * @implNote FOUPCartBehavior
 */
public class FOUPCartBehavior implements ICustomRenderer, IInteractionItem {
    final IRenderer renderer = FOUPCartRenderer.INSTANCE;

    public FOUPCartBehavior() {
    }

    @NotNull
    @Override
    public IRenderer getRenderer() {
        return renderer;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof AMHSRailBlock railBlock) {
            if (context.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.addFreshEntity(FOUPCartEntity.create(serverLevel, pos, railBlock.getRailDirection(state)));
            }
            context.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(true);
        }
        return IInteractionItem.super.useOn(context);
    }
}
