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
 * <ol>
 * 	<li>Add junit-4.11.jar and hamcrest-core-1.3.jar as library to your project (if not already done)</li>
 *  <li>Copy the test files (xxx.in/yyy.out) into the project root</li>
 *  <li>Run the test (In Eclipse: Right click on this file -> Run As -> JUnit Test)</li>
 * </ol>
 * 
 * Note that in order to add better visual support when a test case fails, I had to make a few assumptions 
 * about the structure of the input/output set. If these assumptions don't hold, the result may be unexpected.
 * <ul>
 * 	<li>The first line of any input set ALWAYS indicates the total number of tests to process</li>
 * 	<li>The total number of input lines is linear with the total number of tests</li>
 * 	<li>The total number of output lines is linear with the total number of tests</li>
 * </ul>
 *  
 *  @author Sandro Felicioni
 */
@RunWith(Parameterized.class)
public class Judge {
	
	private static final String IN_EXT = ".in";
	private static final String OUT_EXT = ".out";
	
	private String inputFile;
	private String outputFile;
	private int estimatedOutLinesPerTest;
	private int estimatedInLinesPerTest;

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
		validateEstimatedValues();

		while(expectedOutputScanner.hasNextLine()){
			
			for(int i = 0; i < getEstimatedInLinesPerTest(); i++){
				if(inputScanner.hasNext()) 
					System.out.println(inputScanner.nextLine());
			}

			String expected = expectedOutputScanner.nextLine();
			String actual = outputScanner.hasNextLine() ? outputScanner.nextLine() : null;
			
			for(int i = 1; i < getEstimatedOutLinesPerTest(); i++){
				expected += " \\n " + (expectedOutputScanner.hasNextLine() ? expectedOutputScanner.nextLine() : null);
				actual += " \\n " + (outputScanner.hasNextLine() ? outputScanner.nextLine() : null);
			}

			if(!equals(expected, actual))
				System.err.format("You failed: expected: <%s>, but was: <%s>%n%n", expected, actual);
			else if(getEstimatedInLinesPerTest() <= 0) // in case the input is omitted, we print the output
				System.out.println(actual);
			
			
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
	 * Estimates k = the number of output lines per test. (1:1 or 2:1 etc.). Why is this required? 
	 * It may occur that for one single test, two or more output lines have to be written. 
	 * Therefore in order to show assertion errors at the correct position, it may be necessary to 
	 * compare multiple output lines in a single assertion.
	 * 
	 * @return -1 if the #outputLinesPerTest could NOT be estimated, and k otherwise, but never 0.
	 * @throws IOException 
	 */
	public int getEstimatedOutLinesPerTest() throws IOException{
		if(estimatedOutLinesPerTest != 0) 
			return estimatedOutLinesPerTest;
		
		Scanner scanner = new Scanner(new File(inputFile));
		int numberOfTests = scanner.nextInt(); // we assume the first line ALWAYS indicates the number of tests
		scanner.close();
		
		LineNumberReader outReader = new LineNumberReader(new FileReader(new File(outputFile)));
		while(outReader.skip(Long.MAX_VALUE) > 0);
		outReader.close();
		
		// strategy: if (#totalOutputLines = k * #numberOfTests) => k output lines per test
		int totalInputLines = outReader.getLineNumber();
		if(totalInputLines % numberOfTests == 0){
			return estimatedOutLinesPerTest = totalInputLines / numberOfTests;
		}
		
		// we give up...
		return -1;
	}
	
	
	/**
	 * Estimates k = the number of input lines per test.
	 * 
	 * @return -1 if the #inputLinesPerTest could NOT be estimated, and k otherwise, but never 0.
	 * @throws IOException
	 */
	public int getEstimatedInLinesPerTest() throws IOException{
		if(estimatedInLinesPerTest != 0)
			return estimatedInLinesPerTest;
		
		LineNumberReader inReader = new LineNumberReader(new FileReader(new File(inputFile)));
		int numberOfTests = Integer.valueOf(inReader.readLine()); // we assume the first line ALWAYS indicates the number of tests
		while(inReader.skip(Long.MAX_VALUE) > 0);
		inReader.close();
		int totalInputLines = inReader.getLineNumber() -1; // -1, first line = #numberOfTests
		
		// strategy: if (#totalInputLines = k * #numberOfTests) => k input lines per test 
		if(totalInputLines % numberOfTests == 0){
			return estimatedInLinesPerTest = totalInputLines / numberOfTests;
		}
		
		// we give up...
		return estimatedInLinesPerTest = -1;
	}
	
	private void validateEstimatedValues() throws IOException{
		if(getEstimatedInLinesPerTest() <= 0){
			System.err.println("We failed to estimate the number of input lines per test and therefore are unalbe to stop at the right position.");
			System.err.println("Therefore we will omit the input and instead print and compare the output!");
		}
		if(getEstimatedOutLinesPerTest() <= 0){
			System.err.println("We failed to estimate the number of output lines per test and therefore are unalbe to compare multiple lines at once.");
			System.err.println("Therefore we will compare each line of the ouput separately!");
		}		
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
