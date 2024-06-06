package screret.robotarm.item.behavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import screret.robotarm.client.renderer.block.FOUPCartRenderer;

import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/8/10
 * @implNote FOUPCasketBehavior
 */
public class FOUPCasketBehavior implements ICustomRenderer, IInteractionItem {
    final IRenderer renderer = FOUPCartRenderer.CASKET;

    public FOUPCasketBehavior() {
    }

    public static Optional<FOUPCasketBehavior> getBehavior(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getItem() instanceof ComponentItem item && !item.getComponents().isEmpty() && item.getComponents().get(0) instanceof FOUPCasketBehavior behavior ? behavior : null);
    }

    public ItemStack[] getStoredItems(ItemStack itemStack) {
        var list = itemStack.getOrCreateTag().getList("items", Tag.TAG_COMPOUND);
        var items = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i++) {
            items[i] = ItemStack.of(list.getCompound(i));
        }
        return items;
    }

    public void setStoredItems(ItemStack itemStack, ItemStack[] items) {
        var list = new ListTag();
        for (var item : items) {
            list.add(item.save(new CompoundTag()));
        }
        if (list.isEmpty()) {
            itemStack.getOrCreateTag().remove("items");
        } else {
            itemStack.getOrCreateTag().put("items", list);
        }
    }

    @NotNull
    @Override
    public IRenderer getRenderer() {
        return renderer;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return IInteractionItem.super.useOn(context);
    }
}
