package approximation;

import java.util.*;

/**
 * Dijkstra's algorithm implementation for shortest path computations.
 * Used to compute all-pairs shortest paths between terminal vertices.
 */
public class Dijkstra {
    
    /**
     * Compute shortest paths from a single source vertex to all other vertices.
     * 
     * @param graph The input graph
     * @param source Source vertex
     * @return Array of distances, where distances[i] is the shortest distance from source to i
     */
    public static int[] shortestPaths(Graph graph, int source) {
        int vertexCount = graph.getVertexCount();
        int[] distances = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];
        
        // Initialize distances
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[source] = 0;
        
        // Priority queue: (distance, vertex)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, source});
        
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int dist = current[0];
            int vertex = current[1];
            
            // Skip if we've already found a better path
            if (visited[vertex]) continue;
            visited[vertex] = true;
            
            // Relax edges
            for (Graph.Edge edge : graph.getAdjacentEdges(vertex)) {
                int neighbor = edge.to;
                int newDist = dist + edge.weight;
                
                if (newDist < distances[neighbor]) {
                    distances[neighbor] = newDist;
                    pq.offer(new int[]{newDist, neighbor});
                }
            }
        }
        
        return distances;
    }
    
    /**
     * Compute all-pairs shortest paths between specified vertices.
     * 
     * @param graph The input graph
     * @param vertices Array of vertices to compute distances between
     * @return 2D array where distances[i][j] is the shortest distance between vertices[i] and vertices[j]
     */
    public static int[][] allPairsShortestPaths(Graph graph, int[] vertices) {
        int n = vertices.length;
        int[][] distances = new int[n][n];
        
        // Compute shortest paths from each vertex
        for (int i = 0; i < n; i++) {
            int[] fromSource = shortestPaths(graph, vertices[i]);
            for (int j = 0; j < n; j++) {
                distances[i][j] = fromSource[vertices[j]];
            }
        }
        
        return distances;
    }
}
