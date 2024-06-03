package screret.robotarm.util;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import screret.robotarm.block.ConveyorBeltBlock;
import screret.robotarm.block.properties.ConveyorSlope;

public class ConveyorBeltState {
	private final Level level;
	private final BlockPos pos;
	private final ConveyorBeltBlock block;
	private final Direction direction;
	private final ConveyorSlope slope;
	@Getter
    private BlockState state;
	@Getter
    private final List<BlockPos> connections = Lists.newArrayList();

	public ConveyorBeltState(Level level, BlockPos pos, BlockState state) {
		this.level = level;
		this.pos = pos;
		this.state = state;
		this.block = (ConveyorBeltBlock)state.getBlock();
		this.direction = state.getValue(ConveyorBeltBlock.FACING);
		this.slope = state.getValue(ConveyorBeltBlock.SLOPE);
		this.updateConnections(this.direction, this.slope);
	}

    private void updateConnections(Direction direction, ConveyorSlope slope) {
		this.connections.clear();
		switch (direction) {
			case NORTH:
				switch (slope) {
					case NONE, UP -> this.connections.add(this.pos.north());
                    case DOWN -> this.connections.add(this.pos.north().above());
				}
				break;
			case SOUTH:
				switch (slope) {
					case NONE, UP -> this.connections.add(this.pos.south());
                    case DOWN -> this.connections.add(this.pos.south().above());
				}
				break;
			case WEST:
				switch (slope) {
					case NONE, UP -> this.connections.add(this.pos.west());
                    case DOWN -> this.connections.add(this.pos.west().above());
				}
				break;
			case EAST:
				switch (slope) {
					case NONE, UP -> this.connections.add(this.pos.east());
                    case DOWN -> this.connections.add(this.pos.east().above());
				}
				break;
		}

	}

	private void removeSoftConnections() {
		for(int i = 0; i < this.connections.size(); ++i) {
			ConveyorBeltState railstate = this.getRail(this.connections.get(i));
			if (railstate != null && railstate.connectsTo(this)) {
				this.connections.set(i, railstate.pos);
			} else {
				this.connections.remove(i--);
			}
		}

	}

	private boolean hasRail(BlockPos pos) {
		return BaseRailBlock.isRail(this.level, pos) || BaseRailBlock.isRail(this.level, pos.above()) || BaseRailBlock.isRail(this.level, pos.below());
	}

	@Nullable
	private ConveyorBeltState getRail(BlockPos pos) {
		BlockState blockstate = this.level.getBlockState(pos);
		if (blockstate.getBlock() instanceof ConveyorBeltBlock) {
			return new ConveyorBeltState(this.level, pos, blockstate);
		} else {
			BlockPos blockPos = pos.above();
			blockstate = this.level.getBlockState(blockPos);
			if (blockstate.getBlock() instanceof ConveyorBeltBlock) {
				return new ConveyorBeltState(this.level, blockPos, blockstate);
			} else {
				blockPos = pos.below();
				blockstate = this.level.getBlockState(blockPos);
				return blockstate.getBlock() instanceof ConveyorBeltBlock ? new ConveyorBeltState(this.level, blockPos, blockstate) : null;
			}
		}
	}

	private boolean connectsTo(ConveyorBeltState state) {
		return this.hasConnection(state.pos);
	}

	private boolean hasConnection(BlockPos pos) {
        for (BlockPos connection : this.connections) {
            if (connection.getX() == pos.getX() && connection.getZ() == pos.getZ()) {
                return true;
            }
        }

		return false;
	}

	protected int countPotentialConnections() {
		int i = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.hasRail(this.pos.relative(direction))) {
                ++i;
            }
        }
		return i;
	}

	private boolean canConnectTo(ConveyorBeltState state) {
		return this.connectsTo(state) || this.connections.size() != 2;
	}

	private void connectTo(ConveyorBeltState state) {
		this.connections.add(state.pos);
		BlockPos north = this.pos.north();
		BlockPos south = this.pos.south();
		BlockPos west = this.pos.west();
		BlockPos east = this.pos.east();
		Direction direction = null;
		ConveyorSlope slope = ConveyorSlope.NONE;
		if (this.hasConnection(north)) {
			direction = Direction.NORTH;
			if (this.level.getBlockState(north.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(north.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		}
		if (this.hasConnection(south)) {
			direction = Direction.SOUTH;
			if (this.level.getBlockState(south.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(south.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		}

		if (this.hasConnection(west)) {
			direction = Direction.WEST;
			if (this.level.getBlockState(west.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(west.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		}
		if (this.hasConnection(east)) {
			direction = Direction.EAST;
			if (this.level.getBlockState(east.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(east.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		}

		if (direction == null) {
			direction = Direction.NORTH;
		}

		this.state = this.state.setValue(ConveyorBeltBlock.FACING, direction).setValue(ConveyorBeltBlock.SLOPE, slope);
		this.level.setBlockAndUpdate(this.pos, this.state);
	}

	private boolean hasNeighborRail(BlockPos pos) {
		ConveyorBeltState railState = this.getRail(pos);
		if (railState == null) {
			return false;
		} else {
			railState.removeSoftConnections();
			return railState.canConnectTo(this);
		}
	}

	public ConveyorBeltState place(Direction facing, ConveyorSlope slope) {
		BlockPos north = this.pos.north();
		BlockPos south = this.pos.south();
		BlockPos west = this.pos.west();
		BlockPos east = this.pos.east();
		boolean hasNorth = this.hasNeighborRail(north);
		boolean hasSouth = this.hasNeighborRail(south);
		boolean hasWest = this.hasNeighborRail(west);
		boolean hasEast = this.hasNeighborRail(east);
		Direction direction = facing;
		boolean hasZAxis = hasNorth || hasSouth;
		boolean hasXAxis = hasWest || hasEast;
		if (hasNorth && !hasXAxis) {
			direction = Direction.NORTH;
		} else if (hasSouth && !hasXAxis) {
			direction = Direction.SOUTH;
		}

		if (hasWest && !hasZAxis) {
			direction = Direction.WEST;
		} else if (hasEast && !hasZAxis) {
			direction = Direction.EAST;
		}

		if (direction == Direction.NORTH) {
			if (this.level.getBlockState(north.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(north.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		} else if (direction == Direction.SOUTH) {
			if (this.level.getBlockState(south.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(south.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		} else if (direction == Direction.WEST) {
			if (this.level.getBlockState(west.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(west.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		} else if (direction == Direction.EAST) {
			if (this.level.getBlockState(east.above()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.UP;
			} else if (this.level.getBlockState(east.below()).getBlock() instanceof ConveyorBeltBlock) {
				slope = ConveyorSlope.DOWN;
			}
		}

		this.updateConnections(direction, slope);
		this.state = this.state.setValue(ConveyorBeltBlock.FACING, direction).setValue(ConveyorBeltBlock.SLOPE, slope);
		if (this.level.getBlockState(this.pos) != this.state) {
			this.level.setBlock(this.pos, this.state, 3);

            for (BlockPos connection : this.connections) {
                ConveyorBeltState railstate = this.getRail(connection);
                if (railstate != null) {
                    railstate.removeSoftConnections();
                    if (railstate.canConnectTo(this)) {
                        railstate.connectTo(this);
                    }
                }
            }
		}

		return this;
	}

}
