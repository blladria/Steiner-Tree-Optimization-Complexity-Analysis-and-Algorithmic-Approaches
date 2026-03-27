package approximation;

import java.util.*;

/**
 * Kruskal's algorithm for Minimum Spanning Tree computation.
 * Uses Union-Find data structure for efficient cycle detection.
 */
public class KruskalMST {
    
    /**
     * Union-Find (Disjoint Set) data structure for Kruskal's algorithm.
     */
    public static class UnionFind {
        private final int[] parent;
        private final int[] rank;
        
        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }
        
        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }
        
        public boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            
            if (rootX == rootY) return false; // Already in same set
            
            // Union by rank
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            
            return true;
        }
    }
    
    /**
     * Result container for MST computation.
     */
    public static class Result {
        public final int cost;
        public final List<Graph.Edge> edges;
        
        public Result(int cost, List<Graph.Edge> edges) {
            this.cost = cost;
            this.edges = edges;
        }
    }
    
    /**
     * Compute MST using Kruskal's algorithm.
     * 
     * @param graph The input graph
     * @param activeVertices Boolean array indicating which vertices are included in the MST
     * @return MST result with cost and edges
     */
    public static Result compute(Graph graph, boolean[] activeVertices) {
        // Create vertex mapping for active vertices only
        int[] vertexMap = new int[graph.getVertexCount()];
        int activeCount = 0;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (activeVertices[i]) {
                vertexMap[i] = activeCount++;
            }
        }
        
        UnionFind uf = new UnionFind(activeCount);
        List<Graph.Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;
        
        // Get all edges and sort by weight
        List<Graph.Edge> allEdges = graph.getAllEdges();
        allEdges.sort(Comparator.comparingInt(e -> e.weight));
        
        // Process edges in order of increasing weight
        for (Graph.Edge edge : allEdges) {
            if (!activeVertices[edge.from] || !activeVertices[edge.to]) {
                continue; // Skip edges with inactive vertices
            }
            
            int mappedFrom = vertexMap[edge.from];
            int mappedTo = vertexMap[edge.to];
            
            if (uf.union(mappedFrom, mappedTo)) {
                mstEdges.add(edge);
                totalCost += edge.weight;
                
                // Stop when we have activeCount - 1 edges (MST is complete)
                if (mstEdges.size() == activeCount - 1) {
                    break;
                }
            }
        }
        
        return new Result(totalCost, mstEdges);
    }
    
    /**
     * Compute MST for all vertices in the graph.
     */
    public static Result compute(Graph graph) {
        boolean[] allActive = new boolean[graph.getVertexCount()];
        Arrays.fill(allActive, true);
        return compute(graph, allActive);
    }
}
