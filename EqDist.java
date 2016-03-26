import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by khan on 12.03.16. EqDist
 */

class EqDist {
    private static Vertex[] supportVertices, vertices;

    public static void main(String[] args) {
        scanInput();
        getAnswer();
    }

    private static void getAnswer() {
        ArrayList<ArrayList<Tuple>> dists = BFS();
        printAnswer(intersectOfDists(dists));
    }

    private static void printAnswer(ArrayList<Tuple> dists) {
        if (dists.isEmpty()) {
            System.out.print("-");
        } else {
            dists
                    .stream()
                    .sorted((a, b) -> a.getIndex() - b.getIndex())
                    .forEach(x -> System.out.print(x.getIndex() + " "));
        }
        System.out.println();
    }

    private static ArrayList<Tuple> intersectOfDists(ArrayList<ArrayList<Tuple>> dists) {
        for (int i = 1; i < dists.size(); i++) {
            ArrayList<Tuple> tempArray = new ArrayList<>();
            dists.get(i).stream()
                    .flatMap(x -> dists.get(0)
                            .stream()
                            .filter(y -> y.getDist() == x.getDist() && y.getIndex() == x.getIndex()))
                    .forEach(tempArray::add);
            dists.set(0, tempArray);
        }
        return dists.get(0);
    }

    private static void scanInput() {
        Scanner scn = new Scanner(System.in);
        final int n = scn.nextInt(), m = scn.nextInt();
        vertices = new Vertex[n];
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
        final int k = scn.nextInt();
        supportVertices = new Vertex[k];
        for (int i = 0; i < k; i++) {
            supportVertices[i] = vertices[scn.nextInt()];
        }
    }

    private static ArrayList<ArrayList<Tuple>> BFS() {
        ArrayDeque<Vertex> deque = new ArrayDeque<>();
        ArrayList<ArrayList<Tuple>> dists = new ArrayList<>(supportVertices.length);
        ArrayList<Vertex> markedVertices = new ArrayList<>();
        for (int i = 0; i < supportVertices.length; i++) {
            Vertex v = supportVertices[i];
            markedVertices.clear();
            dists.add(new ArrayList<>());
            v.setMark(true);
            v.setDist(0);
            deque.addLast(v);
            while (!deque.isEmpty()) {
                Vertex q = deque.pollFirst();
                for (Integer uInd :
                        q.getEdges()) {
                    Vertex u = vertices[uInd];
                    if (!u.getMark()) {
                        u.setDist(q.getDist() + 1);
                        u.setMark(true);
                        markedVertices.add(u);
                        dists.get(i).add(new Tuple(u.getDist(), uInd));
                        deque.addLast(u);
                    }
                }
            }
            markedVertices.forEach(a -> a.setMark(false));
        }
        return dists;
    }
}

class Vertex {
    private final ArrayList<Integer> edges;
    private boolean mark;
    private int dist;

    Vertex() {
        setDist(0);
        setMark(false);
        edges = new ArrayList<>(0);
    }

    boolean getMark() {
        return mark;
    }

    void setMark(boolean mark) {
        this.mark = mark;
    }

    ArrayList<Integer> getEdges() {
        return edges;
    }

    int getDist() {
        return dist;
    }

    void setDist(int dist) {
        this.dist = dist;
    }
}

class Tuple {
    private final int dist;
    private final int index;

    Tuple(int dist, int index) {
        this.dist = dist;
        this.index = index;
    }

    int getDist() {
        return dist;
    }

    int getIndex() {
        return index;
    }
}