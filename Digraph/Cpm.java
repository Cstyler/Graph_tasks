import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

enum Tag {
	IDENT, NUMBER, LESS_THAN, SEMICOLON, LPAREN, RPAREN, END_OF_TEXT
}

/**
 * Created by khan on 17.04.16. Cpm
 */

class Cpm {
	public static void main(String[] args) throws CloneNotSupportedException {
		Scanner in = new Scanner(System.in);
		in.useDelimiter("\\Z");
		try {
			Parser p = new Parser(in.next());
			p.parse();
			Graph<String> g = new Graph<>(p.vertexMap().values());
			g.findAllCriticalPaths(p.baseVertices(), p.vertexMap());
			System.out.println(g);
		} catch (SyntaxError e) {
			System.out.println(e.getMessage());
		}
	}
}

class Parser {
	private Token sym;
	private String prevWork, nextWork;
	private Map<String, Vertex<String>> vertexMap;
	private Set<String> baseVertices;

	Parser(String text) throws SyntaxError {
		sym = new Token(text);
		vertexMap = new HashMap<>();
		baseVertices = new HashSet<>();
	}

	Map<String, Vertex<String>> vertexMap() {
		return vertexMap;
	}

	Set<String> baseVertices() {
		return baseVertices;
	}

	private void expect(Tag type) throws SyntaxError {
		if (!sym.matches(type)) {
			sym.throwError(type + " expected");
		}
		nextToken();
	}

	private void nextToken() throws SyntaxError {
		sym = sym.next();
	}

	/*
	Backus–Naur Form:
	<sentences> ::= <sentence> <sentences'> .
	<sentences'> ::= <sentences> | .
	<sentence> ::= <work> <sentence'> .
	<sentence'> ::= < <sentence> | ; .
	<work> ::= <ident> <work'> .
	<work'> ::= (<number) | .
	*/

	void parse() throws SyntaxError {
		sentences();
		expect(Tag.END_OF_TEXT);
	}

	private void sentences() throws SyntaxError {
		prevWork = nextWork = "";
		sentence();
		sentencesRecursion();
	}

	private void sentencesRecursion() throws SyntaxError {
		if (sym.matches(Tag.IDENT)) {
			sentences();
		}
	}

	private void sentence() throws SyntaxError {
		work();
		sentenceRecursion();
	}

	private void sentenceRecursion() throws SyntaxError {
		if (sym.matches(Tag.LESS_THAN)) {
			nextToken();
			sentence();
		} else if (sym.matches(Tag.SEMICOLON)) {
			nextToken();
		}
	}

	private void work() throws SyntaxError {
		String temp = nextWork;
		nextWork = sym.toString();
		prevWork = temp;
		expect(Tag.IDENT);
		workRecursion();
		if (!prevWork.equals("")) {
			vertexMap.get(prevWork).addVertex(vertexMap.get(nextWork));
		} else {
			baseVertices.add(nextWork);
		}
	}

	private void workRecursion() throws SyntaxError {
		if (sym.matches(Tag.LPAREN)) {
			nextToken();
			vertexMap.put(nextWork, new Vertex<>(nextWork, -Integer.parseInt(sym.toString())));
			expect(Tag.NUMBER);
			expect(Tag.RPAREN);
		}
	}
}

class Position {
	private final int index, line, col;
	private final String text;

	Position(String text) {
		this(text, 0, 1, 1);
	}

	private Position(String text, int index, int line, int col) {
		this.index = index;
		this.line = line;
		this.col = col;
		this.text = text;
	}

	int getChar() {
		return index < text.length() ? text.codePointAt(index) : -1;
	}

	Position skip() {
		int c = getChar();
		switch (c) {
			case -1:
				return this;
			case '\n':
				return new Position(text, index + 1, line + 1, 1);
			default:
				return new Position(text, index + (c > 0xFFFF ? 2 : 1), line, col + 1);
		}
	}

	boolean satisfies(Predicate<Integer> p) {
		return p.test(getChar());
	}

	Position skipWhile(Predicate<Integer> p) {
		Position pos = this;
		while (pos.satisfies(p)) {
			pos = pos.skip();
		}
		return pos;
	}

	String substring(Position follow) {
		return text.substring(this.index, follow.index);
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", line, col);
	}
}

class Token {
	private final Position start;
	private Position follow;
	private Tag tag;

