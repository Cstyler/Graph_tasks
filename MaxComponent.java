import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by khan on 12.03.16. MaxComponent
 */

class MaxComponent {
    public static void main(String[] args) {
        getAnswer(scanInput());
    }

    private static Vertex[] scanInput() {
        Scanner scn = new Scanner(System.in);
        int n = scn.nextInt(), m = scn.nextInt();
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

    private static void getAnswer(Vertex[] vertices) {
        Graph graph = new Graph(vertices);
        graph.DFS();
        printAnswer(vertices, getMaxComponentIndex(graph.getComponentArray()));
    }

    private static int getMaxComponentIndex(ArrayList<Component> componentArray) {
        @SuppressWarnings("unchecked") ArrayList<Component> temp = (ArrayList<Component>) componentArray.clone();
        removeNotMax(temp, Component::getVertexCount);
        removeNotMax(temp, Component::getEdgeCount);
        return componentArray.indexOf(temp.get(0));
    }

    private static void removeNotMax(ArrayList<Component> components, Key key) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") final int maxCount = key.getKey(components.stream().max((a, b) -> key.getKey(a) - key.getKey(b)).get());
        components.removeIf(a -> key.getKey(a) != maxCount);
    }

    private static void printAnswer(Vertex[] vertices, int maxComponentIndex) {
        System.out.println("graph {");
        String redTag = " [color = red]";
        for (int i = 0; i < vertices.length; i++) {
            System.out.printf("\t%d%s\n", i, vertices[i].getcNum() == maxComponentIndex ? redTag : "");
        }
        for (int i = 0; i < vertices.length; i++) {
            printEdgesAndVertices(vertices[i], i, vertices[i].getcNum() == maxComponentIndex ? redTag : "");
        }
        System.out.println("}");
    }

    private static void printEdgesAndVertices(Vertex vertex, int vertexIndex, String tag) {
        for (int j = 0; j < vertex.getEdges().size(); j++) {
            int u = vertex.getEdges().get(j);
            if (vertexIndex <= u) System.out.println("\t" + vertexIndex + " -- " + u + tag);
        }
    }

    private interface Key {
        int getKey(Component c);
    }
}

class Graph {
    private final ArrayList<Component> componentArray;
    private final Vertex[] vertices;

    Graph(Vertex[] vertices) {
        this.componentArray = new ArrayList<>(1);
        this.vertices = vertices;
    }

    ArrayList<Component> getComponentArray() {
        return componentArray;
    }

    void DFS() {
        for (Vertex v :
                vertices) {
            if (v.getMark() == Vertex.Marks.WHITE) {
                componentArray.add(new Component());
                visitVertex(v, componentArray.size() - 1);
            }
        }
    }

    private void visitVertex(Vertex v, int componentNum) {
        v.setMark(Vertex.Marks.GRAY);
        v.setcNum(componentNum);
        componentArray.get(componentNum).setEdgeCount(componentArray.get(componentNum).getEdgeCount() + v.getEdges().size());
        for (Integer i :
                v.getEdges()) {
            Vertex u = vertices[i];
            if (u.getMark() == Vertex.Marks.WHITE) {
                componentArray.get(componentNum).setVertexCount(componentArray.get(componentNum).getVertexCount() + 1);
                u.setcNum(componentNum);
                visitVertex(u, componentNum);
            }
        }
        v.setMark(Vertex.Marks.BLACK);
    }
}

class Vertex {
    private final ArrayList<Integer> edges;
    private Marks mark;
    private int cNum;

    Vertex() {
        setcNum(0);
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

    int getcNum() {
        return cNum;
    }

    void setcNum(int cNum) {
        this.cNum = cNum;
    }

    enum Marks {WHITE, GRAY, BLACK}
}

class Component {
    private int vertexCount;
    private int edgeCount;

    Component() {
        this.setVertexCount(1);
        this.setEdgeCount(0);
    }

    int getVertexCount() {
        return vertexCount;
    }

    void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    int getEdgeCount() {
        return edgeCount;
    }

    void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }
}
