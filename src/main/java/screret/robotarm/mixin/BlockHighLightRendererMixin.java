package screret.robotarm.mixin;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.client.renderer.BlockHighLightRenderer;
import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;
import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Function;

@Mixin(value = BlockHighLightRenderer.class, remap = false)
public abstract class BlockHighLightRendererMixin {

    @Shadow
    private static void drawGridOverlays(PoseStack poseStack, VertexConsumer buffer, BlockHitResult blockHitResult,
                                         Function<Direction, ResourceTexture> test) {
        throw new IllegalStateException("Mixin failed to apply");
    }

    @Inject(method = "renderBlockHighLight",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", remap = false, ordinal = 0),
            cancellable = true)
    private static void robotArm$addBlockHighlightRenderForBlocks(PoseStack poseStack, Camera camera,
                                                                  BlockHitResult target,
                                                                  MultiBufferSource multiBufferSource, float partialTick,
                                                                  CallbackInfo ci,
                                                                  @Local ClientLevel level,
                                                                  @Local LocalPlayer player,
                                                                  @Local ItemStack held,
                                                                  @Local BlockPos blockPos,
                                                                  @Local Set<GTToolType> toolTypes) {
        Block block = level.getBlockState(blockPos).getBlock();
        if (block instanceof IToolGridHighLight gridHighLight) {
            Vec3 pos = camera.getPosition();
            poseStack.pushPose();
            poseStack.translate(-pos.x, -pos.y, -pos.z);
            if (gridHighLight.shouldRenderGrid(player, held, toolTypes)) {
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                RenderSystem.lineWidth(3);
                drawGridOverlays(poseStack, buffer, target, side -> gridHighLight.sideTips(player, toolTypes, side));
            } else {
                var facing = target.getDirection();
                var texture = gridHighLight.sideTips(player, toolTypes, facing);
                if (texture != null) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    poseStack.translate(facing.getStepX() * 0.01, facing.getStepY() * 0.01, facing.getStepZ() * 0.01);
                    RenderUtils.moveToFace(poseStack, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facing);
                    if (facing.getAxis() == Direction.Axis.Y) {
                        RenderUtils.rotateToFace(poseStack, facing, Direction.SOUTH);
                    } else {
                        RenderUtils.rotateToFace(poseStack, facing, null);
                    }
                    poseStack.scale(1f / 16, 1f / 16, 0);
                    poseStack.translate(-8, -8, 0);
                    texture.copy().draw(GuiGraphicsAccessor.create(Minecraft.getInstance(), poseStack,
                            MultiBufferSource.immediate(Tesselator.getInstance().getBuilder())),
                            0, 0, 4, 4, 8, 8);
                    RenderSystem.disableBlend();
                    RenderSystem.enableDepthTest();
                }
            }
            poseStack.popPose();
            ci.cancel();
        }
    }
}
