import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by khan on 12.03.16. BridgeNum
 */

public class BridgeNum {
    private static int edgeNum;

    public static void main(String[] args) {
        getAnswer(scanInput());
    }

    private static void getAnswer(Vertex[] vertices) {
        Graph graph = new Graph(vertices);
        graph.DFS1();
        int comp = graph.DFS2() - 1;
        int edgeCount = 0;
        for (int i = 0; i < comp + 1; i++) {
            for (Vertex v :
                    vertices) {
                if (v.comp == i) {
                    for (Integer index :
                            v.edges) {
                        if (vertices[index].comp == i) {
                            edgeCount++;
                        }
                    }
                }
            }
        }
        System.out.println(edgeNum - edgeCount / 2);
    }

    private static Vertex[] scanInput() {
        Scanner scn = new Scanner(System.in);
        final int n = scn.nextInt(), m = scn.nextInt();
        edgeNum = m;
        Vertex[] vertices = new Vertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new Vertex();
        }
        for (int i = 0; i < m; i++) {
            int a = scn.nextInt(), b = scn.nextInt();
            vertices[a].edges.add(b);
            if (b != a) {
                vertices[b].edges.add(a);
            }
        }
        return vertices;
    }
}

class Graph {
    private final Vertex[] vertices;
    private final ArrayDeque<Vertex> deque;

    public Graph(Vertex[] vertices) {
        deque = new ArrayDeque<>();
        this.vertices = vertices;
    }

    public void DFS1() {
        for (Vertex v :
                vertices) {
            if (v.mark == Vertex.Marks.WHITE) {
                visitVertex1(v);
            }
        }
    }

    private void visitVertex1(Vertex vertex) {
        vertex.mark = Vertex.Marks.GRAY;
        deque.addLast(vertex);
        for (Integer i :
                vertex.edges) {
            Vertex u = vertices[i];
            if (u.mark == Vertex.Marks.WHITE) {
                u.parent = vertex;
                visitVertex1(u);
            }
        }
        vertex.mark = Vertex.Marks.BLACK;
    }

    public int DFS2() {
        int component = 0;
        while (!deque.isEmpty()) {
            Vertex v = deque.pollFirst();
            if (v.comp == -1) {
                visitVertex2(v, component++);
            }
        }
        return component;
    }

    private void visitVertex2(Vertex vertex, int component) {
        vertex.comp = component;
        for (Integer i :
                vertex.edges) {
            Vertex u = vertices[i];
            if (u.comp == -1 && u.parent != vertex) {
                visitVertex2(u, component);
            }
        }
    }
}

class Vertex {
    public Marks mark;
    public final ArrayList<Integer> edges;
    public int comp;
    public Vertex parent;

    enum Marks {WHITE, GRAY, BLACK}

    public Vertex() {
        comp = -1;
        parent = null;
        mark = Marks.WHITE;
        edges = new ArrayList<>(0);
    }
}