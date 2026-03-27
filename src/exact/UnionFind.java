package exact;

/**
 * Union-Find (Disjoint Set Union) data structure with path compression
 * and union by rank. Used by Kruskal's MST algorithm.
 */
public class UnionFind {
    private final int[] parent;
    private final int[] rank;

    public UnionFind(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }

    /**
     * Merges the sets containing a and b.
     * @return true if the sets were distinct and have now been merged.
     */
    public boolean union(int a, int b) {
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) return false;

        // Union by rank
        if (rank[ra] < rank[rb]) {
            parent[ra] = rb;
        } else if (rank[ra] > rank[rb]) {
            parent[rb] = ra;
        } else {
            parent[rb] = ra;
            rank[ra]++;
        }
        return true;
    }
}
