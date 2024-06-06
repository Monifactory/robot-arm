package screret.robotarm.pipenet.amhs.astar;

import screret.robotarm.pipenet.amhs.AMHSRailNode;

public class Node implements Comparable<Node> {
    // Id for readability of result purposes
    public AMHSRailNode value;
    // Parent in the path
    public Node parent = null;
    // Evaluation functions
    public double f = Double.MAX_VALUE;
    public double g = Double.MAX_VALUE;
    // Hardcoded heuristic
    public double h;

    public Node(AMHSRailNode value) {
        this.value = value;
    }

    @Override
    public int compareTo(Node n) {
        return Double.compare(this.f, n.f);
    }

    public double calculateHeuristic(Node target) {
        return Math.abs(this.value.pos.getX() - target.value.pos.getX()) + Math.abs(this.value.pos.getZ() - target.value.pos.getZ());
    }
}
