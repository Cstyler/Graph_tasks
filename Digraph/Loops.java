
import java.util.*;
import java.util.function.BiConsumer;

class Loops {
        private final static Map<Integer, Vertex<Integer>> vertexMap = new HashMap<>();
	private static Vertex<Integer> root;

	public static void main(String[] args) {
		scan();
		Graph<Integer> graph = new Graph<>(vertexMap.values(), root);
		graph.dominators();
		System.out.println(graph.findLoops());
	}

	private static void scan() {
		Scanner in = new Scanner(System.in);
		final int n = in.nextInt();
		int label = in.nextInt();
		vertexMap.put(label, root = new Vertex<>(label));
		for (int i = 0; i < n; i++) {
			Vertex<Integer> v = put(label);
			switch (in.next()) {
				case "ACTION":
					if (i < n - 1) {
						label = in.nextInt();
						v.addVertex(put(label));
					}
					break;
				case "BRANCH":
					v.addVertex(put(in.nextInt()));
					if (i < n - 1) {
						label = in.nextInt();
						v.addVertex(put(label));
					}
					break;
				case "JUMP":
					v.addVertex(put(in.nextInt()));
					if (i < n - 1) {
						label = in.nextInt();
					}
					break;
			}
		}
	}

	private static Vertex<Integer> put(int label) {
		Vertex<Integer> v = vertexMap.get(label);
		if (v == null) {
			v = new Vertex<>(label);
			vertexMap.put(label, v);
		}
		return v;
	}
}

class Graph<T> {
        private final Collection<Vertex<T>> vertices;
	private final Vertex<T> root;
	private int time = 1;

	Graph(Collection<Vertex<T>> vertices, Vertex<T> root) {
		this.vertices = vertices;
		this.root = root;
	}

	private List<Vertex<T>> DFS() {
		List<Vertex<T>> vertexList = new ArrayList<>();
		visitVertex(root, vertexList);
		return vertexList;
	}

	private void visitVertex(Vertex<T> v, List<Vertex<T>> vertexList) {
		v.setTime1(time++);
		if (v != root) {
			vertexList.add(v);
		}
		for (Vertex<T> u : v.outEdges()) {
			if (u.time1() == 0) {
				u.setParent(v);
				visitVertex(u, vertexList);
			}
		}
	}

	void dominators() {
		List<Vertex<T>> vertexList = DFS();
		Set<Vertex<T>> vertexSet = new HashSet<>(vertexList);
		vertexSet.add(root);
		Collections.reverse(vertexList);
		for (Vertex<T> w : vertexList) {
			for (Vertex<T> v : w.inEdges()) {
				if (vertexSet.contains(v)) {
					Vertex<T> u = findMin(v);
					if (u.sdom().compareTo(w.sdom()) < 0) {
						w.setSdom(u.sdom());
					}
				}
			}
			w.setAncestor(w.parent());
			w.sdom().bucket().push(w);
			while (!w.parent().bucket().isEmpty()) {
				Vertex<T> v = w.parent().bucket().pop();
				Vertex<T> u = findMin(v);
				v.setDom(u.sdom() == v.sdom() ? v.sdom() : u);
			}
		}
		Collections.reverse(vertexList);
		for (Vertex<T> w : vertexList) {
			if (w.dom() != w.sdom()) {
				w.setDom(w.dom().dom());
			}
		}
	}

	private Vertex<T> findMin(Vertex<T> v) {
		searchAndCut(v);
		return v.label();
	}

	private Vertex<T> searchAndCut(Vertex<T> v) {
		if (v.ancestor() == null) {
			return v;
		} else {
			Vertex<T> root = searchAndCut(v.ancestor());
			if (v.ancestor().label().sdom().compareTo(v.label().sdom()) < 0) {
				v.setLabel(v.ancestor().label());
			}
			v.setAncestor(root);
			return root;
		}
	}

	int findLoops() {
		int loopCount = 0;
		for (Vertex<T> v : vertices) {
			for (Vertex<T> u : v.inEdges()) {
				if (isDom(u, v)) {
					loopCount++;
					break;
				}
			}
		}
		return loopCount;
	}

	private boolean isDom(Vertex<T> u, Vertex<T> v) {
		return u.dom() != null && (u.dom() == v || isDom(u.dom(), v));
	}
}

class Vertex<T> implements Comparable<Vertex<T>> {
	private final Stack<Vertex<T>> bucket;
	private final List<Vertex<T>> outEdges, inEdges;
	private final T item;
	private Vertex<T> parent, ancestor, label, dom, sdom;
	private int time1;

	Vertex(T item) {
		this.item = item;
		outEdges = new LinkedList<>();
		inEdges = new LinkedList<>();
		sdom = label = this;
		bucket = new Stack<>();
		time1 = 0;
	}

	@Override
	public int compareTo(Vertex<T> o) {
		return this.time1 - o.time1;
	}

	@Override
	public String toString() {
		return item.toString();
	}

	List<Vertex<T>> outEdges() {
		return outEdges;
	}

	List<Vertex<T>> inEdges() {
		return inEdges;
	}

	void addVertex(Vertex<T> v) {
		outEdges().add(v);
		v.inEdges().add(this);
	}

	int time1() {
		return time1;
	}

	void setTime1(int time1) {
		this.time1 = time1;
	}

	Stack<Vertex<T>> bucket() {
		return bucket;
	}

	Vertex<T> parent() {
		return parent;
	}

	void setParent(Vertex<T> parent) {
		this.parent = parent;
	}

	Vertex<T> ancestor() {
		return ancestor;
	}

	void setAncestor(Vertex<T> ancestor) {
		this.ancestor = ancestor;
	}

	Vertex<T> label() {
		return label;
	}

	void setLabel(Vertex<T> label) {
		this.label = label;
	}

	Vertex<T> dom() {
		return dom;
	}

	void setDom(Vertex<T> dom) {
		this.dom = dom;
	}

	Vertex<T> sdom() {
		return sdom;
	}

	void setSdom(Vertex<T> sdom) {
		this.sdom = sdom;
	}
}

