package screret.robotarm.data.blockentity;

import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import screret.robotarm.blockentity.FOUPFunnelBlockEntity;
import screret.robotarm.blockentity.FOUPRailBlockEntity;
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

    public static void init() {

    }
}
