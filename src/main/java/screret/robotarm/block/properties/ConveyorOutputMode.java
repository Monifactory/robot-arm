package screret.robotarm.block.properties;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ConveyorOutputMode implements StringRepresentable {
    NORMAL,
    LEFT_FRONT,
    RIGHT_FRONT;

    public static final ConveyorOutputMode[] VALUES = values();

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
