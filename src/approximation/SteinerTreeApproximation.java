package approximation;

import java.util.*;

/**
 * Implementation of the MST-based 2-approximation algorithm for the Steiner Tree Problem.
 * 
 * Algorithm steps:
 * 1. Compute all-pairs shortest paths between terminal nodes
 * 2. Construct metric closure graph on terminal nodes
 * 3. Find MST of the closure graph
 * 4. Map MST edges back to shortest paths in original graph
 * 5. Compute MST of resulting subgraph and prune non-terminal leaves
 */
public class SteinerTreeApproximation {
    
    /**
     * Result container for the Steiner Tree approximation.
     */
    public static class Result {
        public final int cost;
        public final List<Graph.Edge> edges;
        public final double approximationRatio; // Compared to optimal (if known)
        
        public Result(int cost, List<Graph.Edge> edges) {
            this.cost = cost;
            this.edges = edges;
            this.approximationRatio = Double.NaN; // Unknown optimal cost
        }
        
        public Result(int cost, List<Graph.Edge> edges, int optimalCost) {
            this.cost = cost;
            this.edges = edges;
            this.approximationRatio = (double) cost / optimalCost;
        }
    }
    
    /**
     * Solve the Steiner Tree Problem using the MST-based 2-approximation algorithm.
     * 
     * @param graph The input graph
     * @param terminals Array of terminal vertex indices
     * @return Approximate Steiner tree solution
     */
    public static Result solve(Graph graph, int[] terminals) {
        // Step 1: Compute all-pairs shortest paths between terminals
        int[][] terminalDistances = Dijkstra.allPairsShortestPaths(graph, terminals);
        
        // Step 2: Construct metric closure graph on terminals
        Graph closureGraph = buildMetricClosure(terminals.length, terminalDistances);
        
        // Step 3: Find MST of the closure graph
        KruskalMST.Result closureMST = KruskalMST.compute(closureGraph);
        
        // Step 4: Map MST edges back to shortest paths in original graph
        Set<Graph.Edge> subgraphEdges = mapMSTToOriginalGraph(graph, terminals, terminalDistances, closureMST.edges);
        
        // Step 5: Build subgraph and compute its MST
        Graph subgraph = buildSubgraph(graph, subgraphEdges);
        boolean[] activeVertices = determineActiveVertices(subgraph, terminals);
        KruskalMST.Result finalMST = KruskalMST.compute(subgraph, activeVertices);
        
        // Step 6: Prune non-terminal leaves
        List<Graph.Edge> prunedEdges = pruneNonTerminalLeaves(subgraph, finalMST.edges, terminals);
        
        int finalCost = calculateTotalCost(prunedEdges);
        return new Result(finalCost, prunedEdges);
    }
    
    /**
     * Build the metric closure graph on terminal vertices.
     * The closure graph is a complete graph where edge weights are shortest path distances.
     */
    private static Graph buildMetricClosure(int terminalCount, int[][] distances) {
        Graph closure = new Graph(terminalCount);
        
        for (int i = 0; i < terminalCount; i++) {
            for (int j = i + 1; j < terminalCount; j++) {
                closure.addEdge(i, j, distances[i][j]);
            }
        }
        
        return closure;
    }
    
    /**
     * Map MST edges from the closure graph back to corresponding shortest paths in the original graph.
     * This is the critical step that connects the abstract solution to the concrete graph structure.
     */
    private static Set<Graph.Edge> mapMSTToOriginalGraph(Graph originalGraph, int[] terminals, 
                                                         int[][] terminalDistances, List<Graph.Edge> mstEdges) {
        Set<Graph.Edge> mappedEdges = new HashSet<>();
        
        for (Graph.Edge mstEdge : mstEdges) {
            int terminalU = terminals[mstEdge.from];
            int terminalV = terminals[mstEdge.to];
            
            // Find actual shortest path between these terminals in the original graph
            List<Graph.Edge> shortestPath = findShortestPath(originalGraph, terminalU, terminalV);
            mappedEdges.addAll(shortestPath);
        }
        
        return mappedEdges;
    }
    
