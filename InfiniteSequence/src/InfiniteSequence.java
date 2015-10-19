import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 */

/**
 * @author Sergey Savelyev
 *
 */
public class InfiniteSequence {

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
					
				    String line, position;
				    
				    while ((line = reader.readLine()) != null) {
				    	position = run(line);
				        
			        	System.out.println(position);
			        	
				        writer.write(position);
				        writer.newLine();
				    }
				    
				    reader.close();
				    writer.close();
				    
				    console.readLine("Нажмите Enter для завершения...");
				    System.exit(0);
				}
			}
			
			System.out.println("Наберите help, чтобы посмотреть справку.");
			
			String wanted, position;
			
			while (!"quit".equals(wanted = console.readLine("Последовательность A: "))) {
				
				if ("help".equals(wanted)) {
					showHelp();
					continue;
				}
				
				position = run(wanted);
				
				System.out.println(position);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	private static void showHelp() {
		String help = "";
		
		help += "Программа определяет первое вхождение заданной последовательности A в бесконечной последовательности S (нумерация начинается с 1).\n";
		help += "Бесконечная последовательность S образована склеиванием последовательных положительных чисел: S = 123456789101112131415...\n";
		help += "Пример входных данных:\tПример выходных данных:\n";
		help += "6789                  \t6\n";
		help += "111                   \t12\n";
		help += "Программа позволяет вводить входные данные из файла и выводить результат в файл.\n";
		help += "Для этого необходимо при вызове программы передать два аргумента.\n";
		help += "Пример:\n";
		help += "\t" + "java InfiniteSequence input.txt output.txt\n";
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
	 * Определение первого вхождения заданной последовательности A в бесконечной последовательности S
	 * основано на поиске наименьшего числа N последовательности S, начиная с которого, последовательность S
	 * включает в себя последовательность A
	 * 
	 * @param A
	 * @return
	 */
	private static String run(String A) {
		String N = null;
		
		int inNumber = 0;	// Начало последовательности A содержится в числе N с указанным смещением 
		boolean isZero;		// Последовательность A начинается с нуля
		
		/*
		 * Если последовательность A начинается с 0...,
		 * тогда будем искать 10..., а полученную позицию увеличим на единицу
		 */
		if (isZero = A.charAt(0) == '0') {
			A = "1" + A;
		}
		
		int length = A.length();
		
		/*
		 * Если последовательность A состоит из X цифр, тогда наименьшее число N может быть иметь от 1-го до X разрядов.
		 * Например:
		 * A = 4213, X = 4, тогда возможные значения числа N будут:
		 * 4
		 * 42, Y4
		 * 421, Y42, YY4
		 * 4213, Y421, YY42, YYY4
		 * Зная исходную последовательность A и закон формирования последовательности S, можно найти неизвестные цифры Y
		 * и выбрать наименьшее из полученных чисел
		 */
		for (int i = 1; i <= length; i++) {
			for (int j = 1; j <= i; j++) {
				
				int start = i - (j - 1);

				// Число последовательности S не может начинатья с нуля
				if (start < length && A.charAt(start) == '0') {
					continue;
				}
				
				// Последовательность A совпадает с числом последовательности S
				if (start == length) {
					if (N == null || isLess(A, N)) {
						N = A;
						inNumber = 1;
					}
					continue;
				}
				
				String number = A.substring(0, start);
				
				int x = j - 1;	// Кол-во неизвестных цифр Y в числе N
				
				if (x > 0) {
					// Пропустить неподходящие варианты если все известные цифры числа N равны девятке
					if (isNine(number))  {
						char c = A.charAt(start + x - 1);
						
						if (x == 1 && c == '1') {
							continue;
						}
						
						if (x > 1 && c == '0') {
							continue;
						}
					}
					
					number = A.substring(start, start + x) + number;
				}
				
				String curNumber = number;
				
				/*
				 * Определить последующие числа последовательности S
				 * и их совпадения с последовательностью A
				 */
				while (start < length) {
					curNumber = Math.getSum(curNumber, "1");
					
					if ((length - start) < curNumber.length()) {
						if (curNumber.startsWith(A.substring(start))) {
							if (N == null || isLess(number, N)) {
								N = number;
								inNumber = x + 1;
							}
						}
						break;
					}
					
					if (A.startsWith(curNumber, start)) {
						start += curNumber.length();
						
						if (start == length) {
							if (N == null || isLess(number, N)) {
								N = number;
								inNumber = x + 1;
							}
						}
					}
					else {
						break;
					}
				}
			}
		}

		// Определить позицию на основе полученного наименьшего числа N последовательности S
		int digits = N.length();
		
		int firstD = Integer.parseInt(N.substring(0, 1));
		N = N.substring(1);
		
		if (--firstD > 0) {
			N = firstD + N;
		}
		String position = Math.getProduct(String.valueOf(digits), N);
		
		for (int i = 1; i < digits; i++) {
			String p = "9";
			
			for (int j = 1; j < i; j++) {
				p += "0";
			}
			
			p = Math.getProduct(String.valueOf(i), p);
			
			position = Math.getSum(position, p);
		}
		
		// Добавить смещение
		position = Math.getSum(position, String.valueOf(inNumber));
		
		if (isZero) {
			position = Math.getSum(position, "1");
		}
		
		return position;
	}
	
	private static boolean isLess(String string1, String string2) {
		if (string1.length() < string2.length()) {
			return true;
		}
		
		if (string1.length() > string2.length()) {
			return false;
		}
		
		return string1.compareTo(string2) < 0;
	}
	
	private static boolean isNine(String string) {
		if (string == null) {
			return false;
		}

		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != '9') {
				return false;
			}
		}
		
		return true;
	}
	
}
