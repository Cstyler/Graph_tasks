
import java.util.*;
import java.util.function.Predicate;

enum Tag {
	IDENT, NUMBER,
	EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN_OR_EQUALS, NOT_EQUALS,
	LEFT_PAREN, RIGHT_PAREN, COMMA, COLON, SEMICOLON, QUESTION,
	PLUS, MINUS, MUL, DIV,
	ASSIGN, END_OF_TEXT
}

class Modules {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		in.useDelimiter("\\Z");
		try {
			Parser p = new Parser(in.next());
			p.parse();
			System.out.println(p.calcComponents());
		} catch (SyntaxError e) {
			System.out.println("error");
		}
	}
}

class Parser {
	/*
	Backus–Naur Form:
	<program> ::= <function> <program> | .
	<function> ::= <ident> ( <formal-args-list> ) := <expr> ; .
	<formal-args-list> ::= <ident-list> | .
	<ident-list> ::= <ident> <ident-list'> .
	<ident-list'> ::= , <ident-list> | .
	<expr> ::= <comparison_expr> <expr'> .
	<expr'> ::= ? <comparison_expr> : <expr> | .
	<comparison_expr> ::= <arith_expr> <comparison_expr'> .
	<comparison_expr'> ::= <comparison_op> <arith_expr> | .
	<comparison_op> ::= = | <> | < | > | <= | >= .
	<arith_expr> ::=  <term> <arith_expr'> .
	<arith_expr'> ::= (+ | -) <term> <arith_expr'> | .
	<term> ::= <factor> <term'> .
	<term'> ::= (* | /) <factor> <term'> | .
	<factor> ::= <number> | <ident> <factor'> | ( <expr> ) | - <factor> .
	<factor'> ::= ( <actual_args_list> ) | .
	<actual_args_list> ::= <expr-list> | .
	<expr-list> ::= <expr> <expr-list'>.
	<expr-list'> ::= , <expr-list> | .
	*/

	private final Set<String> argsSet;
	private final Stack<Integer> argCountStack;
	private final Map<String, Integer> functionsMap;
	private final Map<String, Vertex<String>> vertexMap;
	private final Set<String> mustDefine;
	private Token sym;
	private String curIdent, curFunction;
	private Vertex<String> curVertex;