    /**
     * Find a shortest path between two vertices using Dijkstra's algorithm with path reconstruction.
     */
    private static List<Graph.Edge> findShortestPath(Graph graph, int source, int target) {
        int vertexCount = graph.getVertexCount();
        int[] distances = new int[vertexCount];
        int[] predecessors = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];
        
        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(predecessors, -1);
        distances[source] = 0;
        
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, source});
        
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int dist = current[0];
            int vertex = current[1];
            
            if (visited[vertex]) continue;
            visited[vertex] = true;
            
            if (vertex == target) break; // Found target
            
            for (Graph.Edge edge : graph.getAdjacentEdges(vertex)) {
                int neighbor = edge.to;
                int newDist = dist + edge.weight;
                
                if (newDist < distances[neighbor]) {
                    distances[neighbor] = newDist;
                    predecessors[neighbor] = vertex;
                    pq.offer(new int[]{newDist, neighbor});
                }
            }
        }
        
        // Reconstruct path
        List<Graph.Edge> pathEdges = new ArrayList<>();
        int current = target;
        
        while (current != source && predecessors[current] != -1) {
            int prev = predecessors[current];
            // Find the actual edge between prev and current
            for (Graph.Edge edge : graph.getAdjacentEdges(prev)) {
                if (edge.to == current) {
                    pathEdges.add(edge);
                    break;
                }
            }
            current = prev;
        }
        
        return pathEdges;
    }
    
    /**
     * Build a subgraph containing only the mapped edges.
     */
    private static Graph buildSubgraph(Graph originalGraph, Set<Graph.Edge> edges) {
        Graph subgraph = new Graph(originalGraph.getVertexCount());
        
        for (Graph.Edge edge : edges) {
            subgraph.addEdge(edge.from, edge.to, edge.weight);
        }
        
        return subgraph;
    }
    
    /**
     * Determine which vertices are active (connected) in the subgraph.
     */
    private static boolean[] determineActiveVertices(Graph graph, int[] terminals) {
        boolean[] active = new boolean[graph.getVertexCount()];
        
        // Start with all terminals as active
        for (int terminal : terminals) {
            active[terminal] = true;
        }
        
        // BFS/DFS to find all connected vertices
        Queue<Integer> queue = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        
        for (int terminal : terminals) {
            queue.offer(terminal);
            visited.add(terminal);
        }
        
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            
            for (Graph.Edge edge : graph.getAdjacentEdges(vertex)) {
                int neighbor = edge.to;
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    active[neighbor] = true;
                    queue.offer(neighbor);
                }
            }
        }
        
        return active;
    }
    
    /**
     * Remove non-terminal leaf vertices from the tree.
     * A leaf is a vertex with degree 1; we keep removing non-terminal leaves until none remain.
     */
    private static List<Graph.Edge> pruneNonTerminalLeaves(Graph graph, List<Graph.Edge> edges, int[] terminals) {
        Set<Integer> terminalSet = new HashSet<>();
        for (int terminal : terminals) {
            terminalSet.add(terminal);
        }
        
        // Build adjacency structure for the current tree
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        Set<Integer> vertices = new HashSet<>();
        
        for (Graph.Edge edge : edges) {
            vertices.add(edge.from);
            vertices.add(edge.to);
            
            adjacency.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge.to);
            adjacency.computeIfAbsent(edge.to, k -> new ArrayList<>()).add(edge.from);
        }
        
        // Iteratively remove non-terminal leaves
        Queue<Integer> leavesToRemove = new ArrayDeque<>();
        
        // Find initial non-terminal leaves
        for (int vertex : vertices) {
            if (!terminalSet.contains(vertex) && adjacency.get(vertex).size() == 1) {
                leavesToRemove.offer(vertex);
            }
        }
        
        while (!leavesToRemove.isEmpty()) {
            int leaf = leavesToRemove.poll();
            if (!terminalSet.contains(leaf) && adjacency.containsKey(leaf) && adjacency.get(leaf).size() == 1) {
                int neighbor = adjacency.get(leaf).get(0);
                
                // Remove leaf from adjacency
                adjacency.remove(leaf);
                adjacency.get(neighbor).remove(Integer.valueOf(leaf));
                
                // Check if neighbor became a non-terminal leaf
                if (!terminalSet.contains(neighbor) && adjacency.get(neighbor).size() == 1) {
                    leavesToRemove.offer(neighbor);
                }
            }
        }
        
        // Rebuild edge list from remaining vertices
        List<Graph.Edge> prunedEdges = new ArrayList<>();
        for (Graph.Edge edge : edges) {
            if (adjacency.containsKey(edge.from) && adjacency.containsKey(edge.to)) {
                prunedEdges.add(edge);
            }
        }
        
        return prunedEdges;
    }
    
    /**
     * Calculate total cost of a list of edges.
     */
    private static int calculateTotalCost(List<Graph.Edge> edges) {
        int total = 0;
        for (Graph.Edge edge : edges) {
            total += edge.weight;
        }
        return total;
    }
}
