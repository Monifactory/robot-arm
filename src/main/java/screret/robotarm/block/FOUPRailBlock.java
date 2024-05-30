package screret.robotarm.block;

import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.client.renderer.block.FOUPRailBlockRenderer;
import screret.robotarm.data.blockentity.RobotArmBlockEntities;
import screret.robotarm.pipenet.amhs.AMHSRailType;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/8/9
 * @implNote FOUPRailBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FOUPRailBlock extends AMHSRailBlock implements EntityBlock {
    public FOUPRailBlock(Properties properties) {
        super(properties, AMHSRailType.FOUP);
    }

    @Override
    public IRenderer createRenderer() {
        return new FOUPRailBlockRenderer();
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RobotArmBlockEntities.FOUP_RAIL.get().create(pos, state);
    }

}
