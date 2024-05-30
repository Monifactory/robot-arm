package screret.robotarm.data.creativetab;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;
import screret.robotarm.RobotArm;
import screret.robotarm.data.machine.RobotArmMachines;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> CREATIVE_TAB = REGISTRATE.defaultCreativeTab("creative_tab",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("creative_tab", REGISTRATE))
                    .icon(() -> RobotArmMachines.ROBOT_ARM.asStack())
                    .title(REGISTRATE.addLang("itemGroup", RobotArm.id("creative_tab"), "Robot Arm"))
                    .build())
            .register();
}
