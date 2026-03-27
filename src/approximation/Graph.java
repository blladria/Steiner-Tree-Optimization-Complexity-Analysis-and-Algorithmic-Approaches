package approximation;

import java.util.*;

/**
 * Graph data structure for Steiner Tree approximation algorithms.
 * Represents an undirected weighted graph with adjacency lists.
 */
public class Graph {
    private final int vertexCount;
    private final List<List<Edge>> adjacencyList;
    
    /**
     * Edge representation for undirected weighted graphs.
     */
    public static class Edge {
        public final int from;
        public final int to;
        public final int weight;
        
        public Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
        
        @Override
        public String toString() {
            return String.format("%d -- %d (weight %d)", from, to, weight);
        }
    }
    
    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adjacencyList = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }
    
    public int getVertexCount() {
        return vertexCount;
    }
    
    public void addEdge(int from, int to, int weight) {
        adjacencyList.get(from).add(new Edge(from, to, weight));
        adjacencyList.get(to).add(new Edge(to, from, weight));
    }
    
    public List<Edge> getAdjacentEdges(int vertex) {
        return new ArrayList<>(adjacencyList.get(vertex));
    }
    
    /**
     * Get all edges in the graph (each undirected edge appears once).
     */
    public List<Edge> getAllEdges() {
        Set<String> seen = new HashSet<>();
        List<Edge> edges = new ArrayList<>();
        
        for (int u = 0; u < vertexCount; u++) {
            for (Edge e : adjacencyList.get(u)) {
                String edgeKey = Math.min(e.from, e.to) + "-" + Math.max(e.from, e.to);
                if (!seen.contains(edgeKey)) {
                    seen.add(edgeKey);
                    edges.add(e);
                }
            }
        }
        return edges;
    }
}
