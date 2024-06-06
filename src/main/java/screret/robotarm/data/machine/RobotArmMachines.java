package screret.robotarm.data.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import screret.robotarm.RobotArm;
import screret.robotarm.client.instance.RobotArmInstance;
import screret.robotarm.client.renderer.RobotArmRenderer;
import screret.robotarm.data.block.RobotArmBlocks;
import screret.robotarm.machine.RobotArmMachine;

import java.util.Locale;
import java.util.function.BiFunction;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmMachines {

    public final static MachineDefinition[] ROBOT_ARM = registerTieredMachines("robot_arm", RobotArmMachine::new,
            (tier, builder) -> builder
                    .blockProp(p -> p.noOcclusion())
                    .rotationState(RotationState.NONE)
                    .renderer(() -> new RobotArmRenderer(tier))
                    .onBlockEntityRegister(attachInstance(() -> (materialManager, blockEntity) ->
                                    new RobotArmInstance(materialManager, blockEntity, tier),
                            false))
                    .hasTESR(true)
                    .shape(Shapes.box(0.2, 0, 0.2, 0.8, 1, 0.8))
                    .register(),
            RobotArmBlocks.ALL_TIERS);

    public static NonNullConsumer<BlockEntityType<BlockEntity>> attachInstance(NonNullSupplier<BiFunction<MaterialManager, BlockEntity, BlockEntityInstance<? super BlockEntity>>> instanceFactory, boolean skipRender) {
        return (blockEntityType) -> {
            if (LDLib.isModLoaded(RobotArm.MODID_FLYWHEEL) && instanceFactory != null && LDLib.isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        OneTimeEventReceiver.addModListener(GTRegistration.REGISTRATE, FMLClientSetupEvent.class,
                                ($) -> InstancedRenderRegistry.configure(blockEntityType)
                                        .factory(instanceFactory.get())
                                        .skipRender(be -> skipRender)
                                        .apply()));
            }
        };
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {

    }

}
