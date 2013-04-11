import java.util.Scanner;

/**
 * Example Main class which calculates a modified version of the fibonnaci numbers in O(n).
 */
class Main {
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
	    int t = scanner.nextInt();
	    for(int i = 0; i < t; ++i){
	    	int n = scanner.nextInt();
	    	int a = scanner.nextInt();
	    	int b = scanner.nextInt();
	    	int c = scanner.nextInt();
	    	int d = scanner.nextInt();

	    	System.out.println(fibonacci(n, a, b , c, d));
	    }
	    scanner.close();
	}

	private static long fibonacci(int n, int a, int b, int c, int d) {
		long[] prefix = new long[n+1];
		for(int i = 0; i <= n; i++){
			if(i == 0) prefix[i] = a;
			else if(i == 1) prefix[i] = b;
			else prefix[i] = c * prefix[i-1] + d * prefix[i-2];
		}
		return prefix[n];
	}
}