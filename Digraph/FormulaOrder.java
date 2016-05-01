
import java.util.*;
import java.util.function.Predicate;

enum Color {WHITE, GRAY, BLACK}

enum Tag {
	IDENT, NUMBER,
	LEFT_PAREN, RIGHT_PAREN, COMMA, ASSIGN,
	PLUS, MINUS, MUL, DIV,
	NEWLINE, END_OF_TEXT
}

class FormulaOrder {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		in.useDelimiter("\\Z");
		try {
			Parser p = new Parser(in.next() + '\n');
			Graph<String> g = new Graph<>(p.parse());
			for (Vertex<String> vertex : g.DFS()) {
				System.out.println(vertex);
			}
		} catch (SyntaxError e) {
			System.out.println("syntax error");
		} catch (RuntimeException e) {
			System.out.println("cycle");
		}
	}
}

class Graph<T> {
	private final Collection<Vertex<T>> vertices;

	Graph(Collection<Vertex<T>> vertices) {
		this.vertices = vertices;
	}

	Stack<Vertex<T>> DFS() {
		Stack<Vertex<T>> order = new Stack<>();
		for (Vertex<T> vertex : vertices) {
			if (vertex.color() == Color.WHITE) {
				visitVertex(vertex, order);
			}
		}
		return order;
	}

	private void visitVertex(Vertex<T> v, Stack<Vertex<T>> order) {
		v.setColor(Color.GRAY);
		for (Vertex<T> u : v.outEdges()) {
			if (u.color() == Color.WHITE) {
				visitVertex(u, order);
			} else if (u.color() == Color.GRAY) {
				throw new RuntimeException("cycle");
			}
		}
		order.push(v);
		v.setColor(Color.BLACK);
	}
}

class Parser {
	private final Scanner scn;
	private Token sym;
	private Map<String, Vertex<String>> definedVertexMap;
	private Map<String, List<Vertex<String>>> variableToFormulaMap;
	private Collection<Vertex<String>> vertices;
	private int leftIdent, rightIdent;
	private Vertex<String> curFormula;

