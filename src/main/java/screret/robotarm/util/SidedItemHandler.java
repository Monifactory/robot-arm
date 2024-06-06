package screret.robotarm.util;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import lombok.AllArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@AllArgsConstructor
public class SidedItemHandler implements IItemTransfer {

    private final IItemTransfer delegate;
    private final Direction side;
    private final BiPredicate<Direction, Integer> slotPredicate;

    @Override
    public void setStackInSlot(int i, @NotNull ItemStack arg) {
        if (slotPredicate.test(side, i)) {
            delegate.setStackInSlot(i, arg);
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (slotPredicate.test(side, slot)) {
            return delegate.insertItem(slot, stack, simulate, notifyChanges);
        }
        return stack;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        if (slotPredicate.test(side, i)) {
            return delegate.getStackInSlot(i);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (slotPredicate.test(side, slot)) {
            return delegate.extractItem(slot, amount, simulate, notifyChanges);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return delegate.getSlotLimit(i);
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack arg) {
        return slotPredicate.test(side, i);
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return new Object();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {

    }
}