	Token(String text) throws SyntaxError {
		this(new Position(text));
	}

	private Token(Position currentPos) throws SyntaxError {
		start = currentPos.skipWhile(Character::isWhitespace);
		follow = start.skip();
		switch (start.getChar()) {
			case -1:
				tag = Tag.END_OF_TEXT;
				break;
			case '<':
				tag = Tag.LESS_THAN;
				break;
			case ';':
				tag = Tag.SEMICOLON;
				break;
			case '(':
				tag = Tag.LPAREN;
				break;
			case ')':
				tag = Tag.RPAREN;
				break;
			default:
				if (start.satisfies(Character::isLetter)) {
					follow = follow.skipWhile(Character::isLetterOrDigit);
					tag = Tag.IDENT;
				} else if (start.satisfies(Character::isDigit)) {
					follow = follow.skipWhile(Character::isDigit);
					if (follow.satisfies(Character::isLetter)) {
						throw new SyntaxError("delimiter expected", follow);
					}
					tag = Tag.NUMBER;
				} else {
					throwError("invalid character");
				}
		}
	}

	@Override
	public String toString() {
		return start.substring(follow);
	}

	boolean matches(Tag... types) {
		return Arrays.stream(types).anyMatch(x -> x == tag);
	}

	void throwError(String msg) throws SyntaxError {
		throw new SyntaxError(msg, start);
	}

	Token next() throws SyntaxError {
		return new Token(follow);
	}
}

class SyntaxError extends Exception {
	SyntaxError(String message, Position pos) {
		super(String.format("Syntax error at %s: %s", pos.toString(), message));
	}
}

class Graph<T> {
	private final Collection<Vertex<T>> vertices;

	Graph(Collection<Vertex<T>> vertices) {
		this.vertices = vertices;
	}

	void findAllCriticalPaths(Set<T> baseVertices, Map<T, Vertex<T>> vertexMap) {
		calcComponents();
		List<Vertex<T>> l = new ArrayList<>();
		baseVertices.forEach(x -> l.add(vertexMap.get(x)));
		l.removeIf(x -> x.inEdges().size() != 0);
		calcCondensation();
		l.removeIf(x -> x.color() == Vertex.Color.BLUE);
		BellmanFord(l);
	}

	private void calcComponents() {
		new StronglyConnectedComponent<>(vertices);
	}

	private void calcCondensation() {
		for (Vertex<T> v :
				vertices) {
			for (Vertex<T> u : v.outEdges()) {
				if (u.comp() == v.comp() && u.color() != Vertex.Color.BLUE) {
					makeBlue(u);
					makeBlue(v);
				}
			}
		}
	}

	private void makeBlue(Vertex<T> v) {
		v.setColor(Vertex.Color.BLUE);
		for (Vertex<T> vertex : v.outEdges()) {
			if (vertex.color() != Vertex.Color.BLUE) {
				makeBlue(vertex);
			}
		}
	}

	private void BellmanFord(List<Vertex<T>> a) {
		Integer minOfAll = 1;
		Map<Vertex<T>, Integer> minMap = new HashMap<>();
		for (Vertex<T> s : a) {
			Vertex<T> min = s;
			s.setDist(s.weight());
			for (int i = 1; i < vertices.size(); i++) {
				for (Vertex<T> u : vertices) {
					for (Vertex<T> v : u.outEdges()) {
						if (v.color() != Vertex.Color.BLUE) {
							relax(u, v, v.weight());
							min = min.dist() >= v.dist() ? v : min;
						}
					}
				}
			}
			minOfAll = minOfAll >= min.dist() ? min.dist() : minOfAll;
			minMap.put(s, minOfAll);
		}
		final int min = minOfAll;
		for (Vertex<T> s : a.stream().filter(x -> minMap.get(x) == min).collect(Collectors.toList())) {
			s.setDist(s.weight());
			for (int i = 1; i < vertices.size(); i++) {
				for (Vertex<T> u : vertices) {
					for (Vertex<T> v : u.outEdges()) {
						if (v.color() != Vertex.Color.BLUE) {
							relax(u, v, v.weight());
						}
					}
				}
			}
			for (Vertex<T> v :
					vertices) {
				if (v.dist() == min) {
					findCriticalPath(v);
				}
			}
		}
	}

