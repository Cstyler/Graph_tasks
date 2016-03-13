import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mars {
    private static ArrayList<Component> result;
    private static ArrayList<Integer> alone;
    private static boolean flag;

    public static void main(String[] args) {
        flag = true;
        result = new ArrayList<>(0);
        alone = new ArrayList<>(0);
        Scanner scn = new Scanner(System.in);
        int n = scn.nextInt();
        scn.nextLine();
        Vertex[] vertices = new Vertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new Vertex();
            Scanner line = new Scanner(scn.nextLine());
            for (int j = 0; j < n; j++) {
                if (line.next().equals("+")) {
                    vertices[i].edges.add(j);
                }
            }
        }
        DFS(vertices);
        if (flag) {
            ArrayList<Integer> l = getAnswer(n);
            l.sort((a, b) -> a - b);
            for (Integer i :
                    l) {
                System.out.printf("%s ", i);
            }
        } else {
            System.out.println("No solution");
        }
    }

    private static void DFS(Vertex[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            if (vertex.mark == Vertex.Marks.WHITE) {
                Component c = new Component();
                visitVertex(vertices, vertex, i, c);
                if (!c.pluses.isEmpty()) {
                    result.add(c);
                }
            }
        }
    }

    private static void visitVertex(Vertex[] vertices, Vertex v, int vIndex, Component c) {
        v.mark = Vertex.Marks.GRAY;
        if (v.sign == Vertex.PLUS) {
            if (v.edges.isEmpty()) {
                alone.add(vIndex + 1);
            } else {
                c.pluses.add(vIndex + 1);
            }
        } else {
            c.minuses.add(vIndex + 1);
        }
        for (Integer i :
                v.edges) {
            Vertex u = vertices[i];
            if (u.mark == Vertex.Marks.WHITE) {
                u.sign = v.sign == Vertex.PLUS ? Vertex.MINUS : Vertex.PLUS;
                visitVertex(vertices, u, i, c);
            } else {
                if (u.sign == v.sign) {
                    flag = false;
                    return;
                }
            }
        }
        v.mark = Vertex.Marks.BLACK;
    }

    private static ArrayList<Integer> getAnswer(int n) {
        ArrayList<Integer> answer = new ArrayList<>(0);
        int answerSize = 0;
        for (Component c :
                result) {
            answerSize += c.pluses.size();
        }
        if (answerSize < n / 2) {
            List<Integer> l = alone.subList(0, n / 2 - answerSize);
            answer.addAll(l);
            alone.removeAll(l);
        }
        for (Component c :
                result) {
            if (c.pluses.size() > c.minuses.size() && alone.get(0) < c.pluses.get(0)) {
                List<Integer> l = alone.subList(0, c.pluses.size() - c.minuses.size());
                answer.addAll(c.minuses);
                answer.addAll(l);
                alone.removeAll(l);
            } else {
                answer.addAll(c.pluses);
            }
        }
        return answer;
    }
}

class Vertex {
    public Marks mark;
    public boolean sign;
    public final ArrayList<Integer> edges;
    public static final boolean PLUS, MINUS;

    public enum Marks {WHITE, GRAY, BLACK}

    static {
        PLUS = true;
        MINUS = false;
    }

    public Vertex() {
        mark = Marks.WHITE;
        sign = PLUS;
        edges = new ArrayList<>(0);
    }
}

class Component {
    public final ArrayList<Integer> pluses;
    public final ArrayList<Integer> minuses;

    public Component() {
        pluses = new ArrayList<>(0);
        minuses = new ArrayList<>(0);
    }
}