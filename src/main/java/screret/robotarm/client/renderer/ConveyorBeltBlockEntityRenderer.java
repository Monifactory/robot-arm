package screret.robotarm.client.renderer;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.joml.Vector3d;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorOutputMode;
import screret.robotarm.block.properties.ConveyorSlope;
import screret.robotarm.blockentity.ConveyorBeltBlockEntity;
import screret.robotarm.blockentity.FilterConveyorBeltBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConveyorBeltBlockEntityRenderer<T extends ConveyorBeltBlockEntity> implements BlockEntityRenderer<T> {

    private final ItemRenderer itemRenderer;

    public ConveyorBeltBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer =  context.getItemRenderer();
    }

    @Override
    public void render(ConveyorBeltBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        IItemTransfer beltInv = blockEntity.items;

        matrices.pushPose();

        int forwardSlots = blockEntity.getSize();
        int forwardSlotsSize = 3;

        for (int i = 0; i < forwardSlots; i++) {
            if (!beltInv.getStackInSlot(i).isEmpty()) {
                float cooldownOffset = getTransferCooldownOffset(blockEntity, i);
                float offset = (i + cooldownOffset) / (float) forwardSlotsSize;

                float sidewaysOffset = (float) blockEntity.transferSidewaysOffset[i] / 100f;

                if (blockEntity instanceof FilterConveyorBeltBlockEntity filterConveyorBeltBlockEntity) {
                    if (i == 2 && filterConveyorBeltBlockEntity.outputMode != ConveyorOutputMode.LEFT_FRONT) {
                        renderBeltItem(blockEntity, matrices, vertexConsumers, light, overlay, beltInv.getStackInSlot(2), 0.5f, -getTransferCooldownOffset(blockEntity, 2) / 2);
                        continue;
                    }
                    if (i == 3 && filterConveyorBeltBlockEntity.outputMode != ConveyorOutputMode.RIGHT_FRONT) {
                        renderBeltItem(blockEntity, matrices, vertexConsumers, light, overlay, beltInv.getStackInSlot(3), 0.5f, getTransferCooldownOffset(blockEntity, 3) / 2);
                        continue;
                    }
                }

                matrices.pushPose();
                renderBeltItem(blockEntity, matrices, vertexConsumers, light, overlay, beltInv.getStackInSlot(i), Math.min(offset, 1f), sidewaysOffset);
                matrices.popPose();
            }
        }

        matrices.popPose();
    }

    public float getTransferCooldownOffset(ConveyorBeltBlockEntity blockEntity, int slot) {
        return 1f - ((float) blockEntity.transferCooldownCounter[slot] / (float) blockEntity.transferCooldown);
    }

    public void renderBeltItem(ConveyorBeltBlockEntity blockEntity, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, ItemStack itemStack, float offset, float sidewaysOffset) {
        Direction facing = blockEntity.getBlockState().getValue(ConveyorBeltBlock.FACING);
        ConveyorSlope slope = blockEntity.getBlockState().getValue(ConveyorBeltBlock.SLOPE);

        // Move the item
        Vector3d translated = new Vector3d(0, 0, 0);

        boolean hasDepth = Objects.requireNonNull(itemRenderer.getItemModelShaper().getItemModel(itemStack)).isGui3d();

        float height = hasDepth ? 0.31f : 0.39f;
        float itemScale = 0.7f;

        Direction slopeDir = null;

        if (slope == ConveyorSlope.DOWN) {
            height += 1f - offset;

            slopeDir = facing;
        } else if (slope == ConveyorSlope.UP) {
            height += offset;

            slopeDir = facing.getOpposite();
        }

        if (facing == Direction.NORTH) {
            translated = new Vector3d(0.5f + sidewaysOffset, height, 1 - offset);
        } else if (facing == Direction.EAST) {
            translated = new Vector3d(offset, height, 0.5f + sidewaysOffset);
        } else if (facing == Direction.SOUTH) {
            translated = new Vector3d(0.5f - sidewaysOffset, height, offset);
        } else if (facing == Direction.WEST) {
            translated = new Vector3d(1 - offset, height, 0.5f - sidewaysOffset);
        }

        matrices.translate(translated.x, translated.y, translated.z);

        if (slopeDir == Direction.NORTH) {
            matrices.mulPose(Axis.XP.rotationDegrees(-45));
        } else if (slopeDir == Direction.SOUTH) {
            matrices.mulPose(Axis.XP.rotationDegrees(45));
        } else if (slopeDir == Direction.EAST) {
            matrices.mulPose(Axis.ZP.rotationDegrees(-45));
        } else if (slopeDir == Direction.WEST) {
            matrices.mulPose(Axis.ZP.rotationDegrees(45));
        }

        if (!hasDepth) {
            matrices.translate(0f, 0f, -0.08f);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            matrices.scale(itemScale, itemScale, itemScale);
        }

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, blockEntity.getLevel(), 0);

        if (!hasDepth) {
            matrices.scale(1 / itemScale, 1 / itemScale, 1 / itemScale);
            matrices.mulPose(Axis.XP.rotationDegrees(-90));
            matrices.translate(0f, 0f, 0.08f);
        }

        if (slopeDir == Direction.NORTH) {
            matrices.mulPose(Axis.XP.rotationDegrees(45));
        } else if (slopeDir == Direction.SOUTH) {
            matrices.mulPose(Axis.XP.rotationDegrees(-45));
        } else if (slopeDir == Direction.EAST) {
            matrices.mulPose(Axis.ZP.rotationDegrees(45));
        } else if (slopeDir == Direction.WEST) {
            matrices.mulPose(Axis.ZP.rotationDegrees(-45));
        }

        matrices.translate(-translated.x, -translated.y, -translated.z);
    }
}
