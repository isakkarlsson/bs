package com.bs.parser.tree;

import java.util.List;

import com.bs.interpreter.Interpreter;
import com.bs.parser.token.Token;

/**
 * Node representing a String
 * 
 * @author Isak Karlsson
 * 
 */
public class StringNode extends AbstractNode implements LiteralNode {

	private String string;

	public StringNode(Token token) {
		super(token);
	}

	public void string(String string) {
		this.string = string;
	}

	public String string() {
		return this.string;
	}

	@Override
	public Object visit(Interpreter visitor) {
		return visitor.interpretString(this);
	}

	@Override
	public String toTree() {
		return "String(value=" + string + ")";
	}

	@Override
	public List<Node> childrens() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object value() {
		return string();
	}

}
