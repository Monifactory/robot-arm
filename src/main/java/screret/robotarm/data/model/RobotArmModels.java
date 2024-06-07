package screret.robotarm.data.model;

import com.gregtechceu.gtceu.api.GTValues;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import screret.robotarm.RobotArm;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.FilterConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorSlope;
import screret.robotarm.client.renderer.RobotArmRenderer;
import screret.robotarm.data.block.RobotArmBlocks;

import java.util.Locale;

public class RobotArmModels {

    public static NonNullBiConsumer<DataGenContext<Block, ConveyorBeltBlock>, RegistrateBlockstateProvider> conveyorModel(final String tierName) {
        return (ctx, prov) -> {
            ModelFile slopeNoneOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_flat_on"), prov.models().existingFileHelper);
            ModelFile slopeUpOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_up_on"), prov.models().existingFileHelper);
            ModelFile slopeDownOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_down_on"), prov.models().existingFileHelper);
            ModelFile slopeNoneOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_flat_off"), prov.models().existingFileHelper);
            ModelFile slopeUpOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_up_off"), prov.models().existingFileHelper);
            ModelFile slopeDownOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_down_off"), prov.models().existingFileHelper);

            ModelFile slopeNoneOn = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/conveyor/" + tierName + "/flat_on")
                    .parent(slopeNoneOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));
            ModelFile slopeUpOn = prov.models().getBuilder("conveyor/" + tierName + "/up_on")
                    .parent(slopeUpOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));
            ModelFile slopeDownOn = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/conveyor/" + tierName + "/down_on")
                    .parent(slopeDownOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));

            ModelFile slopeNoneOff = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/conveyor/" + tierName + "/flat_off")
                    .parent(slopeNoneOffParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side_off"))
                    .texture("particle", "#top");
            ModelFile slopeUpOff = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/conveyor/" + tierName + "/up_off")
                    .parent(slopeUpOffParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side_off"))
                    .texture("particle", "#top");
            ModelFile slopeDownOff = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/conveyor/" + tierName + "/down_off")
                    .parent(slopeDownOffParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side_off"))
                    .texture("particle", "#top");

            prov.getVariantBuilder(ctx.getEntry())
                    .forAllStates(state -> {
                        ConveyorSlope slope = state.getValue(ConveyorBeltBlock.SLOPE);
                        boolean enabled = state.getValue(ConveyorBeltBlock.ENABLED);
                        Direction facing = state.getValue(ConveyorBeltBlock.FACING);
                        int yRot = (int) facing.toYRot();

                        return ConfiguredModel.builder()
                                .modelFile(switch (slope) {
                                    case NONE -> enabled ? slopeNoneOn : slopeNoneOff;
                                    case UP -> enabled ? slopeUpOn : slopeUpOff;
                                    case DOWN -> enabled ? slopeDownOn : slopeDownOff;
                                })
                                .rotationY(yRot)
                                .build();
                    });
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, FilterConveyorBeltBlock>, RegistrateBlockstateProvider> filterConveyorModel(final String tierName) {
        return (ctx, prov) -> {
            ModelFile filterOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/filter_conveyor_belt_on"), prov.models().existingFileHelper);
            ModelFile filterOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/filter_conveyor_belt_off"), prov.models().existingFileHelper);

            ModelFile filterOn = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/filter_conveyor/" + tierName + "/on")
                    .parent(filterOnParent);
            ModelFile filterOff = prov.models().getBuilder(ModelProvider.BLOCK_FOLDER + "/filter_conveyor/" + tierName + "/off")
                    .parent(filterOffParent);

            prov.getVariantBuilder(ctx.getEntry())
                    .forAllStates(state -> {
                        boolean enabled = state.getValue(ConveyorBeltBlock.ENABLED);
                        Direction facing = state.getValue(ConveyorBeltBlock.FACING);
                        int yRot = (int) facing.toYRot();

                        return ConfiguredModel.builder()
                                .modelFile(enabled ? filterOn : filterOff)
                                .rotationY(yRot)
                                .build();
                    });
        };
    }

    public static void extraModels(RegistrateBlockstateProvider provider) {
        ModelFile axisParent = new ModelFile.ExistingModelFile(RobotArmRenderer.AXIS_Y, provider.models().existingFileHelper);
        ModelFile arm1Parent = new ModelFile.ExistingModelFile(RobotArmRenderer.ARM_1, provider.models().existingFileHelper);
        ModelFile arm2Parent = new ModelFile.ExistingModelFile(RobotArmRenderer.ARM_2, provider.models().existingFileHelper);
        ModelFile arm3Parent = new ModelFile.ExistingModelFile(RobotArmRenderer.ARM_3, provider.models().existingFileHelper);

        for (int tier : RobotArmBlocks.ALL_TIERS) {
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            ResourceLocation texture = RobotArm.id("block/robot_arm/" + tierName);

            provider.models().getBuilder("block/machine/robot_arm/" + tierName + "/axis_y")
                    .texture("0", texture)
                    .parent(axisParent);
            provider.models().getBuilder("block/machine/robot_arm/" + tierName + "/arm_1")
                    .texture("0", texture)
                    .parent(arm1Parent);
            provider.models().getBuilder("block/machine/robot_arm/" + tierName + "/arm_2")
                    .texture("0", texture)
                    .parent(arm2Parent);
            provider.models().getBuilder("block/machine/robot_arm/" + tierName + "/arm_3")
                    .texture("0", texture)
                    .parent(arm3Parent);
        }
    }
}
