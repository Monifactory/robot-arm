package screret.robotarm.data.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.level.block.Block;
import screret.robotarm.blockentity.ConveyorBeltBlockEntity;
import screret.robotarm.blockentity.FOUPFunnelBlockEntity;
import screret.robotarm.blockentity.FOUPRailBlockEntity;
import screret.robotarm.blockentity.FilterConveyorBeltBlockEntity;
import screret.robotarm.client.renderer.ConveyorBeltBlockEntityRenderer;
import screret.robotarm.data.block.RobotArmBlocks;

import java.util.Arrays;
import java.util.Objects;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmBlockEntities {

    public static final BlockEntityEntry<FOUPRailBlockEntity> FOUP_RAIL = REGISTRATE
            .blockEntity("foup_rail", FOUPRailBlockEntity::new)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlock(RobotArmBlocks.FOUP_RAIL)
            .register();

    public static final BlockEntityEntry<FOUPFunnelBlockEntity> FOUP_FUNNEL = REGISTRATE
            .blockEntity("foup_funnel", FOUPFunnelBlockEntity::new)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlock(RobotArmBlocks.FOUP_FUNNEL)
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<ConveyorBeltBlockEntity> CONVEYOR_BELT = REGISTRATE
            .<ConveyorBeltBlockEntity>blockEntity("conveyor_belt",
                    (type, pos, state) -> new ConveyorBeltBlockEntity(type, pos, state, GTValues.LV))
            .renderer(() -> ConveyorBeltBlockEntityRenderer::new)
            .validBlocks(Arrays.stream(RobotArmBlocks.CONVEYOR_BELTS).filter(Objects::nonNull).toArray(NonNullSupplier[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<FilterConveyorBeltBlockEntity> FILTER_CONVEYOR_BELT = REGISTRATE
            .<FilterConveyorBeltBlockEntity>blockEntity("filter_conveyor_belt",
                    (type, pos, state) -> new FilterConveyorBeltBlockEntity(type, pos, state, GTValues.LV))
            .renderer(() -> ConveyorBeltBlockEntityRenderer::new)
            .validBlocks(Arrays.stream(RobotArmBlocks.FILTER_CONVEYOR_BELTS).filter(Objects::nonNull).toArray(NonNullSupplier[]::new))
            .register();

    public static void init() {

    }
}
