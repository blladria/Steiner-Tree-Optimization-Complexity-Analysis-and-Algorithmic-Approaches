package approximation;

import java.io.*;

/**
 * Parser for graph instances in the same format as the exact solver.
 * Format:
 * - First line: number of vertices
 * - Second line: comma-separated terminal vertices
 * - Third line: comma-separated Steiner pool vertices (optional, can be empty)
 * - Remaining lines: edges in format "u v weight"
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
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            
            // Parse vertex count (skip comments)
            line = reader.readLine();
            lineNumber++;
            while (line != null && (line.trim().isEmpty() || line.trim().startsWith("#"))) {
                line = reader.readLine();
                lineNumber++;
            }
            if (line == null) throw new IOException("Empty file");
            int vertexCount = Integer.parseInt(line.trim());
            
            // Parse terminals (skip comments)
            line = reader.readLine();
            lineNumber++;
            while (line != null && (line.trim().isEmpty() || line.trim().startsWith("#"))) {
                line = reader.readLine();
                lineNumber++;
            }
            if (line == null) throw new IOException("Missing terminals line");
            int[] terminals = parseVertexList(line.trim());
            
            // Parse Steiner pool (may be empty, skip comments)
            line = reader.readLine();
            lineNumber++;
            while (line != null && (line.trim().isEmpty() || line.trim().startsWith("#"))) {
                line = reader.readLine();
                lineNumber++;
            }
            int[] steinerPool;
            if (line == null || line.trim().isEmpty()) {
                steinerPool = new int[0];
            } else {
                steinerPool = parseVertexList(line.trim());
            }
            
            // Create graph
            Graph graph = new Graph(vertexCount);
            
            // Parse edges
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Invalid edge format at line " + lineNumber + ": " + line);
                }
                
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int weight = Integer.parseInt(parts[2]);
                
                if (u < 0 || u >= vertexCount || v < 0 || v >= vertexCount) {
                    throw new IOException("Invalid vertex index at line " + lineNumber + ": " + line);
                }
                
                graph.addEdge(u, v, weight);
            }
            
            return new ParsedGraph(graph, terminals, steinerPool);
        }
    }
    
    private static int[] parseVertexList(String line) {
        if (line.isEmpty()) return new int[0];
        
        String[] parts = line.split(",");
        int[] vertices = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vertices[i] = Integer.parseInt(parts[i].trim());
        }
        return vertices;
    }
}
