package exact;

import java.io.*;
import java.util.*;

/**
 * Parser for graph files in the following format:
 *
 * # Lines starting with # are comments and are ignored
 * # First non-comment line: number of vertices
 * # Second non-comment line: comma-separated terminal vertices
 * # Third non-comment line: comma-separated Steiner pool vertices (optional)
 * # Remaining lines: edges in format "u v weight"
 *
 * Example:
 * # Example graph with 7 vertices
 * 7
 * 0,1,4
 * 2,3,5,6
 * 0 2 3
 * 0 5 1
 * 1 2 2
 * 1 3 2
 * 2 5 4
 * 3 4 3
 * 4 6 1
 * 5 6 2
 */
public class GraphParser {

    public static class ParsedGraph {
        public final Graph graph;
        public final int[] terminals;
        public final int[] steinerPool;

        public ParsedGraph(Graph graph, int[] terminals, int[] steinerPool) {
            this.graph = graph;
            this.terminals = terminals;
            this.steinerPool = steinerPool;
        }
    }

    public static ParsedGraph parse(String filename) throws IOException {
        List<String> lines = readFile(filename);
        
        // Filter out comments and empty lines
        List<String> content = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                content.add(line);
            }
        }

        if (content.size() < 4) {
            throw new IllegalArgumentException("Invalid file format: need at least vertices, terminals, and one edge");
        }

        // Parse vertex count
        int vertexCount = Integer.parseInt(content.get(0));
        Graph graph = new Graph(vertexCount);

        // Parse terminals
        int[] terminals = parseVertexList(content.get(1));

        // Parse Steiner pool (optional - if empty, all non-terminals become Steiner pool)
        int[] steinerPool;
        if (content.get(2).trim().isEmpty()) {
            // All non-terminal vertices become Steiner pool
            Set<Integer> terminalSet = new HashSet<>();
            for (int t : terminals) terminalSet.add(t);
            List<Integer> steinerList = new ArrayList<>();
            for (int v = 0; v < vertexCount; v++) {
                if (!terminalSet.contains(v)) {
                    steinerList.add(v);
                }
            }
            steinerPool = steinerList.stream().mapToInt(Integer::intValue).toArray();
        } else {
            steinerPool = parseVertexList(content.get(2));
        }

        // Parse edges
        for (int i = 3; i < content.size(); i++) {
            String[] parts = content.get(i).split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid edge format: " + content.get(i));
            }
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            int weight = Integer.parseInt(parts[2]);
            
            if (u < 0 || u >= vertexCount || v < 0 || v >= vertexCount) {
                throw new IllegalArgumentException("Vertex out of range: " + u + ", " + v);
            }
            
            graph.addEdge(u, v, weight);
        }

        return new ParsedGraph(graph, terminals, steinerPool);
    }

    private static List<String> readFile(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static int[] parseVertexList(String line) {
        if (line.trim().isEmpty()) {
            return new int[0];
        }
        
        String[] parts = line.split(",");
        int[] vertices = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vertices[i] = Integer.parseInt(parts[i].trim());
        }
        return vertices;
    }
}
