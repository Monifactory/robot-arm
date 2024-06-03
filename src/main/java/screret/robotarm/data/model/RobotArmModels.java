package screret.robotarm.data.model;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import screret.robotarm.RobotArm;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.FilterConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorSlope;

public class RobotArmModels {

    public static NonNullBiConsumer<DataGenContext<Block, ConveyorBeltBlock>, RegistrateBlockstateProvider> conveyorModel(final String tierName) {
        return (ctx, prov) -> {
            ModelFile slopeNoneOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_flat_on"), prov.models().existingFileHelper);
            ModelFile slopeUpOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_up_on"), prov.models().existingFileHelper);
            ModelFile slopeDownOnParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_down_on"), prov.models().existingFileHelper);
            ModelFile slopeNoneOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_flat_off"), prov.models().existingFileHelper);
            ModelFile slopeUpOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_up_off"), prov.models().existingFileHelper);
            ModelFile slopeDownOffParent = new ModelFile.ExistingModelFile(RobotArm.id("block/conveyor_belt_down_off"), prov.models().existingFileHelper);

            ModelFile slopeNoneOn = prov.models().getBuilder("conveyor_" + tierName + "_flat_on")
                    .parent(slopeNoneOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));
            ModelFile slopeUpOn = prov.models().getBuilder("conveyor_" + tierName + "_up_on")
                    .parent(slopeUpOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));
            ModelFile slopeDownOn = prov.models().getBuilder("conveyor_" + tierName + "_down_on")
                    .parent(slopeDownOnParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side"))
                    .texture("particle", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"));

            ModelFile slopeNoneOff = prov.models().getBuilder("conveyor_" + tierName + "_flat_off")
                    .parent(slopeNoneOffParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side_off"))
                    .texture("particle", "#top");
            ModelFile slopeUpOff = prov.models().getBuilder("conveyor_" + tierName + "_up_off")
                    .parent(slopeUpOffParent)
                    .texture("top", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/top_off"))
                    .texture("side", prov.modLoc(ModelProvider.BLOCK_FOLDER + "/conveyor_belt/" + tierName + "/side_off"))
                    .texture("particle", "#top");
            ModelFile slopeDownOff = prov.models().getBuilder("conveyor_" + tierName + "_down_off")
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

            ModelFile filterOn = prov.models().getBuilder("filter_conveyor_" + tierName + "_on")
                    .parent(filterOnParent);
            ModelFile filterOff = prov.models().getBuilder("filter_conveyor_" + tierName + "_off")
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
}
