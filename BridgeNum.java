import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

class BridgeNum {
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
                if (v.getComp() == i) {
                    for (Integer index :
                            v.getEdges()) {
                        if (vertices[index].getComp() == i) {
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
            vertices[a].getEdges().add(b);
            if (b != a) {
                vertices[b].getEdges().add(a);
            }
        }
        return vertices;
    }
}

class Graph {
    private final Vertex[] vertices;
    private final ArrayDeque<Vertex> deque;

    Graph(Vertex[] vertices) {
        deque = new ArrayDeque<>();
        this.vertices = vertices;
    }

    void DFS1() {
        for (Vertex v :
                vertices) {
            if (v.getMark() == Vertex.Marks.WHITE) {
                visitVertex1(v);
            }
        }
    }

    private void visitVertex1(Vertex vertex) {
        vertex.setMark(Vertex.Marks.GRAY);
        deque.addLast(vertex);
        for (Integer i :
                vertex.getEdges()) {
            Vertex u = vertices[i];
            if (u.getMark() == Vertex.Marks.WHITE) {
                u.setParent(vertex);
                visitVertex1(u);
            }
        }
        vertex.setMark(Vertex.Marks.BLACK);
    }

    int DFS2() {
        int component = 0;
        while (!deque.isEmpty()) {
            Vertex v = deque.pollFirst();
            if (v.getComp() == -1) {
                visitVertex2(v, component++);
            }
        }
        return component;
    }

    private void visitVertex2(Vertex vertex, int component) {
        vertex.setComp(component);
        for (Integer i :
                vertex.getEdges()) {
            Vertex u = vertices[i];
            if (u.getComp() == -1 && u.getParent() != vertex) {
                visitVertex2(u, component);
            }
        }
    }
}

class Vertex {
    private final ArrayList<Integer> edges;
    private Marks mark;
    private int comp;
    private Vertex parent;

    Vertex() {
        setComp(-1);
        setParent(null);
        setMark(Marks.WHITE);
        edges = new ArrayList<>(0);
    }

    Marks getMark() {
        return mark;
    }

    void setMark(Marks mark) {
        this.mark = mark;
    }

    ArrayList<Integer> getEdges() {
        return edges;
    }

    int getComp() {
        return comp;
    }

    void setComp(int comp) {
        this.comp = comp;
    }

    Vertex getParent() {
        return parent;
    }

    void setParent(Vertex parent) {
        this.parent = parent;
    }

    enum Marks {WHITE, GRAY, BLACK}
}