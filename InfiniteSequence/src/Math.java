/**
 * 
 */

/**
 * @author Sergey Savelyev
 *
 */
public class Math {
	
	/**
	 * Целочисленное сложение.
	 * Позволяет складывать целые числа, превышающие Long.MAX_VALUE
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public static String getSum(String number1, String number2) {
		String sum = "";
		
		int length1 = number1.length(),
			length2 = number2.length();
		
		String longN, shortN;
		
		if (length1 > length2) {
			longN = number1;
			shortN = number2;
		}
		else {
			longN = number2;
			shortN = number1;
		}
		
		// Calculate the sum
		boolean inMind = false;
		
		for (int i = longN.length() - 1; i >= 0; i--) {
			
			int s = Integer.parseInt(longN.substring(i, i + 1));
			
			int j = i - (longN.length() - shortN.length());
			
			if (j >= 0) {
				int sn = Integer.parseInt(shortN.substring(j, j + 1));
				
				s += sn;
			}
			
			if (inMind) {
				s++;
			}
			
			if (s <= 9) {
				inMind = false;
			}
			else {
				s -= 10;
				inMind = true;
			}
			
			sum = s + sum;
			
			if (j <= 0 && !inMind) {
				sum = longN.substring(0, i) + sum;
				
				return sum;
			}
		}
		
		if (inMind) {
			sum = "1" + sum;
		}
			
		return sum;
	}

	/**
	 * Целочисленное умножение.
	 * Позволяет умножать целые числа, превышающие Long.MAX_VALUE
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public static String getProduct(String number1, String number2) {
		String product = "";
		
		// Calculate the product
		int length1 = number1.length(),
			length2 = number2.length();
			
		for (int i = length1 - 1; i >= 0; i--) {
			for (int j = length2 - 1; j >= 0; j--) {
				
				int d1 = Integer.parseInt(number1.substring(i, i + 1)),
					d2 = Integer.parseInt(number2.substring(j, j + 1)),
					degree = (length1 - 1) - i + (length2 - 1) - j;

				int p = d1 * d2;
				
				String tempProduct = "" + p;
				
				for (int k = 0; k < degree; k++) {
					tempProduct += "0";
				}
				
				product = getSum(product, tempProduct);
			}
		}
		
		return product;
	}
	
}
