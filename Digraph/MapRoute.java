import java.util.PriorityQueue;
import java.util.Scanner;

class MapRoute {
	public static void main(String[] args) {
		System.out.println(Dijkstra(scanInput()));
	}

	private static Vertex[][] scanInput() {
		Scanner in = new Scanner(System.in);
		final int n = in.nextInt();
		final Vertex[][] matrix = new Vertex[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				matrix[i][j] = new Vertex(in.nextInt(), i, j);
			}
		}
		return matrix;
	}

	private static int Dijkstra(Vertex[][] matrix) {
		PriorityQueue<Vertex> queue = new PriorityQueue<>((x, y) -> x.getDist() - y.getDist());
		matrix[0][0].setDist(matrix[0][0].getItem());
		queue.add(matrix[0][0]);
		int n = matrix.length;
		while (!queue.isEmpty()) {
			Vertex v = queue.poll();
			int i = v.getI();
			int j = v.getJ();
			if (i != n - 1) {
				Vertex u = matrix[i + 1][j];
				if (relax(v, u, u.getItem())) {
					queue.add(u);
				}
			}
			if (i != 0) {
				Vertex u = matrix[i - 1][j];
				if (relax(v, u, u.getItem())) {
					queue.add(u);
				}
			}
			if (j != n - 1) {
				Vertex u = matrix[i][j + 1];
				if (relax(v, u, u.getItem())) {
					queue.add(u);
				}
			}
			if (j != 0) {
				Vertex u = matrix[i][j - 1];
				if (relax(v, u, u.getItem())) {
					queue.add(u);
				}
			}
		}
		return matrix[n - 1][n - 1].getDist();
	}

	private static boolean relax(Vertex u, Vertex v, int weight) {
		int newDist = u.getDist() + weight;
		if (newDist < v.getDist()) {
			v.setDist(newDist);
			return true;
		}
		return false;
	}
}

class Vertex {
	private final int item, i, j;
	private int dist;

	Vertex(int item, int i, int j) {
		this.item = item;
		this.i = i;
		this.j = j;
		dist = 50000;
	}

	int getItem() {
		return item;
	}

	int getDist() {
		return dist;
	}

	void setDist(int dist) {
		this.dist = dist;
	}

	int getI() {
		return i;
	}

	int getJ() {
		return j;
	}
}