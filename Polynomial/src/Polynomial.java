import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author Sergey Savelyev
 *
 */
public class Polynomial {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			Console console = System.console();

			if (console == null) {
	            System.err.println("No console.");
	            System.exit(1);
	        }
			
			if (args.length == 2) {
				if (args[0] != null && !"".equals(args[0]) &&
					args[1] != null && !"".equals(args[1])) {
					
					Path inFile = Paths.get(args[0]);
					BufferedReader reader = Files.newBufferedReader(inFile);
					
					Path outFile = Paths.get(args[1]);
					BufferedWriter writer = Files.newBufferedWriter(outFile);
					
				    String line, polynom;
				    
				    while ((line = reader.readLine()) != null) {
				        polynom = run(line);
				        
			        	System.out.println(polynom);
			        	
				        writer.write(polynom);
				        writer.newLine();
				    }
				    
				    reader.close();
				    writer.close();
				    
				    console.readLine("Нажмите Enter для завершения...");
				    System.exit(0);
				}
			}
			
			System.out.println("Наберите help, чтобы посмотреть справку.");
			
			String source = "(((x^2 - 3*x)*3 - 4*x^2 + 5*x*(2))*(x^3)^4)", polynom;
			
			while (!"quit".equals(source = console.readLine("Введите полином: "))) {
				
				if ("help".equals(source)) {
					showHelp();
					continue;
				}
				
				polynom = run(source);
				
				System.out.println(polynom);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	private static void showHelp() {
		String help = "";
		
		help += "Программа приводит исходный полином к виду: cN * x^N + cN-1 * x^N-1 + ... + c2 * x^2 + c1 * x^1 + c0 * x^0\n";
		help += "Пример входных данных:    \tПример выходных данных:\n";
		help += "(x - 5)(2x^3 + x(x^2 - 9))\t3x^4 - 15x^3 - 9x^2 + 45x\n";
		help += "Программа позволяет вводить входные данные из файла и выводить результат в файл.\n";
		help += "Для этого необходимо при вызове программы передать два аргумента.\n";
		help += "Пример:\n";
		help += "\t" + "java Polynomial input.txt output.txt\n";
		help += "где\n";
		help += "\t" + "input.txt  - имя файла, содержащее входные данные в формате, описанном выше\n";
		help += "\t" + "output.txt - имя файла, куда будет записан результат\n";
		help += "\n";
		help += "Команды, поддерживаемые программой:\n";
		help += "\t" + "help - справка\n";
		help += "\t" + "quit - выйти из программы\n";
		
		System.out.print(help);
	}
	
	/**
	 * Чтобы преобразовать многочлен необходимо выполнить:
	 * 1. Лексический анализ входных данных
	 * 2. Синтаксический анализ полученных лексем (токенов)
	 * 3. Арифметические действия над узлами дерева разбора
	 * 
	 * @param source
	 * @return
	 */
	public static String run(String source) throws Exception {
		source = source.replaceAll("[\\s]+", "");
		
		/*
		 * Лексический анализ
		 */
		
		// Определить шаблоны на основе регулярных выражений для каждого токена
		EnumMap<TokenType, Pattern> patterns = new EnumMap<TokenType, Pattern>(TokenType.class);
		
		patterns.put(TokenType.NUMBER, Pattern.compile("[\\d]+(\\.[\\d]+)?"));
		patterns.put(TokenType.VARIABLE, Pattern.compile("[Xx]"));
		patterns.put(TokenType.ADD, Pattern.compile("[+]"));
		patterns.put(TokenType.SUBTRACT, Pattern.compile("[-]"));
		patterns.put(TokenType.MULTIPLY, Pattern.compile("[*]"));
		patterns.put(TokenType.POWER, Pattern.compile("[\\^]"));
		patterns.put(TokenType.OBRACKET, Pattern.compile("[(]"));
		patterns.put(TokenType.CBRACKET, Pattern.compile("[)]"));
		
		ArrayList<Token> tokenList = new ArrayList<Token>();
		Matcher matcher;
		
		int start = 0,		// Начальная позиция в исходном выражении, с которой начинать поиск
			brackets = 0;	// Проверка кол-ва открывающих и закрывающих скобок

		boolean stop;

		// Выполнить поиск на основе регулярных выражений до достижения конца исходного выражения
		do {
			stop = true;
			
			for (Entry<TokenType, Pattern> entry : patterns.entrySet()) {
				matcher = entry.getValue().matcher(source);
				
				if (matcher.find(start) && matcher.start() == start) {
					start = matcher.end();
					
					// Достигнут конец строки
					if (start < source.length()) {
						stop = false;
					}
					
					Token token = new Token(entry.getKey());
					
					// Для числа следует запомнить его значение
					if (token.type == TokenType.NUMBER) {
						token.value = matcher.group();
					}
					
					/*
					 * Проверить грамматическую верность выражения, сравнивая
					 * текущий токен и предыдущий.
					 * При необходимости добавить оператор умножения или недостающий ноль.
					 */
					Token previous = null;
					
					if (tokenList.size() > 0) {
						previous = tokenList.get(tokenList.size() - 1);
					}
					
					boolean isFirst = previous == null;	// Токен token является первым в выражении
					boolean isLast = stop;				// Токен token является последним в выражении
					
					switch (token.type) {
					
					case NUMBER:
						if (!isFirst) {
							/*
							 * Вставить оператор умножения между:
							 * 1. Переменной и числом
							 * 2. Закрывающей скобкой и числом
							 */
							if (previous.type == TokenType.VARIABLE ||
								previous.type == TokenType.CBRACKET) {
								
								tokenList.add(new Token(TokenType.MULTIPLY));
							}
						}
						break;
						
					case VARIABLE:
						if (!isFirst) {
							/*
							 * Вставить оператор умножения между:
							 * 1. Числом и переменной
							 * 2. Переменной и переменной
							 * 3. Закрывающей скобкой и переменной
							 */
							if (previous.type == TokenType.NUMBER ||
								previous.type == TokenType.VARIABLE ||
								previous.type == TokenType.CBRACKET) {
								
								tokenList.add(new Token(TokenType.MULTIPLY));
							}
							else if (previous.type == TokenType.POWER) {
								throw new Exception("Степень должна быть константой.");
							}
						}
						break;
						
					case ADD:
					case SUBTRACT:
						if (isFirst) {
							// Добавить ноль в качестве первого токена
							tokenList.add(new Token(TokenType.NUMBER, "0"));
						}
						else {
							if (isLast) {
								throw new Exception("Не указан правый операнд для бинарного оператора.");
							}
							
							if (previous.type == TokenType.OBRACKET) {
								tokenList.add(new Token(TokenType.NUMBER, "0"));
							}
							else if (previous.type == TokenType.ADD ||
									 previous.type == TokenType.SUBTRACT ||
									 previous.type == TokenType.MULTIPLY ||
									 previous.type == TokenType.POWER) {

								throw new Exception("Не указан правый операнд для бинарного оператора.");
							}
						}
						break;
						
					case MULTIPLY:
					case POWER:
						if (isFirst) {
							throw new Exception("Не указан левый операнд для бинарного оператора.");
						}
						else {
							if (isLast) {
								throw new Exception("Не указан правый операнд для бинарного оператора.");
							}
							
							if (previous.type == TokenType.ADD ||
								previous.type == TokenType.SUBTRACT ||
								previous.type == TokenType.MULTIPLY ||
								previous.type == TokenType.POWER) {
								
								throw new Exception("Не указан правый операнд для бинарного оператора.");
							}
						}
						break;
						
					case OBRACKET:
						brackets++;
						
						if (!isFirst) {
							if (isLast) {
								throw new Exception("Не указана закрывающая скобка.");
							}
							
							/*
							 * Вставить оператор умножения между:
							 * 1. Числом и открывающей скобкой
							 * 2. Переменной и открывающей скобкой
							 * 3. Закрывающей скобкой и открывающей скобкой
							 */
							if (previous.type == TokenType.NUMBER ||
								previous.type == TokenType.VARIABLE ||
								previous.type == TokenType.CBRACKET) {
								
								tokenList.add(new Token(TokenType.MULTIPLY));
							}
						}
						break;
						
					case CBRACKET:
						brackets--;
						
						if (isFirst) {
							throw new Exception("Не указана открывающая скобка.");
						}
						else {
							if (previous.type == TokenType.ADD ||
								previous.type == TokenType.SUBTRACT ||
								previous.type == TokenType.MULTIPLY ||
								previous.type == TokenType.POWER) {
									
								throw new Exception("Не указан правый операнд для бинарного оператора.");
							}
							else if (previous.type == TokenType.OBRACKET) {
								throw new Exception("Указаны пустые скобки.");
							}
						}
						break;
						
					}
					
					// Добавить корректный токен в список
					tokenList.add(token);
					
					break;
				}
			}
		} while (!stop);

		// Если исходное выражение содержит недопустимые символы, то конец строки не будет достигнут
		if (start < source.length()) {
			throw new Exception("Входные данные содержат недопустимые символы: " + source.substring(start));
		}
		
		// Количество открывающих и закрывающих скобок не совпадает
		if (brackets != 0) {
			throw new Exception("Количество открывающих и закрывающих скобок не совпадает.");
		}

		//logTokenList(tokenList);
		
		/*
		 * Синтаксический анализ
		 */
		
		/*
		 * Создать дерево разбора на основе полученного списка токенов.
		 * Узел current может ссылаться только на операторы сложения, вычитания, умножения,
		 * возведение в степень и открывающую скобку, но есть исключения:
		 * 1. Если число или переменная являются первым токеном в выражении, в таком случае узел current также будет
		 * ссылаться на них в начале формирования дерева.
		 * 2. Узел current может ссылаться на закрывающую скобку, только если он является корневым узлом всего дерева.
		 */
		Node current = null;
		
		for (int i = 0; i < tokenList.size(); i++) {
			Node newNode = new Node(tokenList.get(i));
			
			switch (newNode.token.type) {
			
			case NUMBER:
			case VARIABLE:
			case OBRACKET:
				if (current == null) {
					current = newNode;
				}
				else {
					current.setChild(newNode, false);
					
					if (newNode.token.type == TokenType.OBRACKET) {
						current = newNode;
					}
				}
				break;
				
			case ADD:
			case SUBTRACT:
			case MULTIPLY:
			case POWER:
				if (current.token.type == TokenType.NUMBER ||
					current.token.type == TokenType.VARIABLE) {
					
					newNode.setChild(current, true);
				}
				else if (current.token.type == TokenType.OBRACKET) {
					newNode.setChild(current.getRight(), true);
					current.setChild(newNode, false);
				}
				else if (current.token.type == TokenType.CBRACKET) {
					newNode.setChild(current.getRight(), true);
				}
				else {
					boolean isHigher = false;	// Текущий оператор имеет более высокий приоритет, чем предыдущий
					
					if (newNode.token.type == TokenType.POWER &&
						current.token.type != TokenType.POWER) {
						
						isHigher = true;
					}
					
					if (!isHigher) {
						if (newNode.token.type == TokenType.MULTIPLY &&
								(current.token.type == TokenType.ADD || current.token.type == TokenType.SUBTRACT)) {
							
							isHigher = true;
						}
					}
					
					if (isHigher) {
						newNode.setChild(current.getRight(), true);
						current.setChild(newNode, false);
					}
					else {
						// Найти ближайший узел среди предков, приоритет которого меньше или равен текущему
						current = current.getLessOrEqualPriority(newNode.token.type);
						
						Node parent = current.getParent();
						
						newNode.setChild(current, true);
						
						if (parent != null) {
							parent.setChild(newNode, false);
						}
					}
				}
				current = newNode;
				break;

			case CBRACKET:
				current = current.getOpenBracket();
				
				Node parent = current.getParent();
				
				if (parent != null) {
					parent.setChild(current.getRight(), false);
					current = parent;
				}
				else {
					newNode.setChild(current.getRight(), false);
					current = newNode;
				}
				break;
				
			}
		}
		
		Node root = current.getRoot();	// Корневой узел дерева
		//root.log();

		/*
		 * Арифметические действия
		 */
		
		// Представить корень ввиде суммы одночленов
		ArrayList<Monomial> resultList = root.getValue();

		// Упорядочить сумму по убыванию степеней
		Collections.sort(resultList, new Comparator<Monomial>() {
			@Override
			public int compare(Monomial item1, Monomial item2)
			{
				return Integer.compare(item2.degree, item1.degree);
			}
		});

		// Получить строковое представление конечного многочлена
		String polynom = "";
		
		for (Monomial item : resultList) {
			if (item.coefficient != 0) {
				
				// Добавить символы операторов сложения, вычитания
				if (item.coefficient > 0) {
					if (polynom.length() > 0) {
						polynom += " + ";
					}
				}
				else {
					if (polynom.length() > 0) {
						polynom += " - ";
					}
					else {
						polynom = "-";
					}
				}
				
				// Добавить коэффициент
				double c = item.coefficient;
				
				if (c < 0) {
					c *= -1;
				}
				
				if (c != 1 || item.degree == 0) {
					if (c % 1 == 0) {
						polynom += (int)c;
					}
					else {
						polynom += c;
					}
				}
				
				// Добавить степень
				if (item.degree > 0) {
					polynom += "x";
					
					if (item.degree > 1) {
						polynom += "^" + item.degree;
					}
				}
			}
		}
		
		//Double value = getValue(resultList, 3.0);
		//System.out.println(value);

		return polynom;
	}
	
	private static void logTokenList(ArrayList<Token> tokenList) {
		String message = "List of tokens: ";
		
		for (int i = 0; i < tokenList.size(); i++) {
			String val = tokenList.get(i).value;
			
			if (val != null) {
				message += tokenList.get(i).value;
			}
			else {
				switch (tokenList.get(i).type) {
				
				case VARIABLE:
					message += "X";
					break;
					
				case ADD:
					message += "+";
					break;
					
				case SUBTRACT:
					message += "-";
					break;
					
				case MULTIPLY:
					message += "*";
					break;
					
				case POWER:
					message += "^";
					break;
					
				case OBRACKET:
					message += "(";
					break;
					
				case CBRACKET:
					message += ")";
					break;
				}
			}
		}
		
		System.out.println(message);
	}

	private static double getValue(ArrayList<Monomial> polynom, double X) {
		double val = 0;
		
		for (Monomial item : polynom) {
			val += item.coefficient * Math.pow(X, item.degree);
		}
		
		return val;
	}
	
}
