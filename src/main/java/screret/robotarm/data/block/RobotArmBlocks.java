package screret.robotarm.data.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import screret.robotarm.RobotArm;
import screret.robotarm.block.*;
import screret.robotarm.data.model.RobotArmModels;
import screret.robotarm.item.AMHSRailBlockItem;

import java.util.Locale;

import static screret.robotarm.RobotArm.REGISTRATE;

@SuppressWarnings("removal")
public class RobotArmBlocks {

    // Always register all tiers of robot arm & conveyor because GTCEuAPI#isHighTier doesn't work yet.
    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.ULV, GTValues.OpV);

    public static BlockEntry<NormalRailBlock> NORMAL_RAIL = REGISTRATE.block( "normal_rail", NormalRailBlock::new)
            .lang("Normal Rail")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .defaultLoot()
            .addLayer(() -> RenderType::cutoutMipped)
            .onRegister(NormalRailBlock::onRegister)
            .item(AMHSRailBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static BlockEntry<FOUPRailBlock> FOUP_RAIL = REGISTRATE.block( "foup_rail", FOUPRailBlock::new)
            .lang("FOUP Rail")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .defaultLoot()
            .addLayer(() -> RenderType::cutoutMipped)
            .onRegister(FOUPRailBlock::onRegister)
            .item(AMHSRailBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static BlockEntry<FOUPFunnelBlock> FOUP_FUNNEL = REGISTRATE.block( "foup_funnel", FOUPFunnelBlock::new)
            .lang("FOUP Funnel")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.dynamicShape().noOcclusion())
            .blockstate(NonNullBiConsumer.noop())
            .defaultLoot()
            .addLayer(() -> RenderType::cutoutMipped)
            .item(RendererBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    @SuppressWarnings("unchecked")
    public static BlockEntry<ConveyorBeltBlock>[] CONVEYOR_BELTS = new BlockEntry[GTValues.TIER_COUNT];
    @SuppressWarnings("unchecked")
    public static BlockEntry<FilterConveyorBeltBlock>[] FILTER_CONVEYOR_BELTS = new BlockEntry[GTValues.TIER_COUNT];

    static {
        for (int tier : ALL_TIERS) {
            boolean isUlv = tier == GTValues.ULV;
            String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            CONVEYOR_BELTS[tier] = REGISTRATE.block("%s_conveyor_belt".formatted(tierName), (p) -> new ConveyorBeltBlock(p, tier))
                    .lang("%s Conveyor Belt".formatted(GTValues.VNF[tier]))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.noOcclusion())
                    .blockstate(RobotArmModels.conveyorModel(tierName))
                    .loot((loot, block) -> loot.dropOther(block, BuiltInRegistries.ITEM.get(
                            new ResourceLocation(isUlv ? RobotArm.MODID_ULVCOVM : GTCEu.MOD_ID, "%s_conveyor_module".formatted(tierName))))
                    )
                    .addLayer(() -> RenderType::cutoutMipped)
                    .register();
            FILTER_CONVEYOR_BELTS[tier] = REGISTRATE.block("%s_filter_conveyor_belt".formatted(tierName), (p) -> new FilterConveyorBeltBlock(p, tier))
                    .lang("%s Conveyor Belt".formatted(GTValues.VNF[tier]))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.noOcclusion())
                    .blockstate(RobotArmModels.filterConveyorModel(tierName))
                    .loot((loot, block) -> loot.dropOther(block, BuiltInRegistries.ITEM.get(
                            new ResourceLocation(isUlv ? RobotArm.MODID_ULVCOVM : GTCEu.MOD_ID, "%s_conveyor_module".formatted(tierName))))
                    )
                    .addLayer(() -> RenderType::cutoutMipped)
                    .register();
        }
    }

    public static void init() {

    }

}
