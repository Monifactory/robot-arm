package screret.robotarm.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.jetbrains.annotations.Nullable;
import screret.robotarm.blockentity.FOUPRailBlockEntity;
import screret.robotarm.data.entity.RobotArmEntities;
import screret.robotarm.pipenet.amhs.AMHSRailNet;
import screret.robotarm.pipenet.amhs.LevelAMHSRailNet;
import screret.robotarm.pipenet.amhs.op.AwaitOp;
import screret.robotarm.pipenet.amhs.op.FOUPOp;
import screret.robotarm.pipenet.amhs.op.OP;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FOUPCartEntity extends Entity {
    private final static int AWAITING_MAX_TIME = 25;
    private final static int AWAITING_WAITING_TIME = 10;
    private static final EntityDataAccessor<Integer> DATA_ID_AWAITING = SynchedEntityData.defineId(FOUPCartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_ID_AWAITED_RAIL = SynchedEntityData.defineId(FOUPCartEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> DATA_ID_SLING = SynchedEntityData.defineId(FOUPCartEntity.class, EntityDataSerializers.INT);
    private WeakReference<AMHSRailNet> currentRailNet = new WeakReference<>(null);
    protected final Queue<FOUPOp> opQueue = new LinkedList<>();

    // runtime
    private float currentAwaiting, currentSling;
    private float awaitingO, slingO;
    private int lAwaiting, lSling;
    private int lSteps;
    private double lx;
    private double ly;
    private double lz;
    private float lyr;
    private float lxr;

    public FOUPCartEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static FOUPCartEntity create(Level level, BlockPos foupRailPos, Direction initialDirection) {
        var entity = RobotArmEntities.FOUP.create(level);
        assert entity != null;
        entity.setPos(foupRailPos.getX() + 0.5, foupRailPos.getY(), foupRailPos.getZ() + 0.5);
        entity.xo = foupRailPos.getX() + 0.5;
        entity.yo = foupRailPos.getY();
        entity.zo = foupRailPos.getZ() + 0.5;
        entity.setYRot((initialDirection.get2DDataValue() * 90 + 180) % 360);
        return entity;
    }

    //////////////////////////////////////
    //*****     INITIALIZATION    ******//
    //////////////////////////////////////
    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_AWAITING, 0);
        this.entityData.define(DATA_ID_SLING, 0);
        this.entityData.define(DATA_ID_AWAITED_RAIL, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        var opList = compound.getList("ops", Tag.TAG_COMPOUND);
        for (var i = 0; i < opList.size(); i++) {
            FOUPOp.loadFromNBT(this, opList.getCompound(i)).ifPresent(opQueue::add);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var opList = new ListTag();
        for (var op : opQueue) {
            opList.add(op.serializeNBT());
        }
        compound.put("ops", opList);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void setLevelCallback(EntityInLevelCallback levelCallback) {
        super.setLevelCallback(levelCallback);
    }

    @Override
    public BlockPos getOnPos() {
        return super.getOnPos().above();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int lerpSteps, boolean teleport) {
        this.lx = x;
        this.ly = y;
        this.lz = z;
        this.lyr = yRot;
        this.lxr = xRot;
        this.lSteps = lerpSteps;
    }

    @Override
    public void syncPacketPositionCodec(double x, double y, double z) {
        super.syncPacketPositionCodec(x, y, z);
    }

    public Optional<BlockPos> getAwaitedPos() {
        return this.entityData.get(DATA_ID_AWAITED_RAIL);
    }

    public void setAwaitedPos(@Nullable BlockPos destination) {
        this.entityData.set(DATA_ID_AWAITED_RAIL, Optional.ofNullable(destination));
    }

    public int getAwaitingData() {
        return this.entityData.get(DATA_ID_AWAITING);
    }

    public void setAwaitingData(int data) {
        if (data >= 0 && data <= AWAITING_MAX_TIME) {
            this.entityData.set(DATA_ID_AWAITING, data);
            if (data == 0) {
                this.setAwaitedPos(null);
            }
        }
    }

    public int getSlingData() {
        return this.entityData.get(DATA_ID_SLING);
    }

    public void setSling(int data) {
        if (data >= 0) {
            this.entityData.set(DATA_ID_SLING, data);
        }
    }

    /**
     * is on the way to foup track or is awaited.
     */
    public boolean isAwaiting() {
        return this.entityData.get(DATA_ID_AWAITING) > 0;
    }

    /**
     * is awaited.
     */
    public boolean isAwaited() {
        return this.entityData.get(DATA_ID_AWAITING) >= AWAITING_MAX_TIME;
    }

    public boolean isIdle() {
        return isAwaiting() || opQueue.isEmpty();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (level().isClientSide) {
            if (DATA_ID_SLING.equals(key)) {
                lSling = 3;
            } else if (DATA_ID_AWAITING.equals(key)) {
                lAwaiting = 3;
            } else {
                super.onSyncedDataUpdated(key);
            }
        } else {
            super.onSyncedDataUpdated(key);
        }
    }

    public void syncCartPosAndRot() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().chunkMap.broadcast(this, new ClientboundTeleportEntityPacket(this));
        }
    }

    //////////////////////////////////////
    //********       LOGIC     *********//
    //////////////////////////////////////
    public void addOps(FOUPOp... ops) {
        opQueue.addAll(Arrays.asList(ops));
    }

    public void discardAllOps() {
        opQueue.clear();
        if (isAwaiting() && !isAwaited()) {
            setAwaitingData(0);
        }
    }

    @Override
    public void baseTick() {
        this.level().getProfiler().push("foupEntityTick");

        if (!level().isClientSide) {
            var net = getRailNet();
            if (net == null) {
                discard();
                dropContainer();
                return;
            }
            if (opQueue.isEmpty()) {
                // TODO if container is non empty drop it
                doAwait();
            } else {
                var head = opQueue.peek();
                if (!isAwaiting() || head.getType() == OP.AWAIT) {
                    head.updateOP(net);
                    if (head.isRemoved()) {
                        opQueue.poll();
                    }
                } else {
                    setAwaitingData(getAwaitingData() - 1);
                }
            }
        } else {
            // update client position
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.awaitingO = this.currentAwaiting;
            this.slingO = this.currentSling;
            var realAwaitingData = this.getAwaitingData();
            this.getAwaitedPos().ifPresent(p -> {
                if (level().getBlockEntity(p) instanceof FOUPRailBlockEntity rail) {
                    rail.setAwaitedCart(this);
                }
            });

            if (lAwaiting > 0) {
                this.currentAwaiting += (realAwaitingData - this.currentAwaiting) / lAwaiting;
                lAwaiting--;
            } else {
                this.currentAwaiting = realAwaitingData;
            }

            if (lSling > 0) {
                this.currentSling += (this.getSlingData() - this.currentSling) / lSling;
                lSling--;
            } else {
                this.currentSling = this.getSlingData();
            }

            if (lSteps > 0) {
                var x = this.getX() + (this.lx - this.getX()) / this.lSteps;
                var y = this.getY() + (this.ly - this.getY()) / this.lSteps;
                var z = this.getZ() + (this.lz - this.getZ()) / this.lSteps;
                setPos(x, y, z);

                float diff = (this.lyr - this.getYRot()) % 360;
                if (diff < -180.0F) {
                    diff += 360.0F;
                } else if (diff >= 180.0F) {
                    diff -= 360.0F;
                }

                setYRot(this.getYRot() + diff / this.lSteps);
                --this.lSteps;
            }
        }

        this.firstTick = false;
        this.tickCount++;
        this.level().getProfiler().pop();
    }

    public void doAwait() {
        if (!isAwaiting()) {
            discardAllOps();
            addOps(new AwaitOp(this));
            setAwaitingData(1);
        }
    }

    @Nullable
    public AMHSRailNet getRailNet() {
        if (level() instanceof ServerLevel serverLevel) {
            var lastRailNet = this.currentRailNet.get();
            var pos = getOnPos();
            if (lastRailNet != null && lastRailNet.isValid() && lastRailNet.containsNode(pos) && lastRailNet.containCart(this)) {
                return lastRailNet;
            }

            var newRailNet = LevelAMHSRailNet.getOrCreate(serverLevel).getNetFromPos(pos);
            if (newRailNet != null) {
                newRailNet.addCart(this);
            }
            this.currentRailNet = new WeakReference<>(newRailNet);

            if (lastRailNet != null && lastRailNet != newRailNet && lastRailNet.containCart(this)) {
                lastRailNet.removeCart(this);
            }
        }
        return currentRailNet.get();
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
    }

    protected void dropContainer() {
    }

    public float getAwaitingDegree(float partialTicks) {
        return Mth.lerp(partialTicks, Math.max(this.awaitingO - 1 - AWAITING_WAITING_TIME, 0), Math.max(this.currentAwaiting - 1 - AWAITING_WAITING_TIME, 0)) / (AWAITING_MAX_TIME - 1 - AWAITING_WAITING_TIME) * 180;
    }

    public float getSlingLength(float partialTicks) {
        return Mth.lerp(partialTicks, this.slingO, this.currentSling);
    }
}
