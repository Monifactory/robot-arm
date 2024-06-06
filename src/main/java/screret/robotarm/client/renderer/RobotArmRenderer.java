package screret.robotarm.client.renderer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.jozufozu.flywheel.backend.Backend;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import screret.robotarm.RobotArm;
import screret.robotarm.machine.RobotArmMachine;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RobotArmRenderer extends IModelRenderer {
    public final static ResourceLocation FULL = RobotArm.id("block/machine/robot_arm/robot_arm");
    public final static ResourceLocation BASE = RobotArm.id("block/machine/robot_arm/base");
    public final static ResourceLocation AXIS_Y = RobotArm.id("block/machine/robot_arm/axis_y");
    public final static ResourceLocation ARM_1 = RobotArm.id("block/machine/robot_arm/arm_1");
    public final static ResourceLocation ARM_2 = RobotArm.id("block/machine/robot_arm/arm_2");
    public final static ResourceLocation ARM_3 = RobotArm.id("block/machine/robot_arm/arm_3");
    private final static RandomSource RANDOM = RandomSource.create();

    @OnlyIn(Dist.CLIENT)
    protected Map<ResourceLocation, BakedModel> models;

    private final int tier;

    private final ResourceLocation axis, arm1, arm2, arm3;

    public RobotArmRenderer(int tier) {
        super(BASE);
        this.tier = tier;
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);

        axis = RobotArm.id("block/machine/robot_arm/" + tierName + "/axis_y");
        arm1 = RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_1");
        arm2 = RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_2");
        arm3 = RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_3");

        if (LDLib.isClient()) {
            models = new ConcurrentHashMap<>();
        }
    }

    @Nullable
    @Override
    protected BakedModel getItemBakedModel() {
        if (itemModel == null) {
            var model = ModelFactory.getUnBakedModel(FULL);
            if (model instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                // fabric doesn't help us to fix vanilla bakery, so we have to do it ourselves
                model = ModelFactory.ITEM_MODEL_GENERATOR.generateBlockModel(this::materialMapping, blockModel);
            }
            itemModel = model.bake(
                    ModelFactory.getModeBaker(),
                    this::materialMapping,
                    BlockModelRotation.X0_Y0,
                    modelLocation);
        }
        return itemModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(axis);
        registry.accept(arm1);
        registry.accept(arm2);
        registry.accept(arm3);
    }

    @OnlyIn(Dist.CLIENT)
    protected BakedModel getBakedModel(ResourceLocation location) {
        return models.computeIfAbsent(location, l -> ModelFactory.getUnBakedModel(l).bake(
            ModelFactory.getModeBaker(),
            this::materialMapping,
            BlockModelRotation.X0_Y0,
            l));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderBlockModel(PoseStack poseStack, VertexConsumer buffer, int light, int combinedOverlay, BakedModel bakedmodel) {
        var brd = Minecraft.getInstance().getBlockRenderer();
        brd.getModelRenderer().renderModel(poseStack.last(), buffer, null, bakedmodel, 1, 1, 1, light, combinedOverlay, ModelData.EMPTY, null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        var useInstance = LDLib.isModLoaded(RobotArm.MODID_FLYWHEEL) && Backend.canUseInstancing(blockEntity.getLevel());
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity && machineBlockEntity.getMetaMachine() instanceof RobotArmMachine robotArm) {
            var rotation = robotArm.getArmRotation(partialTicks);
            renderRobotArm(useInstance, stack, bufferSource, combinedLight, combinedOverlay,
                    rotation.x(), rotation.y(), rotation.z(), rotation.w(), robotArm.getClampRotation(partialTicks),
                    robotArm.getTransferredItems());
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderRobotArm(boolean useInstance, PoseStack stack, MultiBufferSource bufferSource,
                                  int combinedLight, int combinedOverlay,
                                  float axisDegree, float arm1Degree, float arm2Degree, float arm3Degree, float clampDegree,
                                  @NotNull ItemStack @Nullable [] heldStacks) {
        var buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        var axis = getBakedModel(this.axis);
        var arm1 = getBakedModel(this.arm1);
        var arm2 = getBakedModel(this.arm2);
        var arm3 = getBakedModel(this.arm3);

        stack.pushPose();
        // axis
        stack.translate(0.5, 0, 0.5);
        stack.mulPose(Axis.YP.rotationDegrees(axisDegree));
        stack.translate(-0.5, 0, -0.5);
        if (!useInstance) renderBlockModel(stack, buffer, combinedLight, combinedOverlay, axis);

        // arm1
        stack.translate(0.5f, 5 / 16f, 1f);
        stack.mulPose(Axis.XP.rotationDegrees(arm1Degree));
        stack.translate(-0.5f, -5 / 16f, -1f);
        if (!useInstance) renderBlockModel(stack, buffer, combinedLight,combinedOverlay, arm1);

        // arm2
        stack.translate(0.5f, 5 / 16f, 0);
        stack.mulPose(Axis.XP.rotationDegrees(arm2Degree));
        stack.translate(-0.5f, -5 / 16f, 0);
        if (!useInstance) renderBlockModel(stack, buffer, combinedLight, combinedOverlay, arm2);

        // arm3
        stack.translate(0.5f, 1 + 5 / 16f, 0);
        stack.mulPose(Axis.XP.rotationDegrees(arm3Degree));
        stack.translate(-0.5f, -(1 + 5 / 16f), 0);
        if (!useInstance) renderBlockModel(stack, buffer, combinedLight, combinedOverlay, arm3);

        // clamp
        // TODO clamp (the "hand" part) rotation
//        stack.translate(0.5f, 1 + 5 / 16f, 1f);
//        stack.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(clampDegree), 1, 0, 0));
//        stack.translate(-0.5f, -(1 + 5 / 16f), -1f);
//        if (!useInstance) renderBlockModel(stack, buffer, combinedLight, combinedOverlay, clamp);

        // held items
        if (heldStacks != null) {
            for (ItemStack itemStack : heldStacks) {
                var itemRenderer = Minecraft.getInstance().getItemRenderer();
                var bakedmodel = itemRenderer.getModel(itemStack, Minecraft.getInstance().level, null, Item.getId(itemStack.getItem()) + itemStack.getDamageValue());
                var isGui3d = bakedmodel.isGui3d();
                var renderAmount = getRenderAmount(itemStack);
                RANDOM.setSeed(itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue());

                for (int i = 0; i < renderAmount; i++) {
                    stack.pushPose();
                    stack.translate(0.5f, 5 / 16f, 1 + 3 / 16f);
                    if (isGui3d) { // e.g. blocks
                        float rx = 0, ry = 0, rz = 0;
                        if (renderAmount > 1) {
                            rx = (RANDOM.nextFloat() * 2.0f - 1.0f) * 0.06f;
                            ry = (RANDOM.nextFloat() * 2.0f - 1.0f) * 0.06f;
                            rz = (RANDOM.nextFloat() * 2.0f - 1.0f) * 0.06f;
                        }
                        stack.translate(rx, 0.84f + ry, rz);
                    } else { // e.g. ingots
                        stack.translate(0, i / 16f - (renderAmount - 1) / 32f, 0);
                        stack.translate(0.5D, 0.5d, 0.5D);
                        stack.mulPose(new Quaternionf().rotateAxis(Mth.HALF_PI, 1, 0, 0));
                        stack.translate(-0.5f, -0.5f, -0.5f);
                    }
                    itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, stack, bufferSource, combinedLight, combinedOverlay, bakedmodel);
                    stack.popPose();
                }
                break;
            }
        }

        stack.popPose();
    }

    protected int getRenderAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 32) {
            i = 3;
        } else if (stack.getCount() > 16) {
            i = 2;
        }
        return i;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            models.clear();
        }
    }
}
