package screret.robotarm;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import screret.robotarm.data.block.RobotArmBlocks;
import screret.robotarm.data.blockentity.RobotArmBlockEntities;
import screret.robotarm.data.creativetab.RobotArmCreativeModeTabs;
import screret.robotarm.data.entity.RobotArmEntities;
import screret.robotarm.data.item.RobotArmItems;
import screret.robotarm.data.lang.LangHandler;
import screret.robotarm.data.machine.RobotArmMachines;
import screret.robotarm.data.model.RobotArmModels;
import screret.robotarm.data.model.RobotArmPartialModels;
import screret.robotarm.data.tag.TagHandler;

@Mod(RobotArm.MOD_ID)
public class RobotArm {
    public static final String MOD_ID = "robot_arm";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(RobotArm.MOD_ID);

    public static final String MODID_ULVCOVM = "ulvcovm",
                                MODID_FLYWHEEL = "flywheel";

    static {
        REGISTRATE.creativeModeTab(() -> RobotArmCreativeModeTabs.CREATIVE_TAB);
    }

    public RobotArm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        MinecraftForge.EVENT_BUS.register(this);

        RobotArmBlocks.init();
        RobotArmBlockEntities.init();
        RobotArmEntities.init();
        RobotArmItems.init();
        REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, TagHandler::itemTags);
        REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE, RobotArmModels::extraModels);
        REGISTRATE.registerEventListeners(modEventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (LDLib.isModLoaded(MODID_FLYWHEEL)) {
                RobotArmPartialModels.init();
            }
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        RobotArmMachines.init();
    }
}
