package screret.robotarm.mixin;

import com.gregtechceu.gtceu.api.GTValues;
import com.jmoiron.ulvcovm.data.covers.CoverItems;
import com.jmoiron.ulvcovm.registry.UCMRegistries;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import screret.robotarm.item.behavior.RobotArmBehavior;

@Mixin(value = CoverItems.class, remap = false)
public class UCMCoverItemsMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private static void robotArm$addBehaviors(CallbackInfo ci) {
        UCMRegistries.REGISTRATE.addRegisterCallback("ulv_robot_arm", Registries.ITEM, CoverItems.attach(new RobotArmBehavior(GTValues.ULV)));
    }

}
