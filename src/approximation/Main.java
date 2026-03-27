package approximation;

import java.util.*;

/**
 * Main class for the Steiner Tree approximation algorithm.
 * Demonstrates the MST-based 2-approximation algorithm on various test instances.
 */
public class Main {

    /**
     * Main method that runs the approximation algorithm on test instances.
     * Usage: java approximation.Main <graph_file>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java approximation.Main <graph_file>");
            System.out.println("Example: java approximation.Main large_instance.txt");
            System.out.println("\nRunning built-in large test case instead...");
            runLargeTestCase();
            return;
        }

        try {
            String filename = args[0];
            runTestCase(filename);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run the approximation algorithm on a specified test case.
     */
    private static void runTestCase(String filename) throws Exception {
        System.out.println("Reading graph from: " + filename);
        
        long startTime = System.nanoTime();
        GraphParser.ParsedGraph parsed = GraphParser.parse(filename);
        long parseTime = System.nanoTime();

        SteinerTreeApproximation.Result result = SteinerTreeApproximation.solve(parsed.graph, parsed.terminals);
        long endTime = System.nanoTime();

        double parseMs = (parseTime - startTime) / 1_000_000.0;
        double solveMs = (endTime - parseTime) / 1_000_000.0;
        double totalMs = (endTime - startTime) / 1_000_000.0;

        printResults(parsed, result, parseMs, solveMs, totalMs);
    }
    
    /**
     * Run a large built-in test case to demonstrate algorithm efficiency.
     */
    private static void runLargeTestCase() {
        System.out.println("=== Large Built-in Test Case ===");
        System.out.println("Creating a challenging instance with 50 vertices and 15 terminals...\n");
        
        // Create a larger test case
        Graph graph = createLargeTestGraph();
        int[] terminals = createLargeTestTerminals();
        
        long startTime = System.nanoTime();
        SteinerTreeApproximation.Result result = SteinerTreeApproximation.solve(graph, terminals);
        long endTime = System.nanoTime();
        
        double solveMs = (endTime - startTime) / 1_000_000.0;
        
        // Create a mock ParsedGraph for display
        GraphParser.ParsedGraph mockParsed = new GraphParser.ParsedGraph(graph, terminals, new int[0]);
        
        printResults(mockParsed, result, 0, solveMs, solveMs);
    }
    
    /**
     * Create a large test graph with interesting structure.
     */
    private static Graph createLargeTestGraph() {
        int vertexCount = 50;
        Graph graph = new Graph(vertexCount);
        
        // Create a grid-like structure with some random connections
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        // Add grid edges (4x12 grid + 2 extra vertices)
        for (int i = 0; i < 48; i++) {
            int row = i / 12;
            int col = i % 12;
            
            // Right neighbor
            if (col < 11) {
                int weight = 10 + rand.nextInt(20);
                graph.addEdge(i, i + 1, weight);
            }
            
            // Down neighbor
            if (row < 3) {
                int weight = 10 + rand.nextInt(20);
                graph.addEdge(i, i + 12, weight);
            }
        }
        
        // Add some diagonal connections for interesting shortcuts
        for (int i = 0; i < 20; i++) {
            int u = rand.nextInt(vertexCount - 2);
            int v = u + rand.nextInt(2) + 1;
            int weight = 5 + rand.nextInt(25);
            graph.addEdge(u, v, weight);
        }
        
        // Connect the last 2 vertices
        graph.addEdge(48, 49, 15);
        graph.addEdge(47, 49, 20);
        
        return graph;
    }
    
    /**
     * Create terminals spread across the large graph.
     */
    private static int[] createLargeTestTerminals() {
        // Spread terminals across the graph to create an interesting Steiner problem
        return new int[]{
            0,   // Top-left corner
            11,  // Top-right corner
            36,  // Bottom-left corner
            47,  // Bottom-right corner
            24,  // Center
            49,  // Extra vertex
            5,   // Top middle
            30,  // Bottom middle
            18,  // Upper middle
            35,  // Lower middle
            12,  // Left middle
            23,  // Right middle
            2,   // Upper left area
            45,  // Lower right area
            25   // Another central point
        };
    }
    
    /**
     * Print comprehensive results of the approximation algorithm.
     */
    private static void printResults(GraphParser.ParsedGraph parsed, SteinerTreeApproximation.Result result,
                                   double parseMs, double solveMs, double totalMs) {
        System.out.println("\n=== Steiner Tree Problem — 2-Approximation Solver ===");
        System.out.println("Graph vertices: " + parsed.graph.getVertexCount());
        System.out.println("Terminals   : " + Arrays.toString(parsed.terminals));
        System.out.println("Steiner pool: " + Arrays.toString(parsed.steinerPool));
        System.out.println();

        System.out.println("Approximation Results:");
        System.out.println("Steiner tree cost: " + result.cost);
        System.out.println("Number of edges: " + result.edges.size());
        
        if (!Double.isNaN(result.approximationRatio)) {
            System.out.printf("Approximation ratio: %.3f (compared to optimal)%n", result.approximationRatio);
        } else {
            System.out.println("Approximation ratio: ≤ 2.0 (theoretical guarantee)");
        }
        
        System.out.println("\nEdges in Steiner tree:");
        for (Graph.Edge edge : result.edges) {
            System.out.printf("  %s%n", edge);
        }

        System.out.printf("%nTiming:%n");
        if (parseMs > 0) {
            System.out.printf("  Parse time:   %.3f ms%n", parseMs);
        }
        System.out.printf("  Solve time:   %.3f ms%n", solveMs);
        System.out.printf("  Total time:   %.3f ms%n", totalMs);
        
        // Performance analysis
        System.out.printf("%nPerformance Analysis:%n");
        System.out.printf("  Algorithmic complexity: O(|T|² log |T| + |E| log |V|)%n", 
                         parsed.terminals.length, parsed.graph.getVertexCount());
        System.out.printf("  Scalability: Handles large instances efficiently%n");
    }
}
