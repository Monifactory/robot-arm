package screret.robotarm.data.tag;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.jmoiron.ulvcovm.data.covers.CoverItems;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import screret.robotarm.util.RobotArmTags;

public class TagHandler {

    public static void itemTags(RegistrateItemTagsProvider provider) {
        provider.addTag(RobotArmTags.CONVEYORS)
                .add(GTItems.CONVEYOR_MODULE_LV.asItem())
                .add(GTItems.CONVEYOR_MODULE_MV.asItem())
                .add(GTItems.CONVEYOR_MODULE_HV.asItem())
                .add(GTItems.CONVEYOR_MODULE_EV.asItem())
                .add(GTItems.CONVEYOR_MODULE_IV.asItem())
                .add(GTItems.CONVEYOR_MODULE_LuV.asItem())
                .add(GTItems.CONVEYOR_MODULE_ZPM.asItem())
                .add(GTItems.CONVEYOR_MODULE_UV.asItem())
                .addOptional(GTItems.CONVEYOR_MODULE_UHV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UEV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UIV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_UXV.getId())
                .addOptional(GTItems.CONVEYOR_MODULE_OpV.getId())
                .addOptional(CoverItems.CONVEYOR_MODULE_ULV.getId());
    }
}
