package screret.robotarm.block;

import com.gregtechceu.gtceu.api.block.AppearanceBlock;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.client.renderer.block.RailBlockRenderer;
import screret.robotarm.pipenet.amhs.AMHSRailNet;
import screret.robotarm.pipenet.amhs.AMHSRailType;
import screret.robotarm.pipenet.amhs.LevelAMHSRailNet;
import screret.robotarm.pipenet.amhs.RailConnection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

import static screret.robotarm.pipenet.amhs.RailConnection.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AMHSRailBlock extends AppearanceBlock implements IBlockRendererProvider, IToolGridHighlight, IToolable {
    public static final EnumProperty<Direction> DIRECTION = EnumProperty.create("direction", Direction.class, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);

    public final AMHSRailType railType;
    protected IRenderer renderer;

    public AMHSRailBlock(Properties properties, AMHSRailType railType) {
        super(railType.onProperties(properties));
        var defaultState = defaultBlockState();
        if (railType.connectionProperty != null) {
            defaultState = defaultState.setValue(railType.connectionProperty, RailConnection.STRAIGHT);
        }
        registerDefaultState(defaultState.setValue(DIRECTION, Direction.SOUTH));
        this.railType = railType;
    }

    public void onRegister() {
        if (LDLib.isClient()) {
            this.renderer = createRenderer();
        } else {
            this.renderer = null;
        }
    }

    public IRenderer createRenderer() {
        return new RailBlockRenderer(railType);
    }

    public RailConnection getRailConnection(BlockState state) {
        if (railType.connectionProperty == null) {
            return railType.railConnections.iterator().next();
        }
        return state.getValue(railType.connectionProperty);
    }

    public BlockState setRailConnection(BlockState state, RailConnection connection) {
        if (railType.connectionProperty == null) {
            return state;
        }
        if (railType.railConnections.contains(connection)) {
            return state.setValue(railType.connectionProperty, connection);
        }
        return state;
    }

    public Direction getRailDirection(BlockState state) {
        return state.getValue(DIRECTION);
    }

    public BlockState setRailDirection(BlockState state, Direction direction) {
        return state.setValue(DIRECTION, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        var type = AMHSRailType.get();
        if (type.connectionProperty != null) {
            builder = builder.add(type.connectionProperty);
        }
        super.createBlockStateDefinition(builder.add(DIRECTION, BlockProperties.SERVER_TICK));
    }

    public LevelAMHSRailNet getWorldRailNet(ServerLevel level) {
        return LevelAMHSRailNet.getOrCreate(level);
    }

    @Override
    public @Nullable IRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = this.defaultBlockState();
        //find connected rail
        for (var side : AMHSRailNet.VALUES) {
            var neighbour = context.getLevel().getBlockState(context.getClickedPos().relative(side));
            if (neighbour.getBlock() instanceof AMHSRailBlock railBlock) {
                var con = railBlock.getRailConnection(neighbour);
                var dir = railBlock.getRailDirection(neighbour);
                var io = con.getIO(dir, side.getOpposite());
                if (io == IO.IN) {
                    return setRailDirection(setRailConnection(state, RailConnection.STRAIGHT), side.getOpposite());
                } else if (io == IO.OUT) {
                    return setRailDirection(setRailConnection(state, RailConnection.STRAIGHT), side);
                }
            }
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level instanceof ServerLevel serverLevel) {
            var net = getWorldRailNet(serverLevel);
            if (net.getNetFromPos(pos) == null) {
                net.addNode(pos, railType, getRailConnection(state), getRailDirection(state), Node.DEFAULT_MARK, true);
            } else {
                net.updateNodeConnections(pos, getRailConnection(state), getRailDirection(state));
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            pLevel.updateNeighbourForOutputSignal(pPos, this);
            if (pState.hasBlockEntity()) {
                pLevel.removeBlockEntity(pPos);
            }
            if (pLevel instanceof ServerLevel serverLevel) {
                getWorldRailNet(serverLevel).removeNode(pPos);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player){
            var held = player.getMainHandItem();
            if (held.is(GTToolType.WRENCH.itemTags.get(0))) {
                return Shapes.block();
            }
        }
        return getRailConnection(pState).getShape(getRailDirection(pState));
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held, Set<GTToolType> toolTypes) {
        return canToolTunePipe(toolTypes);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes, Direction side) {
        if (canToolTunePipe(toolTypes) && side.getAxis() != Direction.Axis.Y) {
            if (player.isCrouching()) {
                var direction = getRailDirection(state);
                if (direction != side) {
                    return GuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            } else {
                var direction = getRailDirection(state);
                if(side.getAxis() != direction.getAxis()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        Set<GTToolType> types = ToolHelper.getToolTypes(itemStack);
        if (state.getBlock() instanceof IToolable toolable && !types.isEmpty() && ToolHelper.canUse(itemStack)) {
            var result = toolable.onToolClick(types, itemStack, new UseOnContext(player, hand, hit));
            if (result.getSecond() == InteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(result.getFirst(), serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(itemStack, serverPlayer, 1);
                }
            }
            return result.getSecond();
        }
        return InteractionResult.PASS;
    }

    @Override
    public Pair<@Nullable GTToolType, InteractionResult> onToolClick(@NotNull Set<GTToolType> toolTypes, ItemStack itemStack, UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return Pair.of(null, InteractionResult.PASS);
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        if (gridSide == null) gridSide = hitResult.getDirection();

        var state = level.getBlockState(pos);
        if (canToolTunePipe(toolTypes) && gridSide.getAxis() != Direction.Axis.Y) {
            if (playerIn.isCrouching()) {
                var direction = getRailDirection(state);
                if (direction != gridSide) {
                    if (!level.isClientSide) {
                        level.setBlockAndUpdate(context.getClickedPos(), state = setRailDirection(state, gridSide));
                    }
                    return Pair.of(GTToolType.WRENCH, InteractionResult.CONSUME);
                }
            } else {
                var direction = getRailDirection(state);
                if(gridSide.getAxis() != direction.getAxis()) {
                    if (!level.isClientSide) {
                        boolean isLeft = direction.getClockWise() == gridSide;
                        RailConnection nextConnection = switch (getRailConnection(state)) {
                            case STRAIGHT -> isLeft ? LEFT : RIGHT;
                            case LEFT -> isLeft ? STRAIGHT_LEFT_IN : RIGHT;
                            case RIGHT -> isLeft ? LEFT : STRAIGHT_RIGHT_IN;
                            case STRAIGHT_LEFT_IN -> isLeft ? STRAIGHT_LEFT_OUT : STRAIGHT_RIGHT_IN;
                            case STRAIGHT_RIGHT_IN -> isLeft ? STRAIGHT_LEFT_IN : STRAIGHT_RIGHT_OUT;
                            case STRAIGHT_LEFT_OUT -> isLeft ? STRAIGHT : STRAIGHT_RIGHT_OUT;
                            case STRAIGHT_RIGHT_OUT -> isLeft ? STRAIGHT_LEFT_OUT : STRAIGHT;
                        };
                        level.setBlockAndUpdate(context.getClickedPos(), state = setRailConnection(state, nextConnection));
                    }
                    return Pair.of(GTToolType.WRENCH, InteractionResult.CONSUME);
                }
            }
        }

        return Pair.of(null, InteractionResult.PASS);
    }

    protected boolean canToolTunePipe(Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.WRENCH);
    }

}
