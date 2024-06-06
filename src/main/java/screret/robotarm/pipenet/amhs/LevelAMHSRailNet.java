package screret.robotarm.pipenet.amhs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * @author KilaBash
 * @date 2023/8/8
 * @implNote LevelRailNet
 */
public class LevelAMHSRailNet extends SavedData {

    private final ServerLevel serverLevel;
    protected List<AMHSRailNet> railNets = new ArrayList<>();
    protected final Map<ChunkPos, List<AMHSRailNet>> railNetsByChunk = new HashMap<>();

    public static LevelAMHSRailNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelAMHSRailNet(serverLevel, tag), () -> new LevelAMHSRailNet(serverLevel), "gtcue_amhs_rail_net");
    }

    protected LevelAMHSRailNet(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    protected LevelAMHSRailNet(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        this.railNets = new ArrayList<>();
        ListTag allEnergyNets = tag.getList("RailNets", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergyNets.size(); i++) {
            CompoundTag pNetTag = allEnergyNets.getCompound(i);
            var railNet = createNetInstance();
            railNet.deserializeNBT(pNetTag);
            addRailNetSilently(railNet);
        }
        init();
    }

    public ServerLevel getWorld() {
        return serverLevel;
    }

    protected void init() {
        this.railNets.forEach(AMHSRailNet::onNodeConnectionsUpdate);
    }

    public void addNode(BlockPos nodePos, AMHSRailType railType, RailConnection connection, Direction direction, int mark, boolean isActive) {
        AMHSRailNet myRailNet = null;
        AMHSRailNode node = new AMHSRailNode(nodePos, railType, connection, direction, mark, isActive);
        for (Direction facing : AMHSRailNet.VALUES) {
            BlockPos offsetPos = nodePos.relative(facing);
            var railNet = getNetFromPos(offsetPos);
            AMHSRailNode secondNode = railNet == null ? null : railNet.getAllNodes().get(offsetPos);
            if (railNet != null && railNet.canNodesConnected(secondNode, facing.getOpposite(), node, null)) {
                if (myRailNet == null) {
                    myRailNet = railNet;
                    myRailNet.addNode(nodePos, node);
                } else if (myRailNet != railNet) {
                    myRailNet.uniteNetworks(railNet);
                }
            }

        }
        if (myRailNet == null) {
            myRailNet = createNetInstance();
            myRailNet.addNode(nodePos, node);
            addRailNet(myRailNet);
            setDirty();
        }
    }

    protected void addRailNetToChunk(ChunkPos chunkPos, AMHSRailNet railNet) {
        this.railNetsByChunk.computeIfAbsent(chunkPos, any -> new ArrayList<>()).add(railNet);
    }

    protected void removeRailNetFromChunk(ChunkPos chunkPos, AMHSRailNet railNet) {
        var list = this.railNetsByChunk.get(chunkPos);
        if (list != null) {
            list.remove(railNet);
            if (list.isEmpty()) this.railNetsByChunk.remove(chunkPos);
        }
    }

    public void removeNode(BlockPos nodePos) {
        var railNet = getNetFromPos(nodePos);
        if (railNet != null) {
            railNet.removeNode(nodePos);
        }
    }

    public void updateNodeConnections(BlockPos nodePos, RailConnection connection, Direction direction) {
        var railNet = getNetFromPos(nodePos);
        if (railNet != null) {
            railNet.updateNodeConnections(nodePos, connection, direction);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        var railNet = getNetFromPos(nodePos);
        if (railNet != null) {
            railNet.updateMark(nodePos, newMark);
        }
    }

    public AMHSRailNet getNetFromPos(BlockPos blockPos) {
        List<AMHSRailNet> railNetsInChunk = railNetsByChunk.getOrDefault(new ChunkPos(blockPos), Collections.emptyList());
        for (AMHSRailNet railNet : railNetsInChunk) {
            if (railNet.containsNode(blockPos))
                return railNet;
        }
        return null;
    }

    protected void addRailNet(AMHSRailNet railNet) {
        addRailNetSilently(railNet);
    }

    protected void addRailNetSilently(AMHSRailNet railNet) {
        this.railNets.add(railNet);
        railNet.getContainedChunks().forEach(chunkPos -> addRailNetToChunk(chunkPos, railNet));
        railNet.isValid = true;
    }

    protected void removeRailNet(AMHSRailNet railNet) {
        this.railNets.remove(railNet);
        railNet.getContainedChunks().forEach(chunkPos -> removeRailNetFromChunk(chunkPos, railNet));
        railNet.isValid = false;
        setDirty();
    }

    protected AMHSRailNet createNetInstance() {
        return new AMHSRailNet(this);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag allRailNets = new ListTag();
        for (var railNet : railNets) {
            CompoundTag pNetTag = railNet.serializeNBT();
            allRailNets.add(pNetTag);
        }
        compound.put("RailNets", allRailNets);
        return compound;
    }
}
