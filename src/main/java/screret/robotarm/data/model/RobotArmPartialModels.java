package screret.robotarm.data.model;

import com.gregtechceu.gtceu.api.GTValues;
import com.jozufozu.flywheel.core.PartialModel;
import screret.robotarm.RobotArm;
import screret.robotarm.client.renderer.RobotArmRenderer;
import screret.robotarm.data.block.RobotArmBlocks;

import java.util.Locale;

public class RobotArmPartialModels {
    public static final PartialModel[] AXIS_Y = new PartialModel[GTValues.TIER_COUNT];
    public static final PartialModel[] ARM_1 = new PartialModel[GTValues.TIER_COUNT];
    public static final PartialModel[] ARM_2 = new PartialModel[GTValues.TIER_COUNT];
    public static final PartialModel[] ARM_3 = new PartialModel[GTValues.TIER_COUNT];

    public static void init() {
        for (int tier : RobotArmBlocks.ALL_TIERS) {
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);

            AXIS_Y[tier] = new PartialModel(RobotArm.id("block/machine/robot_arm/" + tierName + "/axis_y"));
            ARM_1[tier] = new PartialModel(RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_1"));
            ARM_2[tier] = new PartialModel(RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_2"));
            ARM_3[tier] = new PartialModel(RobotArm.id("block/machine/robot_arm/" + tierName + "/arm_3"));
        }
    }
}
