import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Created by khan on 18.03.16. Prim
 */

class Prim {
    public static void main(String[] args) {
        System.out.println(MST_Prim(scanInput()));
    }

    private static Vertex[] scanInput() {
        Scanner scn = new Scanner(System.in);
        final int n = scn.nextInt(), m = scn.nextInt();
        Vertex[] vertices = new Vertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new Vertex();
        }
        for (int i = 0; i < m; i++) {
            int a = scn.nextInt(), b = scn.nextInt(), dist = scn.nextInt();
            vertices[a].getEdges().add(new Edge(b, dist));
            if (b != a) {
                vertices[b].getEdges().add(new Edge(a, dist));
            }
        }
        return vertices;
    }

    private static int MST_Prim(Vertex[] vertices) {
        PriorityQueue<Vertex> queue = new PriorityQueue<>(vertices.length + 1, (a, b) -> a.getKey() - b.getKey());
        Vertex v = vertices[0];
        int result = 0;
        while (true) {
            v.setMark(Vertex.Marks.IN_TREE);
            for (Edge edge :
                    v.getEdges()) {
                Vertex u = vertices[edge.getVertex()];
                int a = edge.getKey();
                if (u.getMark() == Vertex.Marks.OUT_TREE) {
                    u.setKey(a);
                    u.setMark(Vertex.Marks.IN_QUEUE);
                    queue.add(u);
                } else if (u.getMark() == Vertex.Marks.IN_QUEUE && a < u.getKey()) {
                    queue.remove(u);
                    u.setKey(a);
                    queue.add(u);
                }
            }
            result += v.getKey();
            if (queue.isEmpty()) {
                break;
            }
            v = queue.poll();
        }
        return result;
    }
}

class Vertex {
    private final ArrayList<Edge> edges;
    private int key;
    private Marks mark;

    public enum Marks {IN_QUEUE, IN_TREE, OUT_TREE}

    public Vertex() {
        mark = Marks.OUT_TREE;
        key = 0;
        edges = new ArrayList<>(0);
    }

    public void setMark(Marks mark) {
        this.mark = mark;
    }

    public Marks getMark() {
        return mark;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return edges.toString();
    }
}

class Edge {
    private final int vertex, key;

    public Edge(int vertex, int key) {
        this.vertex = vertex;
        this.key = key;
    }

    public int getVertex() {
        return vertex;
    }

    public int getKey() {
        return key;
    }

    @Override
    public String toString() {
        return vertex + " " + key;
    }
}