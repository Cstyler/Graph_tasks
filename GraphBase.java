import java.util.*;

class GraphBase {
        public static void main(String[] args) {
		Graph<Integer> graph = new Graph<>(scanInput());
		graph.calcComponents();
		graph.calcCondensation();
		graph.calcBase().forEach(x -> System.out.print(x + " "));
		System.out.println();
	}

	private static ArrayList<Vertex<Integer>> scanInput() {
		Scanner in = new Scanner(System.in);
		int n = in.nextInt(), m = in.nextInt();
		ArrayList<Vertex<Integer>> vertices = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			vertices.add(new Vertex<>(i));
		}
		for (int i = 0; i < m; i++) {
			int a = in.nextInt(), b = in.nextInt();
			vertices.get(a).getOutEdges().add(vertices.get(b));
			vertices.get(b).getInEdges().add(vertices.get(a));
		}
		return vertices;
	}
}

class Graph<T> {
        private final ArrayList<Vertex<T>> vertices;

	Graph(ArrayList<Vertex<T>> vertices) {
		this.vertices = vertices;
	}

	void calcComponents() {
		new StronglyConnectedComponent<>(vertices);
	}

	void calcCondensation() {
		for (Vertex<T> v :
				vertices) {
			Iterator<Vertex<T>> outIterator = v.getOutEdges().iterator();
			while (outIterator.hasNext()) {
				Vertex<T> u = outIterator.next();
				if (u.comp == v.comp) {
					u.getInEdges().remove(v);
					outIterator.remove();
				}
			}
			Iterator<Vertex<T>> inIterator = v.getInEdges().iterator();
			while (outIterator.hasNext()) {
				Vertex<T> u = inIterator.next();
				if (u.comp == v.comp) {
					u.getOutEdges().remove(v);
					inIterator.remove();
				}
			}
		}
	}

	List<Integer> calcBase() {
		List<Vertex<T>> base = new LinkedList<>();
		HashSet<Integer> hashSet = new HashSet<>();
		List<Integer> answer = new ArrayList<>();
		for (Vertex<T> v : vertices) {
			if (v.getInDegree() == 0) {
				base.add(v);
			} else {
				hashSet.add(v.comp);
			}
		}
		base.removeIf(x -> hashSet.contains(x.comp));
		hashSet.clear();
		base.forEach(v -> {
			if (!hashSet.contains(v.comp)) {
				hashSet.add(v.comp);
				answer.add((Integer) v.getItem());
			}
		});
		return answer;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("digraph {\n");
		for (Vertex<T> vertex : vertices) {
			s.append("\t").append(vertex.toString()).append(";\n");
			for (Vertex<T> outEdge : vertex.getOutEdges()) {
				s.append("\t").append(vertex).append(" -> ").append(outEdge).append(";\n");
			}
		}
		s.append("}\n");
		return s.toString();
	}
}

class StronglyConnectedComponent<T> {
	private final ArrayList<Vertex<T>> vertices;
	private final Stack<Vertex<T>> stack;
	private int time, count;

	StronglyConnectedComponent(ArrayList<Vertex<T>> vertices) {
		this.vertices = vertices;
		time = count = 1;
		stack = new Stack<>();
		Tarjan();
	}

	private void Tarjan() {
		vertices.stream().filter(v -> v.time1 == 0).forEach(this::visitVertexTarjan);
	}

	private void visitVertexTarjan(Vertex<T> v) {
		v.time1 = v.low = time++;
		stack.push(v);
		for (Vertex<T> u :
				v.getOutEdges()) {
			if (u.time1 == 0)
				visitVertexTarjan(u);
			if (u.comp == 0 && u.low < v.low)
				v.low = u.low;
		}
		if (v.time1 == v.low) {
			Vertex<T> u;
			do {
				u = stack.pop();
				u.comp = count;
			} while (u != v);
			count++;
		}
	}
}

class Vertex<T> {
        private final List<Vertex<T>> outEdges, inEdges;
	private final T item;
	int comp, time1, low;

	Vertex(T item) {
		this.item = item;
		comp = time1 = 0;
		outEdges = new LinkedList<>();
		inEdges = new LinkedList<>();
	}

	T getItem() {
		return item;
	}

	int getInDegree() {
		return getInEdges().size();
	}

	@Override
	public String toString() {
		return item.toString();
	}

	List<Vertex<T>> getOutEdges() {
		return outEdges;
	}

	List<Vertex<T>> getInEdges() {
		return inEdges;
	}
}
