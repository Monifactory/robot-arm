package screret.robotarm.client.instance;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.math.Axis;
import net.minecraft.world.level.block.entity.BlockEntity;
import screret.robotarm.RobotArm;
import screret.robotarm.data.model.RobotArmPartialModels;
import screret.robotarm.machine.RobotArmMachine;

import java.util.Locale;

public class RobotArmInstance extends BlockEntityInstance<BlockEntity> implements DynamicInstance {

    private final ModelData axisY;
    private final ModelData arm1;
    private final ModelData arm2;
    private final ModelData arm3;

    public RobotArmInstance(MaterialManager materialManager, BlockEntity blockEntity, int tier) {
        super(materialManager, blockEntity);
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);

        axisY = createModelData(RobotArmPartialModels.AXIS_Y[tier]);
        arm1 = createModelData(RobotArmPartialModels.ARM_1[tier]);
        arm2 = createModelData(RobotArmPartialModels.ARM_2[tier]);
        arm3 = createModelData(RobotArmPartialModels.ARM_3[tier]);
    }

    private ModelData createModelData(PartialModel partialModel) {
        return materialManager.defaultCutout()
            .material(Materials.TRANSFORMED)
            .getModel(partialModel)
            .createInstance();
    }

    @Override
    public void remove() {
        axisY.delete();
        arm1.delete();
        arm2.delete();
        arm3.delete();
    }

    @Override
    public void beginFrame() {
        if (blockEntity instanceof IMachineBlockEntity machineHolder && machineHolder.getMetaMachine() instanceof RobotArmMachine robotArm) {
            var rotation = robotArm.getArmRotation(AnimationTickHolder.getPartialTicks());
            var axisDegree = rotation.x;
            var arm1Degree = rotation.y;
            var arm2Degree = rotation.z;
            var arm3Degree = rotation.w;
            axisY.loadIdentity()
                .translate(getInstancePosition())
                .translate(0.5, 0, 0.5)
                .multiply(Axis.YP.rotationDegrees(axisDegree))
                .translate(-0.5, 0, -0.5);
            arm1.loadIdentity()
                .translate(getInstancePosition())
                .translate(0.5, 0, 0.5)
                .multiply(Axis.YP.rotationDegrees(axisDegree))
                .translate(-0.5, 0, -0.5)
                .translate(0.5f, 5 / 16f, 1f)
                .multiply(Axis.XP.rotationDegrees(arm1Degree))
                .translate(-0.5f, -5 / 16f, -1f);
            arm2.loadIdentity()
                .translate(getInstancePosition())
                .translate(0.5, 0, 0.5)
                .multiply(Axis.YP.rotationDegrees(axisDegree))
                .translate(-0.5, 0, -0.5)
                .translate(0.5f, 5 / 16f, 1f)
                .multiply(Axis.XP.rotationDegrees(arm1Degree))
                .translate(-0.5f, -5 / 16f, -1f)
                .translate(0.5f, 5 / 16f, 0)
                .multiply(Axis.XP.rotationDegrees(arm2Degree))
                .translate(-0.5f, -5 / 16f, 0);
            arm3.loadIdentity()
                .translate(getInstancePosition())
                .translate(0.5, 0, 0.5)
                .multiply(Axis.YP.rotationDegrees(axisDegree))
                .translate(-0.5, 0, -0.5)
                .translate(0.5f, 5 / 16f, 1f)
                .multiply(Axis.XP.rotationDegrees(arm1Degree))
                .translate(-0.5f, -5 / 16f, -1f)
                .translate(0.5f, 5 / 16f, 0)
                .multiply(Axis.XP.rotationDegrees(arm2Degree))
                .translate(-0.5f, -5 / 16f, 0)
                .translate(0.5f, 1 + 5 / 16f, 0)
                .multiply(Axis.XP.rotationDegrees(arm3Degree))
                .translate(-0.5f, -(1 + 5 / 16f), 0);
        }
    }

    @Override
    public void updateLight() {
        relight(getWorldPosition(), axisY, arm1, arm2, arm3);
    }
}
