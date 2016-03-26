import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Mars {
    private static ArrayList<Component> result;
    private static ArrayList<Integer> alone;
    private static boolean flag;
    private static int n;

    public static void main(String[] args) {
        flag = true;
        result = new ArrayList<>(0);
        alone = new ArrayList<>(0);
        DFS(scanInput());
        printAnswer();
    }

    private static void printAnswer() {
        if (flag) {
            getAnswer(n)
                    .stream()
                    .sorted((a, b) -> a - b)
                    .forEach(i -> System.out.printf("%s ", i));
        } else {
            System.out.println("No solution");
        }
        System.out.println();
    }

    private static Vertex[] scanInput() {
        Scanner in = new Scanner(System.in);
        n = in.nextInt();
        in.nextLine();
        Vertex[] vertices = new Vertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new Vertex();
            Scanner line = new Scanner(in.nextLine());
            for (int j = 0; j < n; j++) {
                if (line.next().equals("+")) {
                    vertices[i].getEdges().add(j);
                }
            }
        }
        return vertices;
    }

    private static void DFS(Vertex[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            if (vertex.getMark() == Vertex.Marks.WHITE) {
                Component c = new Component();
                visitVertex(vertices, vertex, i, c);
                if (!c.getPluses().isEmpty()) {
                    result.add(c);
                }
            }
        }
    }

    private static void visitVertex(Vertex[] vertices, Vertex v, int vIndex, Component c) {
        v.setMark(Vertex.Marks.GRAY);
        if (v.getSign()) {
            if (v.getEdges().isEmpty()) {
                alone.add(vIndex + 1);
            } else {
                c.getPluses().add(vIndex + 1);
            }
        } else {
            c.getMinuses().add(vIndex + 1);
        }
        for (Integer i :
                v.getEdges()) {
            Vertex u = vertices[i];
            if (u.getMark() == Vertex.Marks.WHITE) {
                u.setSign(!v.getSign());
                visitVertex(vertices, u, i, c);
            } else {
                if (u.getSign() == v.getSign()) {
                    flag = false;
                    return;
                }
            }
        }
        v.setMark(Vertex.Marks.BLACK);
    }

    private static ArrayList<Integer> getAnswer(int n) {
        ArrayList<Integer> answer = new ArrayList<>(0);
        int answerSize = result.stream().mapToInt(x -> x.getPluses().size()).reduce(0, (r, x) -> r + x);
        if (answerSize < n / 2) {
            List<Integer> l = alone.subList(0, n / 2 - answerSize);
            answer.addAll(l);
            alone.removeAll(l);
        }
        for (Component c :
                result) {
            if (c.getPluses().size() > c.getMinuses().size() && alone.get(0) < c.getPluses().get(0)) {
                List<Integer> l = alone.subList(0, c.getPluses().size() - c.getMinuses().size());
                answer.addAll(c.getMinuses());
                answer.addAll(l);
                alone.removeAll(l);
            } else {
                answer.addAll(c.getPluses());
            }
        }
        return answer;
    }
}

class Vertex {
    private final ArrayList<Integer> edges;
    private Marks mark;
    private boolean sign;

    Vertex() {
        setMark(Marks.WHITE);
        setSign(true);
        edges = new ArrayList<>(0);
    }

    Marks getMark() {
        return mark;
    }

    void setMark(Marks mark) {
        this.mark = mark;
    }

    boolean getSign() {
        return sign;
    }

    void setSign(boolean sign) {
        this.sign = sign;
    }

    ArrayList<Integer> getEdges() {
        return edges;
    }

    enum Marks {WHITE, GRAY, BLACK}
}

class Component {
    private final ArrayList<Integer> pluses, minuses;

    Component() {
        pluses = new ArrayList<>(0);
        minuses = new ArrayList<>(0);
    }

    ArrayList<Integer> getPluses() {
        return pluses;
    }

    ArrayList<Integer> getMinuses() {
        return minuses;
    }
}