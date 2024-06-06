package screret.robotarm.client.renderer.block;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import screret.robotarm.RobotArm;
import screret.robotarm.block.AMHSRailBlock;
import screret.robotarm.blockentity.FOUPRailBlockEntity;

/**
 * @author KilaBash
 * @date 2023/8/13
 * @implNote FOUPRailBlockRenderer
 */
public class FOUPRailBlockRenderer extends IModelRenderer {

    public FOUPRailBlockRenderer() {
        super(RobotArm.id("block/amhs/foup/straight"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof FOUPRailBlockEntity rail && rail.getBlockState().getBlock() instanceof AMHSRailBlock railBlock) {
            poseStack.pushPose();

            var cart = rail.getAwaitedCart();
            if (cart != null) {
                var dir = railBlock.getRailDirection(blockEntity.getBlockState());
                var degree = cart.getAwaitingDegree(partialTicks);
                poseStack.translate(0.5, 1, 0.5);
                if (dir.getAxis() == Direction.Axis.X) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(degree));
                } else if (dir.getAxis() == Direction.Axis.Z) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(degree));
                }
                poseStack.translate(-0.5, -1, -0.5);
            }

            var bakedmodel = getRotatedModel(railBlock.getRailDirection(blockEntity.getBlockState()).getOpposite());
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer.getBuffer(Sheets.cutoutBlockSheet()),
                    blockEntity.getBlockState(),
                    bakedmodel,
                    1,
                    1,
                    1,
                    combinedLight,
                    combinedOverlay);
            poseStack.popPose();
        }
    }
}
