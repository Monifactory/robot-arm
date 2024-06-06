package screret.robotarm.pipenet.amhs.op;

import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import screret.robotarm.entity.FOUPCartEntity;
import screret.robotarm.pipenet.amhs.AMHSRailNet;

import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/8/13
 * @implNote FOUPOp
 */
@Accessors(chain = true)
public abstract class FOUPOp implements ITagSerializable<CompoundTag> {
    public final FOUPCartEntity cart;
    @Getter
    protected boolean isRunning, isDone;
    @Setter
    private int preDelay, postDelay;
    // runtime
    @Getter
    private boolean isRemoved;
    private int delay;

    public FOUPOp(FOUPCartEntity cart) {
        this.cart = cart;
    }

    public FOUPOp(FOUPCartEntity cart, CompoundTag tag) {
        this.cart = cart;
    }

    public static Optional<FOUPOp> loadFromNBT(FOUPCartEntity cart, CompoundTag tag) {
        try {
            var op = OP.valueOf(tag.getString("type")).create(cart);
            return Optional.of(op);
        } catch (Exception e) {
            GTCEu.LOGGER.error("Failed to load OP from NBT {}", tag, e);
            return Optional.empty();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("type", getType().name());
        tag.putBoolean("isRunning", isRunning);
        tag.putBoolean("isDone", isDone);
        tag.putInt("preDelay", preDelay);
        tag.putInt("postDelay", postDelay);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        isRunning = compoundTag.getBoolean("isRunning");
        isDone = compoundTag.getBoolean("isDone");
        preDelay = compoundTag.getInt("preDelay");
        postDelay = compoundTag.getInt("postDelay");
    }

    public abstract OP getType();

    /**
     * @return true if the OP is done
     */
    protected abstract boolean updateCart(AMHSRailNet net);

    protected void onStart(AMHSRailNet net) {
        delay = preDelay;
    }

    protected void onDone() {
        delay = postDelay;
    }

    protected void setRemoved() {
        isRemoved = true;
    }

    public final void updateOP(AMHSRailNet net) {
        if (isRemoved()) return;
        if (isDone()) {
            if (delay > 0) {
                delay--;
            } else {
                setRemoved();
            }
        } else {
            if (delay > 0) {
                delay--;
                return;
            }
            if (!isRunning()) {
                isRunning = true;
                onStart(net);
            }
            if (updateCart(net)) {
                isDone = true;
                onDone();
            }
        }
    }

}
