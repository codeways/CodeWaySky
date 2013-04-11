import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Simulates the judge.
 * How to use this class:
 * 	1. Add junit-4.11.jar and hamcrest-core-1.3.jar as library to your project (if not already done)
 *  2. Copy the test files (xxx.in/yyy.out) into the project root (or make them available in the classpath)
 *  3. Run the test (In Eclipse: Right click on this file -> Run As -> JUnit Test)
 *  
 *  @author Sandro Felicioni
 */
@RunWith(Parameterized.class)
public class Judge {
	
	private static final String IN_EXT = ".in";
	private static final String OUT_EXT = ".out";
	
	private String inputFile;
	private String outputFile;
	private int outInRatio = -1;

	public Judge(String filename){
		this.inputFile = filename + IN_EXT;
		this.outputFile = filename + OUT_EXT;
	}
	
	@Test 
	public void test() throws IOException {
		
		// redirect io streams
		PrintStream oldOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baos));
		System.setIn(new FileInputStream(inputFile));

		// run your code
		Main.main(new String[0]);
		
		// revert io streams
		System.setOut(oldOut);

		// validate with test sets
		System.out.println("############################################");
		System.out.println("Input:");
		
		Scanner inputScanner = new Scanner(new FileInputStream(inputFile));
		Scanner expectedOutputScanner = new Scanner(new FileInputStream(outputFile));
		Scanner outputScanner = new Scanner(new ByteArrayInputStream(baos.toByteArray()));
		
		System.out.println(inputScanner.nextLine());
		while(inputScanner.hasNextLine()){
			System.out.println(inputScanner.nextLine());

			String expected = expectedOutputScanner.hasNextLine() ? expectedOutputScanner.nextLine() : null;
			String actual = outputScanner.hasNextLine() ? outputScanner.nextLine() : null;
			
			for(int i = 1; i < getOutInRatio(); i++){
				expected += " \\n " + (expectedOutputScanner.hasNextLine() ? expectedOutputScanner.nextLine() : null);
				actual += " \\n " + (outputScanner.hasNextLine() ? outputScanner.nextLine() : null);
			}

			if(!equals(expected, actual))
				System.err.format("You failed: expected: <%s>, but was: <%s>%n%n", expected, actual);
			
			String wrappedExpected = expected.replaceAll(" \\\\n ", System.getProperty("line.separator"));
			String wrappedActual = actual.replaceAll(" \\\\n ", System.getProperty("line.separator"));
			Assert.assertEquals(wrappedExpected, wrappedActual);
		}
		
		inputScanner.close();
		expectedOutputScanner.close();
		outputScanner.close();
	}
	
	public static boolean equals(String first, String second){
		return first == second || first != null && first.equals(second);
	}
	
	
	/**
	 * Gets the ratio of output lines per input lines (1:1 or 2:1 etc.) Why is this required? 
	 * It may occur that for one single input line, two or more output lines have to be written. 
	 * Therefore in order to show assertion errors at the correct position, it may be necessary to 
	 * compare multiple output lines in a single assertion.
	 * 
	 * @return The number of output lines, which have to be compared for one input line.
	 * @throws IOException 
	 */
	public int getOutInRatio() throws IOException{
		if(outInRatio != -1) 
			return outInRatio;
		
		LineNumberReader  inReader = new LineNumberReader(new FileReader(new File(inputFile)));
		while(inReader.skip(Long.MAX_VALUE) > 0);
		inReader.close();
		
		LineNumberReader outReader = new LineNumberReader(new FileReader(new File(outputFile)));
		while(outReader.skip(Long.MAX_VALUE) > 0);
		outReader.close();
		
		int inputlines = inReader.getLineNumber() - 1; // -1, first line = number of test cases within file
		int outputLines = outReader.getLineNumber();
		
		return outputLines / inputlines;
	}
	
	/**
	 * Dynamically adds a JUnit test for each test file.
	 */
	@Parameters(name = "{0}")
	public static Collection<String[]> data(){
		
		List<String[]> files = new ArrayList<String[]>();
		
		System.out.println("Scan for test files...");
		File folder = new File(".");
		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(IN_EXT)) {
				String rawName = file.getName().substring(0, file.getName().length() - IN_EXT.length());
				files.add(new String[] {rawName});
				System.out.println(" - " + rawName); 
			}
		}
		
		if(files.isEmpty()){
			System.err.println("No testfiles were found. Make sure to place them into the project root directory!");
		}
		return files;
	}
}