	Parser(String text) throws SyntaxError {
		this.scn = new Scanner(text);
		sym = new Token(text);
		variableToFormulaMap = new HashMap<>();
		vertices = new ArrayList<>();
		definedVertexMap = new HashMap<>();
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
	<formulas> ::= <formula> <formulas> | .
	<formula> ::= <ident'> = <expr> '\n' | .
	<ident'> ::= <ident> <ident''>.
	<ident''> ::= , <ident'> | .
	<expr> ::= <arith_expr> <expr'> .
	<expr'> ::= , <expr> | .
	<arith_expr> ::=  <term> <arith_expr'> .
	<arith_expr'> ::= (+ | -) <term> <arith_expr'> | .
	<term> ::= <factor> <term'> .
	<term'> ::= (* | /) <factor> <term'> | .
	<factor> ::= <number> | <ident> | ( <expr> ) | - <factor>.
	*/

	Collection<Vertex<String>> parse() throws SyntaxError {
		formulas();
		expect(Tag.END_OF_TEXT);
		if (!variableToFormulaMap.isEmpty()) {
			sym.throwError("undefined functions");
		}
		return vertices;
	}

	private void formulas() throws SyntaxError {
		// using cycle to avoid stack overflow
		while (true) {
			if (!sym.matches(Tag.IDENT)) {
				break;
			}
			formula();
		}
	}

	private void formula() throws SyntaxError {
		curFormula = new Vertex<>(scn.nextLine());
		vertices.add(curFormula);
		leftIdent = 0;
		ident();
		expect(Tag.ASSIGN);
		rightIdent = 0;
		expr();
		if (leftIdent != rightIdent) {
			sym.throwError("number of variables don't match");
		}
		expect(Tag.NEWLINE);
	}

	private void ident() throws SyntaxError {
		String s = sym.toString();
		Vertex<String> get = definedVertexMap.get(s);
		if (get != null) {
			sym.throwError("twice defined variable");
		}
		definedVertexMap.put(s, curFormula);
		List<Vertex<String>> temp = variableToFormulaMap.get(s);
		if (temp != null) {
			for (Vertex<String> v : temp) {
				v.addVertex(curFormula);
			}
			variableToFormulaMap.remove(s);
		}
		expect(Tag.IDENT);
		leftIdent++;
		identRecursion();
	}

	private void identRecursion() throws SyntaxError {
		if (sym.matches(Tag.COMMA)) {
			nextToken();
			ident();
		}
	}

	private void expr() throws SyntaxError {
		rightIdent++;
		arithExpr();
		exprRecursion();
	}

	private void exprRecursion() throws SyntaxError {
		if (sym.matches(Tag.COMMA)) {
			nextToken();
			expr();
		}
	}

	private void arithExpr() throws SyntaxError {
		term();
		arithExprRecursion();
	}

	private void arithExprRecursion() throws SyntaxError {
		if (sym.matches(Tag.PLUS, Tag.MINUS)) {
			nextToken();
			term();
			arithExprRecursion();
		}
	}

	private void term() throws SyntaxError {
		factor();
		termRecursion();
	}

	private void termRecursion() throws SyntaxError {
		if (sym.matches(Tag.MUL, Tag.DIV)) {
			nextToken();
			factor();
			termRecursion();
		}
	}

	private void factor() throws SyntaxError {
		if (sym.matches(Tag.NUMBER)) {
			nextToken();
		} else if (sym.matches(Tag.LEFT_PAREN)) {
			nextToken();
			arithExpr();
			expect(Tag.RIGHT_PAREN);
		} else if (sym.matches(Tag.MINUS)) {
			nextToken();
			factor();
		} else if (sym.matches(Tag.IDENT)) {
			String s = sym.toString();
			Vertex<String> get = definedVertexMap.get(s);
			if (get == null) {
				List<Vertex<String>> temp = variableToFormulaMap.get(s);
				if (temp != null) {
					temp.add(curFormula);
				} else {
					List<Vertex<String>> l = new LinkedList<>();
					l.add(curFormula);
					variableToFormulaMap.put(s, l);
				}
			} else {
				curFormula.addVertex(get);
			}
			nextToken();
		} else {
			sym.throwError("number, ident, left parentheses or minus expected");
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

class SyntaxError extends Exception {
	SyntaxError(String message, Position pos) {
		super(String.format("Syntax error at %s: %s", pos.toString(), message));
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
		start = currentPos.skipWhile(c -> c == ' ' || c == '\t');
		follow = start.skip();
		switch (start.getChar()) {
			case -1:
				tag = Tag.END_OF_TEXT;
				break;
			case '+':
				tag = Tag.PLUS;
				break;
			case '-':
				tag = Tag.MINUS;
				break;
			case '*':
				tag = Tag.MUL;
				break;
			case '/':
				tag = Tag.DIV;
				break;
			case '=':
				tag = Tag.ASSIGN;
				break;
			case '(':
				tag = Tag.LEFT_PAREN;
				break;
			case ')':
				tag = Tag.RIGHT_PAREN;
				break;
			case ',':
				tag = Tag.COMMA;
				break;
			case '\n':
				tag = Tag.NEWLINE;
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

class Vertex<T> {
	private final Set<Vertex<T>> outEdges;
	private final T item;
	private Color color;

	Vertex(T item) {
		this.item = item;
		color = Color.WHITE;
		outEdges = new HashSet<>();
	}

	Set<Vertex<T>> outEdges() {
		return outEdges;
	}

	@Override
	public String toString() {
		return item.toString();
	}

	void addVertex(Vertex<T> v) {
		outEdges.add(v);
	}

	Color color() {
		return color;
	}

	void setColor(Color color) {
		this.color = color;
	}
}
