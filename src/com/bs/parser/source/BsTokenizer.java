package com.bs.parser.source;

import org.apache.commons.lang3.StringEscapeUtils;

import com.bs.parser.token.Token;
import com.bs.parser.token.TokenFactory;
import com.bs.parser.token.TokenType;
import com.bs.util.Message;
import com.bs.util.MessageHandler;
import com.bs.util.MessageType;

public class BsTokenizer implements Tokenizer {

	private Token next, current;
	private Scanner scanner;
	private TokenFactory factory;

	private char comment;
	private MessageHandler messageHandler;

	public BsTokenizer(Scanner s, TokenFactory factory, MessageHandler handler,
			char comment) {
		this.scanner = s;
		this.comment = comment;
		this.factory = factory;
		this.messageHandler = handler;
	}

	public Scanner scanner() {
		return this.scanner;
	}

	@Override
	public Token current() {
		return current;
	}

	@Override
	public Token peek() {
		if (next == null) {
			next = extract();
		}

		return next;
	}

	@Override
	public Token next() {
		if (next != null) {
			current = next;
			next = null;
		} else {
			current = extract();
		}

		return current();
	}

	protected Token extract() {
		Token token = null;
		consumeWhitespace();
		char current = scanner.current();
		int line = scanner.line();
		if (current == Scanner.EOF) {
			token = factory.eof();
			line -= 1;
		} else if (validIdentifierStart(current)) {
			token = extractIdentifier();
		} else if (Character.isDigit(current)) {
			token = extractNumber();
		} else if (current == ':' && scanner.peek() != '=') {
			token = extractSymbol();
		} else if (TokenType.isSpecial(current)) {
			token = extractSpecial();
		} else if (current == '"') {
			token = extractString();
		} else if (current == '\'') {
			token = extractCharacter();
		} else {
			messageHandler().error(scanner(), MessageType.SYNTAX_ERROR,
					Message.UNEXPECTED_TOKEN, String.valueOf(current));
			scanner().next(); // skip.. (and recover?)
			token = factory.error(scanner().line(), scanner().position());

		}
		token.currentLine(scanner.line(line - 1));
		return token;
	}

	protected Token extractCharacter() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().next();
		while (c != '\'') {
			builder.append(c);
			c = scanner().next();
		}
		scanner().next();

		String str = StringEscapeUtils.unescapeJava(builder.toString());
		if (str.length() > 1) {
			messageHandler.error(scanner(), MessageType.SYNTAX_ERROR,
					Message.UNEXPECTED_TOKEN, str);
			return factory.error(scanner().line(), scanner().position());
		} else {
			return factory.character(str, str.charAt(0), scanner().line(),
					scanner().position());
		}

	}

	protected Token extractSymbol() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().next();
		while (validIdentifierStart(c, false)) {
			builder.append(c);
			
			c = scanner().next();
			if (c == '.' && !validIdentifierStart(scanner().peek(), false)) {
				break;
			}
		}

		return factory.symbol(
				StringEscapeUtils.unescapeJava(builder.toString()),
				scanner.line(), scanner.position());
	}

	public MessageHandler messageHandler() {
		return messageHandler;
	}

	protected Token extractString() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().next();
		while (c != '"') {
			builder.append(c);
			c = scanner().next();
		}
		scanner.next(); // consume "
		return factory.string(
				StringEscapeUtils.unescapeJava(builder.toString()),
				scanner.line(), scanner.position());
	}

	protected Token extractSpecial() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().current();
		char n = scanner().peek();

		builder.append(c);
		if (c == ':' && n == '=') {
			scanner().next();
			builder.append(n);
		}

		scanner().next();
		return factory.special(builder.toString(), scanner.line(),
				scanner.position());
	}

	private Token extractNumber() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().current();
		boolean hasDot = false;
		while (Character.isDigit(c)
				|| (c == '.' && Character.isDigit(scanner.peek()) && !hasDot)) {
			if (c == '.') {
				hasDot = true;
			}

			builder.append(c);
			c = scanner().next();
		}

		String value = builder.toString();
		Number number = null;
		try {
			number = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			try {
				number = Long.parseLong(value);
			} catch (NumberFormatException e2) {
				try {
					number = Double.parseDouble(value);
				} catch (Exception e3) {
					messageHandler().error(scanner(), MessageType.SYNTAX_ERROR,
							Message.UNEXPECTED_NUMBER, value);
					return factory
							.error(scanner().line(), scanner().position());
				}
			}
		}

		return factory.number(value, number, scanner().line(), scanner()
				.position());
	}

	private Token extractIdentifier() {
		StringBuilder builder = new StringBuilder();
		char c = scanner().current();
		while (validIdentifierStart(c, false)) {
			builder.append(c);

			c = scanner().next();
			if (c == '.' && !validIdentifierStart(scanner().peek(), false)) {
				break;
			}
		}

		return factory.identifier(builder.toString(), scanner().line(),
				scanner().position());
	}

	protected boolean validIdentifierStart(char current, boolean isStart) {
		if (Character.isLetter(current)) {
			return true;
		} else if (!isStart && (Character.isDigit(current) || current == '.')) {
			return true;
		} else {
			switch (current) {
			case '$':
			case '%':
			case '*':
			case '+':
			case '-':
			case '/':
			case '<':
			case '>':
			case '=':
			case '\\':
			case '&':
			case '?':
			case '!':
			case '_':
				return true;
			default:
				return false;
			}
		}
	}

	protected boolean validIdentifierStart(char current) {
		return validIdentifierStart(current, true);
	}

	protected void consumeWhitespace() {
		char current = scanner.current();
		while (Character.isWhitespace(current) || current == this.comment) {
			if (current == this.comment) {
				do {
					current = scanner().next();
				} while (current != Scanner.EOL && current != Scanner.EOF);
				if (current == Scanner.EOL) {
					current = scanner().next();
				}

			} else {
				current = scanner().next();
			}
		}
	}
}
