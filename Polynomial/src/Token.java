/**
 * 
 */

/**
 * @author Sergey Savelyev
 *
 */
class Token {
	
	public TokenType type;
	public String value;
	
	Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	Token(TokenType type) {
		this(type, null);
	}
	
}

enum TokenType {
	NUMBER,
	VARIABLE,
	ADD,
	SUBTRACT,
	MULTIPLY,
	POWER,
	OBRACKET,
	CBRACKET
}
