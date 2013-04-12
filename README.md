judge
=====

This project evolved from my college assignments at ETH. In the course "Datastructures and Algorithms" we had to implement various algorithms and with each assignment one or more test sets (xyz.in and xyz.out) were given in order to verify our results (manually). 

This project can be used to automate the verification with JUnit tests. The class Judge is a JUnit Test which scans the project root for any input files named *.in. For each input file it will dynamically add a separate JUnit test and run the Method Main.main(String[] args).The main method can read from System.in and write to System.out. After the execution of the main method completed, the results written to System.out are compared with those within *.out

In order to enhance feedback, the judge makes the following assumptions about the structure of the input/output data:
* The first line of any input set ALWAYS indicates the total number of tests to process
* The total number of input lines is linear with the total number of tests
* The total number of output lines is linear with the total number of tests
