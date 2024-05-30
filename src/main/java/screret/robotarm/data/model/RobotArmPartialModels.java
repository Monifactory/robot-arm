package screret.robotarm.data.model;

import com.jozufozu.flywheel.core.PartialModel;
import screret.robotarm.client.renderer.RobotArmRenderer;

public class RobotArmPartialModels {
    public static final PartialModel AXIS_Y = new PartialModel(RobotArmRenderer.AXIS_Y);
    public static final PartialModel ARM_1 = new PartialModel(RobotArmRenderer.ARM_1);
    public static final PartialModel ARM_2 = new PartialModel(RobotArmRenderer.ARM_2);
    public static final PartialModel ARM_3 = new PartialModel(RobotArmRenderer.ARM_3);

    public static void init() {

    }
}
