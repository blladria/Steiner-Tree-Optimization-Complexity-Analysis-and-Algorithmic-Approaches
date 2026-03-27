package exact;

import java.util.*;

/**
 * Simple undirected weighted graph representation.
 */
public class Graph {
    private final int vertexCount;
    private final List<Edge>[] adjacency;

    @SuppressWarnings("unchecked")
    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adjacency = new List[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacency[i] = new ArrayList<>();
        }
    }

    public void addEdge(int u, int v, int weight) {
        adjacency[u].add(new Edge(v, weight));
        adjacency[v].add(new Edge(u, weight));
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public List<Edge> getNeighbors(int vertex) {
        return adjacency[vertex];
    }

    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        for (int u = 0; u < vertexCount; u++) {
            for (Edge e : adjacency[u]) {
                if (e.destination > u) { // Add each undirected edge once
                    edges.add(new Edge(u, e.destination, e.weight));
                }
            }
        }
        return edges;
    }

    /**
     * Returns all edges where both endpoints are in the active node set.
     */
    public List<Edge> getInternalEdges(boolean[] activeNodes) {
        List<Edge> edges = new ArrayList<>();
        for (int u = 0; u < vertexCount; u++) {
            if (!activeNodes[u]) continue;
            for (Edge e : adjacency[u]) {
                int v = e.destination;
                if (v > u && activeNodes[v]) { // Avoid duplicates
                    edges.add(new Edge(u, v, e.weight));
                }
            }
        }
        return edges;
    }

    /**
     * Immutable edge representation.
     */
    public static class Edge {
        public final int source;
        public final int destination;
        public final int weight;

        public Edge(int destination, int weight) {
            this.source = -1; // Unknown source
            this.destination = destination;
            this.weight = weight;
        }

        public Edge(int source, int destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
    }
}
