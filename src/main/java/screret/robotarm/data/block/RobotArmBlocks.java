package screret.robotarm.data.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.FOUPFunnelBlock;
import screret.robotarm.block.FOUPRailBlock;
import screret.robotarm.block.NormalRailBlock;
import screret.robotarm.item.AMHSRailBlockItem;

import static screret.robotarm.RobotArm.REGISTRATE;

@SuppressWarnings("removal")
public class RobotArmBlocks {

    public static BlockEntry<NormalRailBlock> NORMAL_RAIL = REGISTRATE.block( "normal_rail", NormalRailBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
            .addLayer(() -> RenderType::cutoutMipped)
            .onRegister(NormalRailBlock::onRegister)
            .item(AMHSRailBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static BlockEntry<FOUPRailBlock> FOUP_RAIL = REGISTRATE.block( "foup_rail", FOUPRailBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
            .addLayer(() -> RenderType::cutoutMipped)
            .onRegister(FOUPRailBlock::onRegister)
            .item(AMHSRailBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static BlockEntry<FOUPFunnelBlock> FOUP_FUNNEL = REGISTRATE.block( "foup_funnel", FOUPFunnelBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
            .addLayer(() -> RenderType::cutoutMipped)
            .item(RendererBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static BlockEntry<ConveyorBeltBlock> LV_CONVEYOR_BELT = REGISTRATE
            .block( "lv_conveyor_belt",(p) -> new ConveyorBeltBlock(p, GTValues.LV))
            .lang("LV Conveyor Belt")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .loot((loot, block) -> loot.dropOther(block, GTItems.CONVEYOR_MODULE_LV.asItem()))
            .addLayer(() -> RenderType::cutoutMipped)
            .register();

    public static void init() {

    }
}
