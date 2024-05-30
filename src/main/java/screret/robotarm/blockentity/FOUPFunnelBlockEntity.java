package screret.robotarm.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import screret.robotarm.block.FOUPFunnelBlock;
import screret.robotarm.pipenet.amhs.funnel.FunnelBehavior;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote FOUPFunnelBlockEntity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FOUPFunnelBlockEntity extends BlockEntity implements IUIHolder, IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity, IManaged {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FOUPFunnelBlockEntity.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    private final long offset = GTValues.RNG.nextInt(20);
    @Persisted @Getter
    private final FunnelBehavior behavior = new FunnelBehavior(this);

    public FOUPFunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        setChanged();
    }

    @Override
    public IManagedStorage getRootStorage() {
        return syncStorage;
    }

    public Direction getAttachedSide() {
        return getBlockState().getValue(FOUPFunnelBlock.FACING);
    }

    public long getOffsetTimer() {
        return level == null ? offset : (level.getGameTime() + offset);
    }

    public void serverTick() {
        behavior.serverTick();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var widget = behavior.createUI(entityPlayer);
        widget.setSelfPosition(new Position(7, 20));
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(widget)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }

    @Override
    public boolean isInvalid() {
        return isRemoved();
    }

    @Override
    public boolean isRemote() {
        return getLevel() == null ? LDLib.isRemote() : getLevel().isClientSide;
    }

    @Override
    public void markAsDirty() {
        setChanged();
    }
}
