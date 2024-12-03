package screret.robotarm.block;

import com.google.common.collect.Maps;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib.gui.factory.BlockEntityUIFactory;
import com.lowdragmc.lowdraglib.utils.ShapeUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import screret.robotarm.blockentity.FOUPFunnelBlockEntity;
import screret.robotarm.client.renderer.block.FOUPFunnelRenderer;
import screret.robotarm.data.blockentity.RobotArmBlockEntities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/8/14
 * @implNote FOUPFunnelBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FOUPFunnelBlock extends RendererBlock implements EntityBlock {
    public static final VoxelShape SHAPE = Shapes.or(box(0, 1, 0, 16, 2, 16), box(2, 0, 2, 14, 1, 14));
    private final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION;

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN);

    public FOUPFunnelBlock(Properties properties) {
        super(properties, FOUPFunnelRenderer.INSTANCE);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
        this.SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (map) -> {
            map.put(Direction.NORTH, ShapeUtils.rotate(ShapeUtils.rotate(SHAPE, new Vector3f(1, 0, 0), 90),
                    new Vector3f(0, 1, 0), 180));
            map.put(Direction.EAST,  ShapeUtils.rotate(ShapeUtils.rotate(SHAPE, new Vector3f(1, 0, 0), 90),
                    new Vector3f(0, 1, 0), 90));
            map.put(Direction.SOUTH, ShapeUtils.rotate(SHAPE, new Vector3f(1, 0, 0), 90));
            map.put(Direction.WEST,  ShapeUtils.rotate(ShapeUtils.rotate(SHAPE, new Vector3f(1, 0, 0), 90),
                    new Vector3f(0, 1, 0), 270));
            map.put(Direction.DOWN,  SHAPE);
        });
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return RobotArmBlockEntities.FOUP_FUNNEL.create(pPos, pState);
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING,
                pContext.getClickedFace() == Direction.UP ? Direction.DOWN : pContext.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_DIRECTION.get(pState.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType == RobotArmBlockEntities.FOUP_FUNNEL.get() && !level.isClientSide) {
            return (pLevel, pPos, pState, pTile) -> {
                if (pTile instanceof FOUPFunnelBlockEntity foupFunnelBlockEntity) {
                    foupFunnelBlockEntity.serverTick();
                }
            };
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof FOUPFunnelBlockEntity funnel && player instanceof ServerPlayer serverPlayer) {
            BlockEntityUIFactory.INSTANCE.openUI(funnel, serverPlayer);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
