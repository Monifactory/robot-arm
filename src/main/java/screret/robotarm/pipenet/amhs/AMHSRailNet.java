package screret.robotarm.pipenet.amhs;

import com.google.common.graph.*;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.val;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import screret.robotarm.entity.FOUPCartEntity;
import screret.robotarm.pipenet.amhs.astar.AStar;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/8/8
 * @implNote RailNet
 */
@SuppressWarnings("UnstableApiUsage")
public class AMHSRailNet implements ITagSerializable<CompoundTag> {
    public static Direction[] VALUES = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Getter
    protected final LevelAMHSRailNet worldData;
    private final Map<BlockPos, AMHSRailNode> nodeByBlockPos = new HashMap<>();
    private final Map<BlockPos, AMHSRailNode> unmodifiableNodeByBlockPos = Collections.unmodifiableMap(nodeByBlockPos);
    private final Map<ChunkPos, Integer> ownedChunks = new HashMap<>();
    private final Set<BlockPos> foupNodes = new HashSet<>();
    private final Map<Pair<AMHSRailNode, AMHSRailNode>, List<AMHSRailNode>> routeGraphCache = new HashMap<>();
    @Nullable
    private MutableNetwork<AMHSRailNode, RailGraphEdge> graph = null;
    boolean isValid = false;

    public AMHSRailNet(LevelAMHSRailNet levelRailNet) {
        this.worldData = levelRailNet;
    }

    public Set<ChunkPos> getContainedChunks() {
        return Collections.unmodifiableSet(ownedChunks.keySet());
    }

    public ServerLevel getLevel() {
        return worldData.getWorld();
    }

    public boolean isValid() {
        return isValid;
    }

    /**
     * Is only called when connections changed of nodes. Nodes can ONLY connect to other nodes.
     */
    protected void onNodeConnectionsUpdate() {
        graph = null;
        routeGraphCache.clear();
        occupiedNodes.clear();
        carts.removeIf(cart -> !containsNode(cart.getOnPos()) || cart.isRemoved());
    }

    /**
     * Is only called when Data changed of nodes.
     */
    protected void onNodeDataUpdate() {
    }


    public void onNeighbourUpdate(BlockPos fromPos) {
    }

    public Map<BlockPos, AMHSRailNode> getAllNodes() {
        return unmodifiableNodeByBlockPos;
    }

    public AMHSRailNode getNodeAt(BlockPos blockPos) {
        return nodeByBlockPos.get(blockPos);
    }

    public boolean containsNode(BlockPos blockPos) {
        return nodeByBlockPos.containsKey(blockPos);
    }

    public boolean isNodeConnectedTo(BlockPos pos, Direction side) {
        var nodeFirst = getNodeAt(pos);
        if (nodeFirst == null) return false;
        var nodeSecond = getNodeAt(pos.relative(side));
        if (nodeSecond == null) return false;
        return canNodesConnected(nodeFirst, side, nodeSecond, this);
    }

    protected void addNodeSilently(BlockPos nodePos, AMHSRailNode node) {
        this.nodeByBlockPos.put(nodePos, node);
        if (node.railType == AMHSRailType.FOUP) {
            this.foupNodes.add(nodePos);
        }
        checkAddedInChunk(nodePos);
    }

    protected void addNode(BlockPos nodePos, AMHSRailNode node) {
        addNodeSilently(nodePos, node);
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    protected AMHSRailNode removeNodeWithoutRebuilding(BlockPos nodePos) {
        AMHSRailNode removedNode = this.nodeByBlockPos.remove(nodePos);
        this.foupNodes.remove(nodePos);
        ensureRemovedFromChunk(nodePos);
        worldData.setDirty();
        return removedNode;
    }

    public void removeNode(BlockPos nodePos) {
        if (nodeByBlockPos.containsKey(nodePos)) {
            AMHSRailNode selfNode = removeNodeWithoutRebuilding(nodePos);
            rebuildNetworkOnNodeRemoval(nodePos, selfNode);
        }
    }

    protected void checkAddedInChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int newValue = this.ownedChunks.compute(chunkPos, (pos, old) -> (old == null ? 0 : old) + 1);
        if (newValue == 1 && isValid()) {
            this.worldData.addRailNetToChunk(chunkPos, this);
        }
    }