	Parser(String text) throws SyntaxError {
		sym = new Token(text);
		functionsMap = new HashMap<>();
		argsSet = new HashSet<>();
		mustDefine = new HashSet<>();
		vertexMap = new HashMap<>();
		argCountStack = new Stack<>();
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

	int calcComponents() {
		return new Graph<>(vertexMap.values()).calcComponents();
	}

	void parse() throws SyntaxError {
		program();
		if (!mustDefine.isEmpty()) {
			sym.throwError("not defined functions");
		}
		expect(Tag.END_OF_TEXT);
	}

	private void program() throws SyntaxError {
		if (sym.matches(Tag.IDENT)) {
			function();
			program();
		}
	}

	private void function() throws SyntaxError {
		curFunction = sym.toString();
		expect(Tag.IDENT);
		expect(Tag.LEFT_PAREN);
		argsSet.clear();
		formalArgsList();
		functionsMap.put(curFunction, argsSet.size());
		expect(Tag.RIGHT_PAREN);
		expect(Tag.ASSIGN);
		if (mustDefine.contains(curFunction)) {
			mustDefine.remove(curFunction);
		}
		Vertex<String> temp = vertexMap.get(curFunction);
		if (temp == null) {
			curVertex = new Vertex<>(curFunction);
			vertexMap.put(curFunction, curVertex);
		} else {
			curVertex = temp;
		}
		expr();
		expect(Tag.SEMICOLON);
	}

	private void formalArgsList() throws SyntaxError {
		if (sym.matches(Tag.IDENT)) {
			identList();
		}
	}

	private void identList() throws SyntaxError {
		argsSet.add(sym.toString());
		expect(Tag.IDENT);
		identListRecursion();
	}

	private void identListRecursion() throws SyntaxError {
		if (sym.matches(Tag.COMMA)) {
			nextToken();
			identList();
		}
	}

	private void expr() throws SyntaxError {
		comparisonExpr();
		exprRecursion();
	}

	private void exprRecursion() throws SyntaxError {
		if (sym.matches(Tag.QUESTION)) {
			nextToken();
			comparisonExpr();
			expect(Tag.COLON);
			expr();
		}
	}

	private void comparisonExpr() throws SyntaxError {
		arithExpr();
		comparisonExprRecursion();
	}

	private void comparisonExprRecursion() throws SyntaxError {
		if (sym.matches(Tag.EQUALS, Tag.NOT_EQUALS, Tag.LESS_THAN, Tag.GREATER_THAN, Tag.LESS_THAN_OR_EQUALS, Tag.GREATER_THAN_OR_EQUALS)) {
			comparisonOp();
			arithExpr();
		}
	}

	private void comparisonOp() throws SyntaxError {
		if (sym.matches(Tag.EQUALS, Tag.NOT_EQUALS, Tag.LESS_THAN, Tag.GREATER_THAN, Tag.LESS_THAN_OR_EQUALS, Tag.GREATER_THAN_OR_EQUALS)) {
			nextToken();
		} else {
			sym.throwError("comparison operation expected");
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
		} else if (sym.matches(Tag.IDENT)) {
			curIdent = sym.toString();
			nextToken();
			factorRecursion();
		} else if (sym.matches(Tag.LEFT_PAREN)) {
			nextToken();
			expr();
			expect(Tag.RIGHT_PAREN);
		} else if (sym.matches(Tag.MINUS)) {
			nextToken();
			factor();
		} else {
			sym.throwError("number, ident, left parentheses or minus expected");
		}
	}

	private void factorRecursion() throws SyntaxError {
		if (sym.matches(Tag.LEFT_PAREN)) {
			String curIdentFunction = curIdent;
			Vertex<String> temp = vertexMap.get(curIdentFunction);
			if (!curIdentFunction.equals(curFunction)) {
				if (temp != null) {
					curVertex.addVertex(temp);
				} else {
					Vertex<String> v = new Vertex<>(curIdentFunction);
					curVertex.addVertex(v);
					vertexMap.put(curIdentFunction, v);
					mustDefine.add(curIdentFunction);
				}
			}
			nextToken();
			argCountStack.push(0);
			actualArgsList();
			Integer get = functionsMap.get(curIdentFunction);
			int top = argCountStack.pop();
			if (get != null && !get.equals(top)) {
				sym.throwError("number of args don't matches");
			}
			expect(Tag.RIGHT_PAREN);
		} else if (!argsSet.contains(curIdent)) {
			sym.throwError("wrong variable name");
		}
	}

	private void actualArgsList() throws SyntaxError {
		if (sym.matches(Tag.NUMBER, Tag.IDENT, Tag.LEFT_PAREN, Tag.MINUS)) {
			incArgCount();
			exprList();
		}
	}

	private void exprList() throws SyntaxError {
		expr();
		exprListRecursion();
	}

	private void exprListRecursion() throws SyntaxError {
		if (sym.matches(Tag.COMMA)) {
			nextToken();
			incArgCount();
			exprList();
		}
	}

	private void incArgCount() {
		argCountStack.push(argCountStack.pop() + 1);
	}
}

class Graph<T> {
	private final Collection<Vertex<T>> vertices;

	Graph(Collection<Vertex<T>> vertices) {
		this.vertices = vertices;
	}

	int calcComponents() {
		return new StronglyConnectedComponent<>(vertices).getCount() - 1;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("digraph {\n");
		for (Vertex<T> vertex : vertices) {
			s.append("\t").append(vertex.toString()).append(";\n");
			for (Vertex<T> outEdge : vertex.outEdges()) {
				s.append("\t").append(vertex).append(" -> ").append(outEdge).append(";\n");
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

	int getCount() {
		return count;
	}

	private void Tarjan() {
		vertices.stream().filter(v -> v.time1() == 0).forEach(this::visitVertexTarjan);
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

class Vertex<T> {
	private final List<Vertex<T>> outEdges;
	private final T item;
	private int comp, time1, low;

	Vertex(T item) {
		this.item = item;
		comp = time1 = 0;
		outEdges = new LinkedList<>();
	}

	List<Vertex<T>> outEdges() {
		return outEdges;
	}

	@Override
	public String toString() {
		return item + ", " + comp();
	}

	void addVertex(Vertex<T> v) {
		outEdges.add(v);
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
		start = currentPos.skipWhile(Character::isWhitespace);
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
				tag = Tag.EQUALS;
				break;
			case '<':
				if (follow.satisfies(x -> x == '=')) {
					follow = follow.skip();
					tag = Tag.LESS_THAN_OR_EQUALS;
				} else if (follow.satisfies(x -> x == '>')) {
					follow = follow.skip();
					tag = Tag.NOT_EQUALS;
				} else {
					tag = Tag.LESS_THAN;
				}
				break;
			case '>':
				if (follow.satisfies(x -> x == '=')) {
					follow = follow.skip();
					tag = Tag.GREATER_THAN_OR_EQUALS;
				} else {
					tag = Tag.GREATER_THAN;
				}
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
			case ':':
				if (follow.satisfies(x -> x == '=')) {
					follow = follow.skip();
					tag = Tag.ASSIGN;
				} else {
					tag = Tag.COLON;
				}
				break;
			case ';':
				tag = Tag.SEMICOLON;
				break;
			case '?':
				tag = Tag.QUESTION;
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
