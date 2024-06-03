package screret.robotarm.block.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ConveyorSlope implements StringRepresentable {
    UP,
    NONE,
    DOWN;

    public BlockPos getOffsetPos(BlockPos pos) {
        return switch (this) {
            case UP -> pos.above();
            case NONE, DOWN -> pos;
        };
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
