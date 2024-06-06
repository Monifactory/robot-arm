package screret.robotarm.pipenet.amhs;

import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.data.RotationState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;
import java.util.Set;

public enum AMHSRailType {
    NORMAL("normal", RailConnection.values()),
    FOUP("foup", RailConnection.STRAIGHT);

    public final String name;
    public final Set<RailConnection> railConnections;
    @Nullable
    public final EnumProperty<RailConnection> connectionProperty;
    AMHSRailType(String name, RailConnection... railConnections) {
        this.name = name;
        this.railConnections = ImmutableSet.copyOf(railConnections);
        this.connectionProperty = railConnections.length > 1 ? EnumProperty.create("connection", RailConnection.class, railConnections) : null;
    }

    public float getThickness() {
        return 1;
    }

    static final ThreadLocal<AMHSRailType> TYPE = new ThreadLocal<>();

    public static AMHSRailType get() {
        return TYPE.get();
    }

    public static void set(AMHSRailType type) {
        TYPE.set(type);
    }

    public BlockBehaviour.Properties onProperties(BlockBehaviour.Properties properties) {
        set(this);
        return properties;
    }
}
