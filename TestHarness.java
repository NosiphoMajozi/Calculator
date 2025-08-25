import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TestHarness {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        // List of test cases: {gridSize, density, seed}
        int[][] testCases = { // increasing densities and grid sizes, random seeds
            {10, 1, 42},  
            {50, 5, 123}, 
            {100, 10, 30},   
            {100, 15, 42},  
            {150, 20, 789}   
        };

        System.out.println("Starting validation test suite...\n");
        boolean allTestsPassed = true;

        for (int i = 0; i < testCases.length; i++) {
            int gridSize = testCases[i][0];
            double density = testCases[i][1] / 10.0; // Convert to decimal
            int seed = testCases[i][2];
            System.out.printf("Running Test Case %d: gridSize=%d, density=%.1f, seed=%d%n",
                              i+1, gridSize, density, seed);

            // Run Serial Version
            Result serialResult = runProgram("DungeonHunter.java", gridSize, density, seed);
            // Run Parallel Version
            Result parallelResult = runProgram("DungeonHunterParallel.java", gridSize, density, seed);

            // Compare the results
            boolean testPassed = compareResults(serialResult, parallelResult, i+1);

            if (!testPassed) {
                allTestsPassed = false;
            }
            System.out.println("----------------------------------------");
        }

        if (allTestsPassed) {
            System.out.println("\n✅ ALL TESTS PASSED! The parallel implementation appears correct.");
        } else {
            System.out.println("\n❌ SOME TESTS FAILED. The outputs do not match.");
        }
    }

    private static Result runProgram(String programName, int gridSize, double density, int seed) throws IOException, InterruptedException {
        // Build the command to execute the Java program
        // Assumes the classes are in the same directory and compiled
        ProcessBuilder pb = new ProcessBuilder("java", programName, 
                                              String.valueOf(gridSize), 
                                              String.valueOf(density), 
                                              String.valueOf(seed));
        pb.directory(new File(".")); // Run in current directory
        pb.redirectErrorStream(true); // Merge stdout and stderr

        Process process = pb.start();
        String output = captureOutput(process.getInputStream());
        int exitCode = process.waitFor(); // Wait for the process to finish

        if (exitCode != 0) {
            System.err.println("Warning: " + programName + " exited with code " + exitCode);
            System.err.println("Output:\n" + output);
        }

        return new Result(output, exitCode);
    }

    private static String captureOutput(InputStream inputStream) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        }
        return buffer.toString();
    }

    private static boolean compareResults(Result serial, Result parallel, int testNumber) {
        // Check if both programs ran successfully
        if (serial.exitCode != 0 || parallel.exitCode != 0) {
            System.out.printf("❌ Test %d FAILED - One of the programs failed to run.%n", testNumber);
            return false;
        }

        // Extract the crucial lines of output using a simple parser
        String[] serialLines = serial.output.split("\n");
        String[] parallelLines = parallel.output.split("\n");

        String serialManaLine = findLineContaining(serialLines, "Dungeon Master (mana");
        String parallelManaLine = findLineContaining(parallelLines, "Dungeon Master (mana");

        String serialCoordsLine = findLineContaining(serialLines, "x=");
        String parallelCoordsLine = findLineContaining(parallelLines, "x=");

        // Compare the mana value and coordinates
        if (serialManaLine == null || parallelManaLine == null ||
            !serialManaLine.equals(parallelManaLine)) {
            System.out.printf("❌ Test %d FAILED - Mana value or coordinates do not match.%n", testNumber);
            System.out.println("  Serial:   " + (serialManaLine != null ? serialManaLine : "Not Found"));
            System.out.println("  Parallel: " + (parallelManaLine != null ? parallelManaLine : "Not Found"));
            return false;
        }

        if (serialCoordsLine == null || parallelCoordsLine == null ||
            !serialCoordsLine.equals(parallelCoordsLine)) {
            System.out.printf("❌ Test %d FAILED - Coordinates do not match.%n", testNumber);
            System.out.println("  Serial:   " + (serialCoordsLine != null ? serialCoordsLine : "Not Found"));
            System.out.println("  Parallel: " + (parallelCoordsLine != null ? parallelCoordsLine : "Not Found"));
            return false;
        }

        System.out.printf("✅ Test %d PASSED.%n", testNumber);
        System.out.println("  Result: " + serialManaLine);
        System.out.println("  Coords: " + serialCoordsLine);
        return true;
    }

    private static String findLineContaining(String[] lines, String searchTerm) {
        for (String line : lines) {
            if (line.contains(searchTerm)) {
                return line.trim();
            }
        }
        return null;
    }

    // Helper class to store the result of a program execution
    static class Result {
        String output;
        int exitCode;

        Result(String output, int exitCode) {
            this.output = output;
            this.exitCode = exitCode;
        }
    }
}