    protected void ensureRemovedFromChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int newValue = this.ownedChunks.compute(chunkPos, (pos, old) -> old == null ? 0 : old - 1);
        if (newValue == 0) {
            this.ownedChunks.remove(chunkPos);
            if (isValid()) {
                this.worldData.removeRailNetFromChunk(chunkPos, this);
            }
        }
    }

    public void updateNodeConnections(BlockPos nodePos, RailConnection connection, Direction direction) {
        if (!containsNode(nodePos)) {
            return;
        }
        AMHSRailNode selfNode = getNodeAt(nodePos);
        if (selfNode.connection == connection && selfNode.direction == direction) {
            return;
        }

        selfNode.setConnection(connection, direction);

        for (var side : VALUES) {
            var offsetPos = nodePos.relative(side);
            var railNetAtOffset = worldData.getNetFromPos(offsetPos);
            if (railNetAtOffset != null) {
                var neighbourNode = railNetAtOffset.getNodeAt(offsetPos);
                if (railNetAtOffset == this){
                    // if same net but unconnected
                    if (!canNodesConnected(selfNode, side, neighbourNode, this)) {
                        var neighbourNet = findAllConnectedBlocks(offsetPos);
                        if (!neighbourNet.containsKey(nodePos)) {
                            var newRailNet = worldData.createNetInstance();
                            neighbourNet.keySet().forEach(this::removeNodeWithoutRebuilding);
                            newRailNet.transferNodeData(neighbourNet, this);
                            worldData.addRailNet(newRailNet);
                        }
                    }
                } else {
                    // if connected but different net
                    if (canNodesConnected(selfNode, side, neighbourNode, railNetAtOffset) &&
                            railNetAtOffset.canNodesConnected(neighbourNode, side.getOpposite(), selfNode, this)) {
                        //so, side is unblocked now, and nodes can connect, merge two networks
                        //our network consumes other one
                        uniteNetworks(railNetAtOffset);
                    }
                }
            }
        }

        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        if (!containsNode(nodePos)) {
            return;
        }
        HashMap<BlockPos, AMHSRailNode> selfConnectedBlocks = null;
        AMHSRailNode selfNode = getNodeAt(nodePos);
        int oldMark = selfNode.mark;
        selfNode.mark = newMark;
        for (Direction facing : VALUES) {
            BlockPos offsetPos = nodePos.relative(facing);
            AMHSRailNet otherRailNet = worldData.getNetFromPos(offsetPos);
            AMHSRailNode secondNode = otherRailNet == null ? null : otherRailNet.getNodeAt(offsetPos);
            if (secondNode == null)
                continue; //there is noting here
            if (!areNodeBlockedConnectionsCompatible(selfNode, facing, secondNode))
                continue; //if connections aren't compatible, skip them
            if (areMarksCompatible(oldMark, secondNode.mark) == areMarksCompatible(newMark, secondNode.mark))
                continue; //if compatibility didn't change, skip it
            if (areMarksCompatible(newMark, secondNode.mark)) {
                //if marks are compatible now, and offset network is different network, merge them
                //if it is same network, just update mask and paths
                if (otherRailNet != this) {
                    uniteNetworks(otherRailNet);
                }
                //marks are incompatible now, and this net is connected with it
            } else if (otherRailNet == this) {
                //search connected nodes from newly marked node
                //populate self connected blocks lazily only once
                if (selfConnectedBlocks == null) {
                    selfConnectedBlocks = findAllConnectedBlocks(nodePos);
                }
                if (getAllNodes().equals(selfConnectedBlocks)) {
                    continue; //if this node is still connected to this network, just continue
                }
                //otherwise, it is not connected
                HashMap<BlockPos, AMHSRailNode> offsetConnectedBlocks = findAllConnectedBlocks(offsetPos);
                //if in the result of remarking offset node has separated from main network,
                //and it is also separated from current cable too, form new network for it
                if (!offsetConnectedBlocks.equals(selfConnectedBlocks)) {
                    offsetConnectedBlocks.keySet().forEach(this::removeNodeWithoutRebuilding);
                    AMHSRailNet offsetRailNet = worldData.createNetInstance();
                    offsetRailNet.transferNodeData(offsetConnectedBlocks, this);
                    worldData.addRailNet(offsetRailNet);
                }
            }
        }
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    public boolean markNodeAsActive(BlockPos nodePos, boolean isActive) {
        if (containsNode(nodePos) && getNodeAt(nodePos).isActive != isActive) {
            getNodeAt(nodePos).isActive = isActive;
            worldData.setDirty();
            onNodeConnectionsUpdate();
            return true;
        }
        return false;
    }

    protected final void uniteNetworks(AMHSRailNet unitedRailNet) {
        Map<BlockPos, AMHSRailNode> allNodes = new HashMap<>(unitedRailNet.getAllNodes());
        worldData.removeRailNet(unitedRailNet);
        allNodes.keySet().forEach(unitedRailNet::removeNodeWithoutRebuilding);
        transferNodeData(allNodes, unitedRailNet);
    }

    private boolean areNodeBlockedConnectionsCompatible(AMHSRailNode first, Direction firstFacing, AMHSRailNode second) {
        var firstIO = first.getIO(firstFacing);
        var secondIO = second.getIO(firstFacing.getOpposite());
        return (firstIO == IO.IN && secondIO == IO.OUT) || (firstIO == IO.OUT && secondIO == IO.IN);
    }

    private boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == Node.DEFAULT_MARK || mark2 == Node.DEFAULT_MARK;
    }

    /**
     * Checks if given nodes can connect
     * Note that this logic should equal with block connection logic
     * for proper work of network
     */
    protected final boolean canNodesConnected(AMHSRailNode first, Direction firstFacing, AMHSRailNode second, AMHSRailNet secondRailNet) {
        return areNodeBlockedConnectionsCompatible(first, firstFacing, second) &&
                areMarksCompatible(first.mark, second.mark);
    }

    //we need to search only this network
    protected HashMap<BlockPos, AMHSRailNode> findAllConnectedBlocks(BlockPos startPos) {
        HashMap<BlockPos, AMHSRailNode> observedSet = new HashMap<>();
        observedSet.put(startPos, getNodeAt(startPos));
        AMHSRailNode firstNode = getNodeAt(startPos);
        BlockPos.MutableBlockPos currentPos = startPos.mutable();
        Stack<Direction> moveStack = new Stack<>();
        main:
        while (true) {
            for (Direction facing : VALUES) {
                currentPos.move(facing);
                AMHSRailNode secondNode = getNodeAt(currentPos);
                //if there is node, and it can connect with previous node, add it to list, and set previous node as current
                if (secondNode != null && canNodesConnected(firstNode, facing, secondNode, this) && !observedSet.containsKey(currentPos)) {
                    observedSet.put(currentPos.immutable(), getNodeAt(currentPos));
                    firstNode = secondNode;
                    moveStack.push(facing.getOpposite());
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
                firstNode = getNodeAt(currentPos);
            } else break;
        }
        return observedSet;
    }

    //called when node is removed to rebuild network
    protected void rebuildNetworkOnNodeRemoval(BlockPos nodePos, AMHSRailNode selfNode) {
        int amountOfConnectedSides = 0;
        for (Direction facing : VALUES) {
            BlockPos offsetPos = nodePos.relative(facing);
            if (containsNode(offsetPos))
                amountOfConnectedSides++;
        }
        //if we are connected only on one side or not connected at all, we don't need to find connected blocks
        //because they are only on on side or doesn't exist at all
        //this saves a lot of performance in big networks, which are quite big to depth-first them fastly
        if (amountOfConnectedSides >= 2) {
            for (Direction facing : VALUES) {
                BlockPos offsetPos = nodePos.relative(facing);
                AMHSRailNode secondNode = getNodeAt(offsetPos);
                if (secondNode == null || !canNodesConnected(selfNode, facing, secondNode, this)) {
                    //if there isn't any neighbour node, or it wasn't connected with us, just skip it
                    continue;
                }
                HashMap<BlockPos, AMHSRailNode> thisENet = findAllConnectedBlocks(offsetPos);
                if (getAllNodes().equals(thisENet)) {
                    //if cable on some direction contains all nodes of this network
                    //the network didn't change so keep it as is
                    break;
                } else {
                    //and use them to create new network with caching active nodes set
                    AMHSRailNet energyNet = worldData.createNetInstance();
                    //remove blocks that aren't connected with this network
                    thisENet.keySet().forEach(this::removeNodeWithoutRebuilding);
                    energyNet.transferNodeData(thisENet, this);
                    worldData.addRailNet(energyNet);
                }
            }
        }
        if (getAllNodes().isEmpty()) {
            //if this energy net is empty now, remove it
            worldData.removeRailNet(this);
        }
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    protected void transferNodeData(Map<BlockPos, AMHSRailNode> transferredNodes, AMHSRailNet parentNet) {
        transferredNodes.forEach(this::addNodeSilently);
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.put("Nodes", serializeAllNodeList(nodeByBlockPos));
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.nodeByBlockPos.clear();
        this.ownedChunks.clear();
        this.foupNodes.clear();
        deserializeAllNodeList(nbt.getCompound("Nodes"));
    }

    protected void deserializeAllNodeList(CompoundTag compound) {
        ListTag allNodesList = compound.getList("NodeIndexes", Tag.TAG_COMPOUND);

        for (int i = 0; i < allNodesList.size(); i++) {
            CompoundTag nodeTag = allNodesList.getCompound(i);
            var x = nodeTag.getInt("x");
            var y = nodeTag.getInt("y");
            var z = nodeTag.getInt("z");
            var blockPos = new BlockPos(x, y, z);
            var type = AMHSRailType.values()[nodeTag.getByte("type")];
            var connection = RailConnection.values()[nodeTag.getByte("con")];
            var direction = Direction.values()[nodeTag.getByte("dir")];
            var mark = nodeTag.getInt("mark");
            var isNodeActive = nodeTag.getBoolean("active");
            addNodeSilently(blockPos, new AMHSRailNode(blockPos, type, connection, direction, mark, isNodeActive));
        }
    }

    protected CompoundTag serializeAllNodeList(Map<BlockPos, AMHSRailNode> allNodes) {
        var compound = new CompoundTag();
        var allNodesList = new ListTag();

        for (Map.Entry<BlockPos, AMHSRailNode> entry : allNodes.entrySet()) {
            var nodePos = entry.getKey();
            var node = entry.getValue();
            var nodeTag = new CompoundTag();
            nodeTag.putInt("x", nodePos.getX());
            nodeTag.putInt("y", nodePos.getY());
            nodeTag.putInt("z", nodePos.getZ());
            if (node.mark != Node.DEFAULT_MARK) {
                nodeTag.putInt("mark", node.mark);
            }
            nodeTag.putByte("type", (byte) node.railType.ordinal());
            nodeTag.putByte("con", (byte) node.connection.ordinal());
            nodeTag.putByte("dir", (byte) node.direction.ordinal());
            if (node.isActive) {
                nodeTag.putBoolean("active", true);
            }
            allNodesList.add(nodeTag);
        }

        compound.put("NodeIndexes", allNodesList);
        return compound;
    }

    //////////////////////////////////////
    //********       GRAPH      ********//
    //////////////////////////////////////
    private long lastUpdate;
    private final Set<BlockPos> occupiedNodes = new HashSet<>();
    private final Set<FOUPCartEntity> carts = new HashSet<>();

    public boolean containCart(FOUPCartEntity cart) {
        return carts.contains(cart);
    }

    public void addCart(FOUPCartEntity cart) {
        carts.add(cart);
    }

    public void removeCart(FOUPCartEntity cart) {
        carts.remove(cart);
    }

    public boolean isCartValid(FOUPCartEntity cart) {
        return !cart.isRemoved() && containsNode(cart.getOnPos());
    }

    private void updateTick() {
        var latestTime = getWorldData().getWorld().getGameTime();
        if (lastUpdate != latestTime) {
            occupiedNodes.clear();
            val iterator = carts.iterator();
            while (iterator.hasNext()) {
                val cart = iterator.next();
                if (cart.isRemoved()) {
                    iterator.remove();
                } else if (!cart.isAwaited()){
                    occupiedNodes.add(cart.getOnPos());
                }
            }
        }
        lastUpdate = latestTime;
    }

    public boolean isNodeOccupied(BlockPos pos) {
        updateTick();
        return occupiedNodes.contains(pos);
    }

    public boolean occupyNode(BlockPos pos) {
        updateTick();
        return occupiedNodes.add(pos);
    }

    public boolean isGraphNode(AMHSRailNode node) {
        if (node.railType != AMHSRailType.NORMAL) return true;
        return node.connection.getInDirections(node.direction).size() > 1 || node.connection.getOutDirections(node.direction).size() > 1;
    }

    public Network<AMHSRailNode, RailGraphEdge> getGraph() {
        if (graph == null) {
            this.graph = NetworkBuilder.directed().build();
            for (var node : getAllNodes().values()) {
                if (isGraphNode(node)) {
                    graph.addNode(node);
                }
            }
            // build graph
            for (var from : graph.nodes()) {
                for (var outDirection : from.connection.getOutDirections(from.direction)) {
                    var path = new LinkedHashSet<AMHSRailNode>();
                    var to = getNodeAt(from.pos.relative(outDirection));
                    if (to != null) {
                        to = buildPath(graph, to, path);
                        if (to != null && to != from) {
                            var edge = new RailGraphEdge(from, to, path);
                            for (AMHSRailNode node : path) {
                                node.setEdge(edge);
                            }
                            graph.addEdge(from, to, edge);
                        }
                    }
                }
            }
        }
        return graph;
    }

    @Nullable
    protected AMHSRailNode buildPath(Network<AMHSRailNode, RailGraphEdge> graph, AMHSRailNode to, LinkedHashSet<AMHSRailNode> path) {
        while (!graph.nodes().contains(to)) {
            path.add(to);
            for (var outDirection : to.connection.getOutDirections(to.direction)) {
                to = getNodeAt(to.pos.relative(outDirection));
                break;
            }
            if (to == null) {
                path.clear();
                return null;
            }
        }
        return to;
    }

    public List<AMHSRailNode> routePath(AMHSRailNode from, AMHSRailNode to) {
        if (from.equals(to)) return List.of(from);
        var graph = getGraph();
        // if nodes included in graph
        if (graph.nodes().contains(from) && graph.nodes().contains(to)) {
            return routeGraphPath(from, to);
        }

        AMHSRailNode fromNode;
        List<AMHSRailNode> result = new ArrayList<>();
        if (graph.nodes().contains(from)) {
            fromNode = from;
        } else if (from.getEdge() != null) { // if FROM is in an edge.
            var edge = from.getEdge();
            fromNode = edge.to();
            var foundFrom = false;
            for (var node : edge.path()) {
                if (node == to) {
                    if (foundFrom) {
                        result.add(node);
                        return result;
                    }
                } else if (node == from) {
                    foundFrom = true;
                    result.add(node);
                } else if (foundFrom) {
                    result.add(node);
                }
            }
        } else { // no edge found, it is a unreachable node
            return Collections.emptyList();
        }

        if (graph.nodes().contains(to)) {
            if (fromNode.equals(to)) {
                result.add(to);
            } else {
                var route = routeGraphPath(fromNode, to);
                if (route.isEmpty()) return route;
                result.addAll(route);
            }
            return result;
        } else if (to.getEdge() != null){
            var edge = to.getEdge();
            var route = routeGraphPath(fromNode, to);
            if (route.isEmpty()) return route;
            result.addAll(route);
            for (var node : edge.path()) {
                result.add(node);
                if (node == to) break;
            }
            return result;
        } else { // no edge found, it is a unreachable node
            return Collections.emptyList();
        }
    }

    private List<AMHSRailNode> routeGraphPath(AMHSRailNode from, AMHSRailNode to) {
        var graph = getGraph();
        if (from.equals(to)) return List.of(from);
        if (graph.nodes().contains(from) && graph.nodes().contains(to)) {
            return routeGraphCache.computeIfAbsent(Pair.of(from, to), key -> new AStar(graph).search(key.first(), key.second()));
        }
        return Collections.emptyList();
    }

    public List<AMHSRailNode> getFreeFoupRails() {
        List<AMHSRailNode> result = new ArrayList<>();
        for (var pos : foupNodes) {
            var node = getNodeAt(pos);
            if (node != null) {
                var isUsed = false;
                for (FOUPCartEntity cart : carts) {
                    if (pos.equals(cart.getAwaitedPos().orElse(null))) {
                        isUsed = true;
                        break;
                    }
                }
                if (!isUsed) {
                    result.add(node);
                }
            }
        }
        return result;
    }
}
