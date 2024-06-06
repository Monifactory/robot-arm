package screret.robotarm.pipenet.amhs;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.lowdragmc.lowdraglib.utils.ShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/8/8
 * @implNote RailConnection
 */
public enum RailConnection implements StringRepresentable {
    STRAIGHT(in -> new Direction[] {in}, in -> new Direction[] {in.getOpposite()}, Block.box(5, 12, 0, 11, 16, 16), "straight"),
    LEFT(in -> new Direction[] {in}, in -> new Direction[] {in.getClockWise()}, Shapes.or(Block.box(0, 12, 5, 5, 16, 11), Block.box(5, 12, 5, 11, 16, 16)), "left"),
    RIGHT(in -> new Direction[] {in}, in -> new Direction[] {in.getCounterClockWise()}, Shapes.or(Block.box(11, 12, 5, 16, 16, 11), Block.box(5, 12, 5, 11, 16, 16)), "right"),
    STRAIGHT_LEFT_IN(in -> new Direction[] {in, in.getClockWise()}, in -> new Direction[] {in.getOpposite()}, Shapes.or(STRAIGHT.shape, Block.box(0, 12, 5, 5, 16, 11)), "straight_left_in"),
    STRAIGHT_RIGHT_IN(in -> new Direction[] {in, in.getCounterClockWise()}, in -> new Direction[] {in.getOpposite()}, Shapes.or(STRAIGHT.shape, Block.box(11, 12, 5, 16, 16, 11)), "straight_right_in"),
    STRAIGHT_LEFT_OUT(in -> new Direction[] {in}, in -> new Direction[] {in.getOpposite(), in.getClockWise()}, Shapes.or(STRAIGHT.shape, Block.box(0, 12, 5, 5, 16, 11)), "straight_left_out"),
    STRAIGHT_RIGHT_OUT(in -> new Direction[] {in}, in -> new Direction[] {in.getOpposite(), in.getCounterClockWise()}, Shapes.or(STRAIGHT.shape, Block.box(11, 12, 5, 16, 16, 11)), "straight_right_out");

    final EnumMap<Direction, Set<Direction>> outputs = new EnumMap<>(Direction.class);
    final EnumMap<Direction, Set<Direction>> inputs = new EnumMap<>(Direction.class);
    final EnumMap<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    final Function<Direction, Direction[]> inputMaps;
    final Function<Direction, Direction[]> outputMaps;
    final VoxelShape shape;
    public final String name;

    RailConnection(Function<Direction, Direction[]> inputMaps, Function<Direction, Direction[]> outputMaps, VoxelShape shape, String name) {
        this.inputMaps = inputMaps;
        this.outputMaps = outputMaps;
        this.shape = shape;
        this.name = name;
    }

    public Set<Direction> getInDirections(Direction direction) {
        return inputs.computeIfAbsent(direction, d -> new HashSet<>(Arrays.stream(inputMaps.apply(d)).toList()));
    }

    public Set<Direction> getOutDirections(Direction direction) {
        return outputs.computeIfAbsent(direction, d -> new HashSet<>(Arrays.stream(outputMaps.apply(d)).toList()));
    }

    public IO getIO(Direction direction, Direction side) {
        if (getInDirections(direction).contains(side)) {
            return IO.IN;
        }
        if (getOutDirections(direction).contains(side)) {
            return IO.OUT;
        }
        return IO.NONE;
    }

    @Override
    @Nonnull
    public String getSerializedName() {
        return name;
    }

    public VoxelShape getShape(Direction side) {
        return shapes.computeIfAbsent(side, s -> ShapeUtils.rotate(shape, s.getOpposite()));
    }
}
