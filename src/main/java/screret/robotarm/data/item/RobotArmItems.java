package screret.robotarm.data.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import screret.robotarm.item.behavior.FOUPCartBehavior;
import screret.robotarm.item.behavior.FOUPCasketBehavior;

import static screret.robotarm.RobotArm.REGISTRATE;

public class RobotArmItems {

    public static ItemEntry<ComponentItem> FOUP_CART = REGISTRATE.item("foup_cart", ComponentItem::create)
            .lang("FOUP Cart")
            .onRegister(attach(new FOUPCartBehavior()))
            .model(NonNullBiConsumer.noop())
            .register();

    public static ItemEntry<ComponentItem> FOUP_CASKET = REGISTRATE.item("foup_casket", ComponentItem::create)
            .lang("FOUP Casket")
            .onRegister(attach(new FOUPCasketBehavior()))
            .model(NonNullBiConsumer.noop())
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {

    }
}
