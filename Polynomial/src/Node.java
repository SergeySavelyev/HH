import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Sergey Savelyev
 *
 */
class Node {
	
	private Node parent;
	private Node childLeft;
	private Node childRight;
	
	public Token token;
	
	Node (Token token) {
		this.token = token;
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public Node getLeft() {
		return this.childLeft;
	}
	
	public Node getRight() {
		return this.childRight;
	}
	
	public void setChild(Node child, boolean isLeft) {
		if (isLeft) {
			this.childLeft = child;
		}
		else {
			this.childRight = child;
		}
		
		child.setParent(this);
	}
	
	/**
	 * Получить ближайший узел среди предков, приоритет которого меньше или равен текущему узлу.
	 * Приоритет по убыванию:
	 * POWER > MULTIPLY > ADD, SUBTRACT
	 * 
	 * @param type
	 * @return
	 */
	public Node getLessOrEqualPriority(TokenType type) {
		if (this.token.type == type) {
			return this;
		}
		
		switch (this.token.type) {
		
		case ADD:
		case SUBTRACT:
			return this;
			
		case MULTIPLY:
			if (type == TokenType.POWER) {
				return this;
			}
			break;

		}
				
		if (this.parent != null && this.parent.token.type != TokenType.OBRACKET) {
			return this.parent.getLessOrEqualPriority(type);
		}
		
		return this;
	}
	
	/**
	 * Получить ближайшую открывающую скобку среди предков
	 * 
	 * @return
	 */
	public Node getOpenBracket() {
		if (this.token.type == TokenType.OBRACKET) {
			return this;
		}
		
		if (this.parent != null) {
			return this.parent.getOpenBracket();
		}
		
		return null;
	}
	
	public Node getRoot() {
		if (this.parent != null) {
			return this.parent.getRoot();
		}
		
		return this;
	}
	
	/**
	 * Представить узел ввиде суммы одночленов
	 * 
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Monomial> getValue() throws Exception {
		ArrayList<Monomial> itemList = new ArrayList<Monomial>();
		
		switch (this.token.type) {
		
		case NUMBER:
			itemList.add(new Monomial(Double.parseDouble(this.token.value), 0));
			break;
			
		case VARIABLE:
			itemList.add(new Monomial(1.0, 1));
			break;
			
		
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case POWER:
			ArrayList<Monomial> left = this.getLeft().getValue();
			ArrayList<Monomial> right = this.getRight().getValue();
			
			if (this.token.type == TokenType.ADD ||
				this.token.type == TokenType.SUBTRACT) {
				
				itemList = left;
				
				for (Monomial rightItem : right) {
					boolean isAbsent = true;
					
					for (Monomial item : itemList) {
						if (item.degree == rightItem.degree) {
							isAbsent = false;
							
							if (this.token.type == TokenType.ADD) {
								item.coefficient += rightItem.coefficient;
							}
							else {
								item.coefficient -= rightItem.coefficient;
							}
							
							break;
						}
					}
					
					if (isAbsent) {
						if (this.token.type == TokenType.SUBTRACT) {
							rightItem.coefficient *= -1;
						}
						
						itemList.add(rightItem);
					}
				}
			}
			else if (this.token.type == TokenType.MULTIPLY) {
				itemList = multipleLists(left, right);
			}
			else if (this.token.type == TokenType.POWER) {
				int degree = (int)right.get(0).coefficient;
				
				for (Monomial item : right) {
					if (item.degree > 0 && item.coefficient != 0) {
						throw new Exception("Переменная степень недопустима");
					}
					
					if (item.degree == 0) {
						if (item.coefficient % 1 != 0) {
							throw new Exception("Дробная степень не допустима");
						}
						
						degree = (int)item.coefficient;
					}
				}
				
				itemList.add(new Monomial(1.0, 0));
				
				for (int c = 1; c <= degree; c++) {
					itemList = multipleLists(itemList, left);
				}
			}
			break;
			
		case CBRACKET:
			return this.getRight().getValue();
		}
		
		return itemList;
	}
	
	public void log() {
		this.log(0);
	}
	
	private ArrayList<Monomial> multipleLists(ArrayList<Monomial> left, ArrayList<Monomial> right) {
		ArrayList<Monomial> resultList = new ArrayList<Monomial>();
		
		for (int i = 0; i < left.size(); i++) {
			Monomial leftItem = left.get(i);
			
			for (int j = 0; j < right.size(); j++) {
				Monomial rightItem = right.get(j);
				
				Monomial resultItem = new Monomial(leftItem.coefficient * rightItem.coefficient, leftItem.degree + rightItem.degree);
				
				boolean isAbsent = true;
				
				for (Monomial item : resultList) {
					if (item.degree == resultItem.degree) {
						isAbsent = false;
						
						item.coefficient += resultItem.coefficient;
						
						break;
					}
				}
				
				if (isAbsent) {
					resultList.add(resultItem);
				}
			}
		}
		
		return resultList;
	}
	
	private void log(int level) {
		String message = this.token.type.name();
		
		for (int i = 0; i < level; i++) {
			message = "\t" + message;
		}
		
		if (this.token.type == TokenType.NUMBER) {
			message += "(" + this.token.value + ")";
		}
		
		System.out.println(message);
		
		level++;
		
		switch (this.token.type) {

		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case POWER:
			if (this.childLeft != null) {
				this.childLeft.log(level);
			}
			
			if (this.childRight != null) {
				this.childRight.log(level);
			}
			break;
			
		case OBRACKET:
		case CBRACKET:
			if (this.childRight != null) {
				this.childRight.log(level);
			}
			break;
			
		}
	}

}
