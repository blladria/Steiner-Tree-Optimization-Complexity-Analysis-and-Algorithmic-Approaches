package exact;

import java.util.*;

/**
 * Kruskal's algorithm for Minimum Spanning Tree.
 * Works on a subset of vertices defined by the activeNodes array.
 */
public class KruskalMST {

    public static class Result {
        public final int cost;
        public final List<int[]> edges; // {u, v, weight}

        public Result(int cost, List<int[]> edges) {
            this.cost = cost;
            this.edges = edges;
        }
    }

    /**
     * Computes the MST cost for the subgraph induced by activeNodes.
     * 
     * @param graph The original graph
     * @param activeNodes Boolean array indicating which vertices are active
     * @return Result containing cost and edges, or cost=Integer.MAX_VALUE if disconnected
     */
    public static Result compute(Graph graph, boolean[] activeNodes) {
        // Collect all edges internal to the active node set
        List<Graph.Edge> edges = graph.getInternalEdges(activeNodes);
        
        // Sort edges by weight ascending
        edges.sort(Comparator.comparingInt(e -> e.weight));

        UnionFind uf = new UnionFind(graph.getVertexCount());

        int totalCost = 0;
        int edgesAdded = 0;
        List<int[]> mstEdges = new ArrayList<>();
        
        // Count active vertices
        int activeCount = 0;
        for (boolean active : activeNodes) {
            if (active) activeCount++;
        }

        // Kruskal's main loop
        for (Graph.Edge edge : edges) {
            int u = edge.source;
            int v = edge.destination;
            
            if (uf.union(u, v)) {
                totalCost += edge.weight;
                edgesAdded++;
                mstEdges.add(new int[]{u, v, edge.weight});
                if (edgesAdded == activeCount - 1) break; // MST is complete
            }
        }

        // If we couldn't add enough edges, the subgraph is disconnected
        if (edgesAdded < activeCount - 1) {
            return new Result(Integer.MAX_VALUE, Collections.emptyList());
        }

        return new Result(totalCost, mstEdges);
    }
}
