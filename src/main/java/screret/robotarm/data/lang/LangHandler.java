package screret.robotarm.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class LangHandler extends com.gregtechceu.gtceu.data.lang.LangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("robot_arm.gui.arm_console.block_mode.enabled", "Block Mode §eEnabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.block_mode.disabled", "Block Mode §4Disabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.random_mode.enabled", "Random Mode §eEnabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.random_mode.disabled", "Random Mode §4Disabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.reset_mode.enabled", "Reset Mode §eEnabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.reset_mode.disabled", "Reset Mode §4Disabled§r (check compass for more details)");
        provider.add("robot_arm.gui.arm_console.set_as_source", "Set as source");
        provider.add("robot_arm.gui.arm_console.set_as_target", "Set as target");
        provider.add("robot_arm.gui.arm_console.add_op", "Add operation");
        provider.add("robot_arm.gui.arm_console.remove_op", "Remove operation");
        provider.add("robot_arm.gui.arm_console.move_up", "Move up");
        provider.add("robot_arm.gui.arm_console.move_down", "Move down");
        provider.add("robot_arm.gui.arm_console.queue", "Pending queue");
        provider.add("robot_arm.gui.arm_console.reset_queue", "Reset pending queue");
        multiLang(provider, "robot_arm.gui.arm_console.transfer_amount", "Transfer amount", "§e-1 - as many as possible§r", "§e>0 - exact amount§r");
    }
}
