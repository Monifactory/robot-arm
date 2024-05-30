package screret.robotarm.client.renderer.block;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import screret.robotarm.RobotArm;
import screret.robotarm.block.FOUPFunnelBlock;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/8/14
 * @implNote FOUPFunnelRenderer
 */
public class FOUPFunnelRenderer extends IModelRenderer {
    public static final FOUPFunnelRenderer INSTANCE = new FOUPFunnelRenderer();

    protected FOUPFunnelRenderer() {
        super(RobotArm.id("block/amhs/funnel/foup_funnel"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        var rotation = Direction.DOWN;
        if (state != null) {
            rotation = state.getValue(FOUPFunnelBlock.FACING);
        }
        return getRotatedModel(rotation).getQuads(state, side, rand);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModeBaker(),
                this::materialMapping,
                switch (facing) {
                    case DOWN -> BlockModelRotation.X0_Y0;
                    case UP -> BlockModelRotation.X180_Y0;
                    case NORTH -> BlockModelRotation.X90_Y0;
                    case SOUTH -> BlockModelRotation.X270_Y0;
                    case WEST -> BlockModelRotation.X90_Y270;
                    case EAST -> BlockModelRotation.X90_Y90;
                },
                modelLocation));
    }
}
