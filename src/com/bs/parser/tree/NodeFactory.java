package com.bs.parser.tree;

import com.bs.parser.token.Token;

public interface NodeFactory {

	/**
	 * 
	 * @param token
	 * @return
	 */
	IdentifierNode variable(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	NumberNode number(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	StringNode string(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	MessageNode message(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	MessagesNode messages(Token token);

	/**
	 * Expression as a value
	 * 
	 * @param token
	 * @return
	 */
	ExpressionNode expression(Token token);

	/**
	 * A call node
	 * 
	 * @param token
	 * @return
	 */
	CallNode call(Token token);

	/**
	 * A block node
	 * 
	 * @param token
	 * @return
	 */
	BlockNode block(Token token);

	/**
	 * A list of expression
	 * 
	 * @param token
	 * @return
	 */
	ExpressionsNode expressions(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	AssignNode assignment(Token token);

	/**
	 * 
	 * @param token
	 * @return
	 */
	StatementsNode statements(Token token);
}
