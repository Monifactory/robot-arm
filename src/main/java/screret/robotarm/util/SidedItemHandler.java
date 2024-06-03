package screret.robotarm.util;

import lombok.AllArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@AllArgsConstructor
public class SidedItemHandler implements IItemHandlerModifiable {

    private final IItemHandlerModifiable delegate;
    private final Direction side;
    private final BiPredicate<Direction, Integer> slotPredicate;

    @Override
    public void setStackInSlot(int i, @NotNull ItemStack arg) {
        if (slotPredicate.test(side, i)) {
            delegate.setStackInSlot(i, arg);
        }
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

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack arg, boolean bl) {
        if (slotPredicate.test(side, i)) {
            return delegate.insertItem(i, arg, bl);
        }
        return arg;
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int j, boolean bl) {
        if (slotPredicate.test(side, i)) {
            return delegate.extractItem(i, j, bl);
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
}
