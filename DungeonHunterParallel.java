/* Solo-levelling Hunt for Dungeon Master
 * Parallel version 
 * Michelle Kuttel 2025, University of Cape Town
 * Parallel implementation using join() synchronization only
 */
/**
 * DungeonHunterParallel.java
 *
 * Main driver for the parallel Dungeon Hunter assignment.
 * This program initializes the dungeon map and performs parallel searches
 * to locate the global maximum using multiple threads with join() synchronization.
 *
 * Usage:
 *   java DungeonHunterParallel <gridSize> <numSearches> <randomSeed>
 *
 */

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

class DungeonHunterParallel {
    static final boolean DEBUG = false;

    // timers for how long it all takes
    static long startTime = 0;
    static long endTime = 0;
    
    private static void tick() { startTime = System.currentTimeMillis(); }
    private static void tock() { endTime = System.currentTimeMillis(); }

    public static void main(String[] args) {
        double xmin, xmax, ymin, ymax; // dungeon limits - dungeons are square
        DungeonMapParallel dungeon; // object to store the dungeon as a grid

        int numSearches = 10, gateSize = 10;
        HuntParallel[] searches; // Array of searches

        Random rand = new Random(); // the random number generator
        int randomSeed = 0; // set seed to have predictability for testing

        if (args.length != 3) {
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }

        /* Read argument values */
        try {
            gateSize = Integer.parseInt(args[0]);
            if (gateSize <= 0) {
                throw new IllegalArgumentException("Grid size must be greater than 0.");
            }
            
         numSearches = (int) (Double.parseDouble(args[1]) * (gateSize * 2) * (gateSize * 2) * DungeonMapParallel.RESOLUTION);
          //  NEW VALIDATION CHECK  //
         if (numSearches < 1) {
         throw new IllegalArgumentException("Number of searches calculated to " + numSearches + ". The density value must be high enough to generate at least 1 search.");
            }

            randomSeed = Integer.parseInt(args[2]);
            if (randomSeed < 0) {
                throw new IllegalArgumentException("Random seed must be non-negative.");
            } else if (randomSeed > 0) {
                rand = new Random(randomSeed); // BUG FIX
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: All arguments must be numeric.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        xmin = -gateSize;
        xmax = gateSize;
        ymin = -gateSize;
        ymax = gateSize;
        dungeon = new DungeonMapParallel(xmin, xmax, ymin, ymax, randomSeed); // Initialize dungeon

        int dungeonRows = dungeon.getRows();
        int dungeonColumns = dungeon.getColumns();
        searches = new HuntParallel[numSearches];

        for (int i = 0; i < numSearches; i++) { // initialize searches at random locations in dungeon
            searches[i] = new HuntParallel(i + 1, rand.nextInt(dungeonRows), rand.nextInt(dungeonColumns), dungeon);
        }

        // Variables to store final results
        int[] globalMax = new int[]{Integer.MIN_VALUE};
        int[] globalFinder = new int[]{-1};

        tick(); // Start timer for parallel work

        // Determine number of threads (use available processors)
        int numThreads = Runtime.getRuntime().availableProcessors();
        int searchesPerThread = numSearches / numThreads;
        int remainder = numSearches % numThreads;

        // List to hold all threads
        List<Thread> threads = new ArrayList<>();

        // Create and start threads
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            final int startIndex = i * searchesPerThread;
            final int endIndex = (i == numThreads - 1) ? startIndex + searchesPerThread + remainder : startIndex + searchesPerThread;

            Thread thread = new Thread(() -> {
                int localMax = Integer.MIN_VALUE;
                int localFinder = -1;

                // Process assigned searches
                for (int j = startIndex; j < endIndex; j++) {
                    int result = searches[j].findManaPeak();
                    
                    // Update local max for this thread
                    if (result > localMax) {
                        localMax = result;
                        localFinder = j;
                    }
                    
                    if (DEBUG) {
                        System.out.println("Shadow " + searches[j].getID() + " finished at " + result + " in " + searches[j].getSteps());
                    }
                }

                // Update global results (race condition is benign)
                if (localMax > globalMax[0]) {
                    globalMax[0] = localMax;
                    globalFinder[0] = localFinder;
                }
            });

            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to complete using join()
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        tock(); // End timer for parallel work

        int max = globalMax[0];
        int finder = globalFinder[0];

        System.out.printf("\t dungeon size: %d,\n", gateSize);
        System.out.printf("\t rows: %d, columns: %d\n", dungeonRows, dungeonColumns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Number searches: %d\n", numSearches);

        /* Total computation time */
        System.out.printf("\n\t time: %d ms\n", endTime - startTime);
        int tmp = dungeon.getGridPointsEvaluated();
        System.out.printf("\tnumber dungeon grid points evaluated: %d  (%2.0f%s)\n", tmp, (tmp * 1.0 / (dungeonRows * dungeonColumns * 1.0)) * 100.0, "%");

        /* Results */
        System.out.printf("Dungeon Master (mana %d) found at:  ", max);
        System.out.printf("x=%.1f y=%.1f\n\n", dungeon.getXcoord(searches[finder].getPosRow()), dungeon.getYcoord(searches[finder].getPosCol()));
        dungeon.visualisePowerMap("visualiseSearch.png", false);
        dungeon.visualisePowerMap("visualiseSearchPath.png", true);
    }
}
// done