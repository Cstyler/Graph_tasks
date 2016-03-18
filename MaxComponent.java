import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by khan on 12.03.16. MaxComponent
 */

public class MaxComponent {
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
            vertices[a].edges.add(b);
            if (b != a) {
                vertices[b].edges.add(a);
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
        removeNotMax(temp, a -> a.vertexCount);
        removeNotMax(temp, a -> a.edgeCount);
        return componentArray.indexOf(temp.get(0));
    }

    private static void removeNotMax(ArrayList<Component> components, Key key) {
        final int maxCount = key.getKey(components.stream().max((a,b)->key.getKey(a)-key.getKey(b)).get());
        components.removeIf(a -> key.getKey(a) != maxCount);
    }

    interface Key {
        int getKey(Component c);
    }

    private static void printAnswer(Vertex[] vertices, int maxComponentIndex) {
        System.out.println("graph {");
        for (int i = 0; i < vertices.length; i++) {
            printEdgesAndVertices(vertices[i], i, vertices[i].cNum == maxComponentIndex ? " [color = red]" : "", vertices);
        }
        System.out.println("}");
    }

    private static void printEdgesAndVertices(Vertex vertex, int vertexIndex, String tag, Vertex[] vertices) {
        System.out.println("\t" + vertexIndex + tag);
        for (int j = 0; j < vertex.edges.size(); j++) {
            int u = vertex.edges.get(j);
            if(vertexIndex <= u) System.out.println("\t" + vertexIndex + " -- " + u + tag + "\n\t" + u + tag);
        }
    }
}

class Graph {
    private final ArrayList<Component> componentArray;
    private final Vertex[] vertices;

    public Graph(Vertex[] vertices) {
        this.componentArray = new ArrayList<>(1);
        this.vertices = vertices;
    }

    public ArrayList<Component> getComponentArray() {
        return componentArray;
    }

    public void DFS() {
        for (Vertex v :
                vertices) {
            if (v.mark == Vertex.Marks.WHITE) {
                componentArray.add(new Component());
                visitVertex(v, componentArray.size() - 1);
            }
        }
    }

    private void visitVertex(Vertex v, int componentNum) {
        v.mark = Vertex.Marks.GRAY;
        v.cNum = componentNum;
        componentArray.get(componentNum).edgeCount += v.edges.size();
        for (Integer i :
                v.edges) {
            Vertex u = vertices[i];
            if (u.mark == Vertex.Marks.WHITE) {
                componentArray.get(componentNum).vertexCount++;
                u.cNum = componentNum;
                visitVertex(u, componentNum);
            }
        }
        v.mark = Vertex.Marks.BLACK;
    }
}

class Vertex {
    public Marks mark;
    public final ArrayList<Integer> edges;
    public int cNum;

    enum Marks {WHITE, GRAY, BLACK}

    public Vertex() {
        cNum = 0;
        mark = Marks.WHITE;
        edges = new ArrayList<>(0);
    }
}

class Component {
    public int vertexCount;
    public int edgeCount;

    public Component() {
        this.vertexCount = 1;
        this.edgeCount = 0;
    }
}
