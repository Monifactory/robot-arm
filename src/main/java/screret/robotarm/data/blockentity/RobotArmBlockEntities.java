package screret.robotarm.data.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import screret.robotarm.blockentity.ConveyorBeltBlockEntity;
import screret.robotarm.blockentity.FOUPFunnelBlockEntity;
import screret.robotarm.blockentity.FOUPRailBlockEntity;
import screret.robotarm.client.renderer.ConveyorBeltBlockEntityRenderer;
import screret.robotarm.data.block.RobotArmBlocks;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmBlockEntities {

    public static final BlockEntityEntry<FOUPRailBlockEntity> FOUP_RAIL = REGISTRATE
            .blockEntity("foup_rail", FOUPRailBlockEntity::new)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlocks(RobotArmBlocks.FOUP_RAIL)
            .register();

    public static final BlockEntityEntry<FOUPFunnelBlockEntity> FOUP_FUNNEL = REGISTRATE
            .blockEntity("foup_funnel", FOUPFunnelBlockEntity::new)
            .renderer(() -> GTRendererProvider::getOrCreate)
            .validBlocks(RobotArmBlocks.FOUP_FUNNEL)
            .register();

    public static final BlockEntityEntry<ConveyorBeltBlockEntity> CONVEYOR_BELT = REGISTRATE
            .<ConveyorBeltBlockEntity>blockEntity("conveyor_belt",
                    (type, pos, state) -> new ConveyorBeltBlockEntity(type, pos, state, GTValues.LV))
            .renderer(() -> ConveyorBeltBlockEntityRenderer::new)
            .validBlocks(RobotArmBlocks.LV_CONVEYOR_BELT)
            .register();

    public static void init() {

    }
}
