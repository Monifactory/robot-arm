package screret.robotarm.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import screret.robotarm.block.FOUPRailBlock;
import screret.robotarm.client.renderer.block.FOUPCartRenderer;
import screret.robotarm.entity.FOUPCartEntity;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/8/9
 * @implNote FOUPEntityRenderer
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class FOUPEntityRenderer extends EntityRenderer<FOUPCartEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public FOUPEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public ResourceLocation getTextureLocation(FOUPCartEntity entity) {
        return new ResourceLocation("textures/entity/minecart.png");
    }

    @Override
    public boolean shouldRender(FOUPCartEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }

    @Override
    public void render(FOUPCartEntity cart, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(cart, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.pushPose();
        var pos = cart.getAwaitedPos();
        if (pos.isPresent() && cart.getAwaitingData() > 1) {
            float degree = cart.getAwaitingDegree(partialTicks);
            var state = cart.level().getBlockState(pos.get());
            if (state.getBlock() instanceof FOUPRailBlock block) {
                var dir = block.getRailDirection(state);
                poseStack.translate(0, 1, 0);
                if (dir.getAxis() == Direction.Axis.X) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(degree));
                } else if (dir.getAxis() == Direction.Axis.Z) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(degree));
                }
                poseStack.translate(0, -1, 0);
            }
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        poseStack.translate(-0.5, 0, -0.5);

        // render cart
        var cartModel = FOUPCartRenderer.CART.getRotatedModel(Direction.NORTH);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), null,
                cartModel, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.translate(0, -cart.getSlingLength(partialTicks), 0);

        // render sling
        var slingModel = FOUPCartRenderer.SLING.getRotatedModel(Direction.NORTH);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(Sheets.cutoutBlockSheet()), null,
                slingModel, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY);

        // render casket


        poseStack.popPose();

    }

}
