package screret.robotarm.mixin;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import screret.robotarm.data.block.RobotArmBlocks;
import screret.robotarm.item.behavior.ConveyorBeltBehavior;
import screret.robotarm.item.behavior.RobotArmBehavior;

@Mixin(value = GTItems.class, remap = false)
public class GTItemsMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private static void robotArm$addBehaviors(CallbackInfo ci) {
        GTRegistration.REGISTRATE.addRegisterCallback("lv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.LV)));
        GTRegistration.REGISTRATE.addRegisterCallback("mv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.MV)));
        GTRegistration.REGISTRATE.addRegisterCallback("hv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.HV)));
        GTRegistration.REGISTRATE.addRegisterCallback("ev_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.EV)));
        GTRegistration.REGISTRATE.addRegisterCallback("iv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.IV)));
        GTRegistration.REGISTRATE.addRegisterCallback("luv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.LuV)));
        GTRegistration.REGISTRATE.addRegisterCallback("zpm_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.ZPM)));
        GTRegistration.REGISTRATE.addRegisterCallback("uv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.UV)));
        if (GTCEuAPI.isHighTier()) {
            GTRegistration.REGISTRATE.addRegisterCallback("uhv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.UHV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uev_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.UEV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uiv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.UIV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uxv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.UIV)));
            GTRegistration.REGISTRATE.addRegisterCallback("opv_robot_arm", Registries.ITEM, GTItems.attach(new RobotArmBehavior(GTValues.OpV)));
        }

        GTRegistration.REGISTRATE.addRegisterCallback("lv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.LV)));
        GTRegistration.REGISTRATE.addRegisterCallback("mv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.MV)));
        GTRegistration.REGISTRATE.addRegisterCallback("hv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.HV)));
        GTRegistration.REGISTRATE.addRegisterCallback("ev_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.EV)));
        GTRegistration.REGISTRATE.addRegisterCallback("iv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.IV)));
        GTRegistration.REGISTRATE.addRegisterCallback("luv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.LuV)));
        GTRegistration.REGISTRATE.addRegisterCallback("zpm_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.ZPM)));
        GTRegistration.REGISTRATE.addRegisterCallback("uv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.UV)));
        if (GTCEuAPI.isHighTier()) {
            GTRegistration.REGISTRATE.addRegisterCallback("uhv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.UHV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uev_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.UEV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uiv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.UIV)));
            GTRegistration.REGISTRATE.addRegisterCallback("uxv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.UXV)));
            GTRegistration.REGISTRATE.addRegisterCallback("opv_conveyor_module", Registries.ITEM, GTItems.attach(new ConveyorBeltBehavior(GTValues.OpV)));
        }
    }
}
