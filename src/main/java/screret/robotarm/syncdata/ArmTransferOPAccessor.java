package screret.robotarm.syncdata;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import net.minecraft.nbt.CompoundTag;
import screret.robotarm.machine.transfer.ArmTransferOP;

public class ArmTransferOPAccessor extends CustomObjectAccessor<ArmTransferOP> {

    public ArmTransferOPAccessor() {
        super(ArmTransferOP.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, ArmTransferOP op) {
        return NbtTagPayload.of(op.serializeNBT());
    }

    @Override
    public ArmTransferOP deserialize(AccessorOp accessorOp, ITypedPayload<?> payload) {
        var op = new ArmTransferOP();
        if (payload instanceof NbtTagPayload nbtTagPayload && nbtTagPayload.getPayload() instanceof CompoundTag tag) {
            op.deserializeNBT(tag);
        }
        return op;
    }
}
