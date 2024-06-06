package screret.robotarm.pipenet.amhs;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/8/8
 * @implNote RailNode
 */
public class AMHSRailNode {
    public final BlockPos pos;
    public final AMHSRailType railType;
    public RailConnection connection;
    public Direction direction;
    public int mark;
    public boolean isActive;
    //runtime
    @Nullable @Setter @Getter
    private RailGraphEdge edge;

    public AMHSRailNode(BlockPos pos, AMHSRailType railType, RailConnection connection, Direction direction, int mark, boolean isActive) {
        this.pos = pos;
        this.railType = railType;
        this.connection = connection;
        this.direction = direction;
    }

    public IO getIO(Direction side) {
        return connection.getIO(direction, side);
    }

    public void setConnection(RailConnection connection, Direction direction) {
        this.connection = connection;
        this.direction = direction;
    }

    public boolean isEdge() {
        return edge != null;
    }

}
