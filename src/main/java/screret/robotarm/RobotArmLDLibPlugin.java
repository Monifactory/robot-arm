package screret.robotarm;

import com.gregtechceu.gtceu.common.data.GTSyncedFieldAccessors;
import com.lowdragmc.lowdraglib.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib.syncdata.IAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import screret.robotarm.syncdata.ArmTransferOPAccessor;

import static com.lowdragmc.lowdraglib.syncdata.TypedPayloadRegistries.*;

@LDLibPlugin
public class RobotArmLDLibPlugin implements ILDLibPlugin {

    public static final IAccessor ARM_TRANSFER_OP_ACCESSOR = new ArmTransferOPAccessor();

    @Override
    public void onLoad() {
        register(NbtTagPayload.class, NbtTagPayload::new, ARM_TRANSFER_OP_ACCESSOR, 1000);
    }
}