	private void findCriticalPath(Vertex<T> v) {
		v.setColor(Vertex.Color.RED);
		for (Vertex<T> parent : v.parents()) {
			if (parent != null) {
				parent.addRedVertex(v);
				findCriticalPath(parent);
			}
		}
	}

	private void relax(Vertex<T> u, Vertex<T> v, int weight) {
		int temp = u.dist() + weight;
		if (temp < v.dist()) {
			v.setDist(temp);
			v.parents().clear();
			v.parents().add(u);
		} else if (temp == v.dist()) {
			v.parents().add(u);
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("digraph {\n");
		String redTag = "color = red]\n";
		String blueTag = "color = blue]\n";
		for (Vertex<T> vertex : vertices) {
			s.append(String.format("\t%s [label = \"%s(%s)\"", vertex, vertex, -vertex.weight()));
			if (vertex.color() == Vertex.Color.RED) {
				s.append(String.format(", %s", redTag));
			} else if (vertex.color() == Vertex.Color.BLUE) {
				s.append(String.format(", %s", blueTag));
			} else {
				s.append("]\n");
			}
			for (Vertex<T> outEdge : vertex.outEdges()) {
				s.append(String.format("\t%s -> %s", vertex, outEdge));
				if (vertex.color() == outEdge.color() && vertex.color() == Vertex.Color.BLUE) {
					s.append(String.format(" [%s", blueTag));
				} else if (vertex.redEdges().contains(outEdge)) {
					s.append(String.format(" [%s", redTag));
				} else {
					s.append("\n");
				}
			}
		}
		s.append("}\n");
		return s.toString();
	}
}

class StronglyConnectedComponent<T> {
	private final Collection<Vertex<T>> vertices;
	private final Stack<Vertex<T>> stack;
	private int time, count;

	StronglyConnectedComponent(Collection<Vertex<T>> vertices) {
		this.vertices = vertices;
		time = count = 1;
		stack = new Stack<>();
		Tarjan();
	}

	private void Tarjan() {
		for (Vertex<T> v : vertices) {
			if (v.time1() == 0) {
				visitVertexTarjan(v);
			}
		}
	}

	private void visitVertexTarjan(Vertex<T> v) {
		v.setTime1(time);
		v.setLow(time++);
		stack.push(v);
		for (Vertex<T> u :
				v.outEdges()) {
			if (u.time1() == 0)
				visitVertexTarjan(u);
			if (u.comp() == 0 && u.low() < v.low())
				v.setLow(u.low());
		}
		if (v.time1() == v.low()) {
			Vertex<T> u;
			do {
				u = stack.pop();
				u.setComp(count);
			} while (u != v);
			count++;
		}
	}
}

class Vertex<T> implements Cloneable {
	private final Set<Vertex<T>> outEdges, inEdges, redEdges;
	private final T item;
	private final int weight;
	private final Set<Vertex<T>> parents;
	private Color color;
	private int comp, time1, low, dist;

	Vertex(T item, int weight) {
		this.item = item;
		this.weight = weight;
		setDist(100000);
		setColor(null);
		parents = new HashSet<>();
		outEdges = new HashSet<>();
		inEdges = new HashSet<>();
		redEdges = new HashSet<>();
	}

	Set<Vertex<T>> outEdges() {
		return outEdges;
	}

	Set<Vertex<T>> inEdges() {
		return inEdges;
	}

	Set<Vertex<T>> redEdges() {
		return redEdges;
	}

	void addRedVertex(Vertex<T> v) {
		redEdges.add(v);
	}

	void addVertex(Vertex<T> v) {
		outEdges.add(v);
		v.inEdges.add(this);
	}

	int weight() {
		return weight;
	}

	int comp() {
		return comp;
	}

	void setComp(int comp) {
		this.comp = comp;
	}

	int time1() {
		return time1;
	}

	void setTime1(int time1) {
		this.time1 = time1;
	}

	int low() {
		return low;
	}

	void setLow(int low) {
		this.low = low;
	}

	int dist() {
		return dist;
	}

	void setDist(int dist) {
		this.dist = dist;
	}

	Color color() {
		return color;
	}

	void setColor(Color color) {
		this.color = color;
	}

	Set<Vertex<T>> parents() {
		return parents;
	}

	@Override
	public String toString() {
		return item.toString();
	}

	enum Color {
		BLUE, RED
	}
}
