package screret.robotarm.pipenet.amhs.astar;

import com.google.common.graph.Network;
import screret.robotarm.pipenet.amhs.AMHSRailNode;
import screret.robotarm.pipenet.amhs.RailGraphEdge;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AStar {
    PriorityQueue<Node> closedList = new PriorityQueue<>();
    PriorityQueue<Node> openList = new PriorityQueue<>();
    Network<AMHSRailNode, RailGraphEdge> graph;
    Map<AMHSRailNode, Node> nodes = new HashMap<>();

    public AStar(Network<AMHSRailNode, RailGraphEdge> graph) {
        this.graph = graph;
    }

    public Node getGNode(AMHSRailNode node) {
        return nodes.computeIfAbsent(node, Node::new);
    }

    public List<AMHSRailNode> search(AMHSRailNode from, AMHSRailNode to){
        Node start = getGNode(from);
        start.g = 0;
        Node target = getGNode(to);
        start.f = start.g + start.calculateHeuristic(target);
        openList.add(start);

        while(!openList.isEmpty()){
            Node current = openList.peek();
            if(current == target){
                return buildPath(current);
            }

            for (var edge : graph.incidentEdges(current.value)) {
                if (edge.from() == current.value) {
                    var dest = getGNode(edge.to());
                    double totalWeight = current.g + edge.path().size() + 1;

                    if(!openList.contains(dest) && !closedList.contains(dest)){
                        dest.parent = current;
                        dest.g = totalWeight;
                        dest.f = dest.g + dest.calculateHeuristic(target);
                        openList.add(dest);
                    } else {
                        if(totalWeight < dest.g){
                            dest.parent = current;
                            dest.g = totalWeight;
                            dest.f = dest.g + dest.calculateHeuristic(target);

                            if(closedList.contains(dest)){
                                closedList.remove(dest);
                                openList.add(dest);
                            }
                        }
                    }
                }
            }

            openList.remove(current);
            closedList.add(current);
        }
        return Collections.emptyList();
    }

    public List<AMHSRailNode> buildPath(Node target){
        Node n = target;

        if(n == null)
            return Collections.emptyList();

        List<AMHSRailNode> path = new ArrayList<>();
        while(n.parent != null){
            path.add(0, n.value);
            var p = n.parent;
            RailGraphEdge edge = null;
            for (RailGraphEdge railGraphEdge : graph.edgesConnecting(p.value, n.value)) {
                if (edge == null || edge.path().size() > railGraphEdge.path().size()) {
                    edge = railGraphEdge;
                }
            }
            if (edge == null) {
                return Collections.emptyList();
            }
            path.addAll(0, edge.path());
            n = n.parent;
        }
        path.add(0, n.value);

        return path;
    }
}
