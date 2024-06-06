package screret.robotarm.pipenet.amhs.funnel;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.item.behavior.FOUPCasketBehavior;

public class FunnelEmitterSettings implements ITagSerializable<CompoundTag> {
    public final FunnelBehavior funnel;
    @Getter
    private int channel = -1;
    public final ItemStackTransfer cacheContainer = new ItemStackTransfer(1);
    public final ItemStackTransfer filterContainer = new ItemStackTransfer(1);
    // runtime
    private ItemFilter itemFilters;

    public FunnelEmitterSettings(FunnelBehavior funnel) {
        this.funnel = funnel;
        this.cacheContainer.setFilter(itemStack -> FOUPCasketBehavior.getBehavior(itemStack).isPresent());
        this.cacheContainer.setOnContentsChanged(funnel.holder::setChanged);
        this.filterContainer.setFilter(itemStack -> ItemFilter.FILTERS.containsKey(itemStack.getItem()));
        this.filterContainer.setOnContentsChanged(() -> {
            itemFilters = null;
            funnel.holder.setChanged();
        });
    }

    @Nullable
    public ItemFilter getItemFilter() {
        if (itemFilters == null) {
            var filterItem = filterContainer.getStackInSlot(0);
            if (!filterItem.isEmpty() && ItemFilter.FILTERS.containsKey(filterItem.getItem())) {
                itemFilters = ItemFilter.loadFilter(filterItem);
            }
        }
        return itemFilters;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        funnel.holder.setChanged();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("channel", channel);
        tag.put("cache", cacheContainer.serializeNBT());
        tag.put("filter", filterContainer.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        channel = tag.getInt("channel");
        cacheContainer.deserializeNBT(tag.getCompound("cache"));
        filterContainer.deserializeNBT(tag.getCompound("filter"));
    }
}
