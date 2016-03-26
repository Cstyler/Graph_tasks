import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Created by khan on 13.03.16. Kruskal
 */

class Kruskal {

    public static void main(String[] args) {
        System.out.printf("%.2f\n", MST_Kruskal(scanInput()));
    }

    private static Vertex[] scanInput() {
        Scanner scn = new Scanner(System.in);
        final int n = scn.nextInt();
        Vertex[] vertices = new Vertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new Vertex(scn.nextInt(), scn.nextInt());
        }
        return vertices;
    }

    private static PriorityQueue<Edge> calcDists(Vertex[] vertices) {
        int n = vertices.length;
        PriorityQueue<Edge> queue = new PriorityQueue<>((n * (n - 1)) / 2 + 1, (a, b) -> Double.compare(a.getDist(), b.getDist()));
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                queue.add(new Edge(i, j, vertices[i].calcDist(vertices[j])));
            }
        }
        return queue;
    }

    private static double MST_Kruskal(Vertex[] vertices) {
        PriorityQueue<Edge> edges = calcDists(vertices);
        ArrayList<Element<Integer>> elements = new ArrayList<>(vertices.length);
        for (Vertex v : vertices) {
            elements.add(new Element<>());
        }
        int edgeCount = 0;
        double dist = 0.0;
        while (!edges.isEmpty() && edgeCount < vertices.length - 1) {
            Edge q = edges.poll();
            Element<Integer> edge1 = elements.get(q.getI()), edge2 = elements.get(q.getJ());
            if (!edge1.equivalent(edge2)) {
                dist += q.getDist();
                edge1.union(edge2);
                edgeCount++;
            }
        }
        return dist;
    }
}

class Vertex {
    private final double x, y;

    Vertex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    double calcDist(Vertex v) {
        return Math.sqrt(Math.pow(this.x - v.x, 2.0) + Math.pow(this.y - v.y, 2.0));
    }
}

class Edge {
    private final int i, j;
    private final double dist;

    Edge(int i, int j, double dist) {
        this.i = i;
        this.j = j;
        this.dist = dist;
    }

    int getI() {
        return i;
    }

    int getJ() {
        return j;
    }

    double getDist() {
        return dist;
    }
}

class Element<T> {
    private Element<T> parent;
    private int depth;

    Element() {
        parent = this;
        depth = 0;
    }

    boolean equivalent(Element<T> elem) {
        return find(this) == find(elem);
    }

    private Element<T> find(Element<T> x) {
        if (x.parent == x)
            return x;
        else
            return x.parent = find(x.parent);
    }

    void union(Element<T> elem) {
        Element<T> rootX = find(this);
        Element<T> rootY = find(elem);
        if (rootX.depth < rootY.depth) {
            rootX.parent = rootY;
        } else {
            rootY.parent = rootX;
            if (rootX.depth == rootY.depth && rootX != rootY) {
                rootX.depth++;
            }
        }
    }
}