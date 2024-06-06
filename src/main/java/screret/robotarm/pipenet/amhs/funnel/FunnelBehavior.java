package screret.robotarm.pipenet.amhs.funnel;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.blockentity.FOUPFunnelBlockEntity;

import java.util.Arrays;

public class FunnelBehavior implements IManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FunnelBehavior.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    public final FOUPFunnelBlockEntity holder;

    protected final int slotSize = 9;
    private final ItemFilter[] itemFilters;
    @Persisted
    private final IO[] ios;
    @Persisted
    protected final ItemStackTransfer cacheContainer;
    @Persisted
    protected final ItemStackTransfer filterContainer;

    public FunnelBehavior(FOUPFunnelBlockEntity holder) {
        this.holder = holder;
        this.itemFilters = new ItemFilter[slotSize];
        this.ios = new IO[slotSize];
        Arrays.fill(this.ios, IO.NONE);
        this.cacheContainer = new ItemStackTransfer(slotSize);
        this.filterContainer = new ItemStackTransfer(slotSize);
        this.filterContainer.setFilter(itemStack -> ItemFilter.FILTERS.containsKey(itemStack.getItem()));
        this.filterContainer.setOnContentsChanged(() -> {
            for (int i = 0; i < slotSize; i++) {
                itemFilters[i] = null;
            }
        });
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        holder.setChanged();
    }

    @Nullable
    public ItemFilter getItemFilter(int slotIndex) {
        if (itemFilters[slotIndex] == null) {
            var filterItem = filterContainer.getStackInSlot(slotIndex);
            if (!filterItem.isEmpty() && ItemFilter.FILTERS.containsKey(filterItem.getItem())) {
                itemFilters[slotIndex] = ItemFilter.loadFilter(filterItem);
            }
        }
        return itemFilters[slotIndex];
    }

    public void serverTick() {
        if (holder.getOffsetTimer() % 5 == 0) {
            // network
            if (holder.getOffsetTimer() % 20 == 0) {

            }
            // cache
            var attachedSide = holder.getAttachedSide();
            var container = ItemTransferHelper.getItemTransfer(holder.getLevel(), holder.getBlockPos().relative(attachedSide), attachedSide.getOpposite());
            if (container == null) {
                return;
            }
            // input to cache
            for (int i = 0; i < container.getSlots(); i++) {
                var extracted = container.extractItem(i, Integer.MAX_VALUE, true);
                if (!extracted.isEmpty()) {
                    for (int slotIndex = 0; slotIndex < slotSize; slotIndex++) {
                        var io = ios[slotIndex];
                        if (io == IO.OUT) {
                            var filter = getItemFilter(slotIndex);
                            if (filter == null || filter.test(extracted)) {
                                var inserted = extracted.getCount() - cacheContainer.insertItem(slotIndex, extracted, true).getCount();
                                if (inserted > 0) {
                                    extracted.shrink(inserted - cacheContainer.insertItem(slotIndex, container.extractItem(i, inserted, false), false).getCount());
                                }
                                if (extracted.isEmpty()) break;
                            }
                        }
                    }
                }
            }
            // output to attached container
            for (int slotIndex = 0; slotIndex < slotSize; slotIndex++) {
                var io = ios[slotIndex];
                if (io == IO.IN) {
                    var stored = cacheContainer.getStackInSlot(slotIndex);
                    if (!stored.isEmpty()) {
                        var remaining = ItemTransferHelper.insertItemStacked(container, stored, true);
                        remaining = ItemTransferHelper.insertItemStacked(container, cacheContainer.extractItem(slotIndex, stored.getCount() - remaining.getCount(), false), false);
                        if (!remaining.isEmpty()) {
                            cacheContainer.insertItem(slotIndex, remaining, false);
                        }
                    }
                }
            }
        }
    }

    //////////////////////////////////////
    //*********       GUI      *********//
    //////////////////////////////////////
    public WidgetGroup createUI(Player entityPlayer) {
        var slotGroup = new WidgetGroup(0, 0, 162, 18 * 3);
        for (int i = 0; i < slotSize; i++) {
            int finalI = i;
            var filterSlot = new SlotWidget(filterContainer, i, i * 18, 0)
                    .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
            var cacheSlot = new SlotWidget(cacheContainer, i, i * 18, 18)
                    .setBackgroundTexture(GuiTextures.SLOT);
            var ioButton = new EnumSelectorWidget<>(i * 18, 18 * 2, 18, 18,
                    new IO[]{IO.NONE, IO.IN, IO.BOTH}, ios[finalI], io -> ios[finalI] = io);
            slotGroup.addWidget(filterSlot);
            slotGroup.addWidget(cacheSlot);
            slotGroup.addWidget(ioButton);
        }
        return slotGroup;
    }

}
