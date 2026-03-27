package exact;

import java.util.*;

/**
 * Exact solver for the Steiner Tree Problem on small graphs.
 * 
 * Strategy: enumerate every subset of non-terminal (Steiner) vertices,
 * compute the MST of the induced subgraph that contains all terminals plus
 * the current Steiner subset, and track the minimum cost found.
 * 
 * Complexity: O(2^|S| * (E + V) * alpha(V)) where |S| is the number of
 * optional Steiner vertices. This is only tractable for small instances,
 * which is the intended scope (|T| <= ~15, |V| <= ~30).
 */
public class Main {

    /**
     * Result container for the Steiner Tree solution.
     */
    public static class Result {
        public final int cost;
        public final List<int[]> edges; // each entry: {u, v, weight}

        public Result(int cost, List<int[]> edges) {
            this.cost = cost;
            this.edges = edges;
        }
    }

    /**
     * Solves the Steiner Tree Problem exactly by exhaustive subset enumeration.
     *
     * @param graph        The input graph
     * @param terminals    Set of required terminal vertex indices
     * @param steinerPool  Vertices that may optionally be included as Steiner points
     * @return a Result containing the minimum cost and the edges of the optimal tree
     */
    public static Result solve(Graph graph, int[] terminals, int[] steinerPool) {
        int poolSize = steinerPool.length;
        int bestCost = Integer.MAX_VALUE;
        List<int[]> bestEdges = null;

        // Iterate over all 2^|steinerPool| subsets (including the empty subset)
        for (int mask = 0; mask < (1 << poolSize); mask++) {
            boolean[] active = new boolean[graph.getVertexCount()];

            // Always activate all terminals
            for (int t : terminals) active[t] = true;

            // Activate only the Steiner vertices selected by this bitmask
            for (int bit = 0; bit < poolSize; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    active[steinerPool[bit]] = true;
                }
            }

            // Compute MST for this specific combination
            KruskalMST.Result mstResult = KruskalMST.compute(graph, active);

            if (mstResult.cost < bestCost) {
                bestCost = mstResult.cost;
                bestEdges = mstResult.edges;
            }
        }

        return new Result(bestCost, bestEdges);
    }

    /**
     * Main method that reads a graph from a file and solves the Steiner Tree problem.
     * Usage: java exact.Main <graph_file>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java exact.Main <graph_file>");
            System.err.println("Example: java exact.Main ../instances/example.txt");
            return;
        }

        try {
            String filename = args[0];
            System.out.println("Reading graph from: " + filename);
            
            long startTime = System.nanoTime();
            GraphParser.ParsedGraph parsed = GraphParser.parse(filename);
            long parseTime = System.nanoTime();

            Result result = solve(parsed.graph, parsed.terminals, parsed.steinerPool);
            long endTime = System.nanoTime();

            double parseMs = (parseTime - startTime) / 1_000_000.0;
            double solveMs = (endTime - parseTime) / 1_000_000.0;
            double totalMs = (endTime - startTime) / 1_000_000.0;

            System.out.println("\n=== Steiner Tree Problem — Exact Solver ===");
            System.out.println("Graph vertices: " + parsed.graph.getVertexCount());
            System.out.println("Terminals   : " + Arrays.toString(parsed.terminals));
            System.out.println("Steiner pool: " + Arrays.toString(parsed.steinerPool));
            System.out.println();

            if (result.cost == Integer.MAX_VALUE) {
                System.out.println("No feasible Steiner tree found (graph may be disconnected).");
            } else {
                System.out.println("Minimum Steiner tree cost: " + result.cost);
                System.out.println("Edges in optimal tree:");
                for (int[] e : result.edges) {
                    System.out.printf("  %d -- %d  (weight %d)%n", e[0], e[1], e[2]);
                }
            }

            System.out.printf("%nTiming:%n");
            System.out.printf("  Parse time:   %.3f ms%n", parseMs);
            System.out.printf("  Solve time:   %.3f ms%n", solveMs);
            System.out.printf("  Total time:   %.3f ms%n", totalMs);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
