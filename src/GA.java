import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class GA
{
    //Global vars
    static int generationNum = 0;
    static double averageFitness;
    static int minFitness;
    static int maxFitness;
    static int stairCount;

    static final double CHANCE_TO_FINISH_ON_END = 0.35;
    static final double NUMBER_OF_GENERATIONS = 20000;
    static final double CHANCE_TO_AVOID_PREVIOUS = 98; //line 180-ish
    static final double MAX_ALLOWABLE_LENGTH = 110;
    static final double MIN_ALLOWABLE_LENGTH = 72;

    public static void main(String[] args) {
        // Extract parameters from command line args
        int n = Integer.parseInt(args[0]); //pop size
        int selectionMethod = Integer.parseInt(args[1]); //1-elitest, 2-tournament
        double probabilityOfCrossover = Double.parseDouble(args[2]);
        double probabilityOfMutation = Double.parseDouble(args[3]);

        System.out.println("Creating a new simulation...\nPop Size (n) = " + n + "\nSelection Method = " + selectionMethod);
        System.out.println("Pc = " + probabilityOfCrossover + "\nPm = " + probabilityOfMutation + "\n");

        //Create the map
        System.out.println("Initialising the map...");
        File mapFile = new File("src/map.txt");
        MapTile[][] map = initialiseMap(mapFile);
        System.out.println("Map initialised.\n");

        // Create a population of n members
        // The chromosome we are using is the PATH GENERATED
        System.out.println("Creating the population...");
        ArrayList<PathChromosome> oldPop = createPop(n, map);
        System.out.println("Population created.\n");

        // Initialise a new population
        ArrayList<PathChromosome> newPop = new ArrayList<>(n);
        System.out.println("Beginning simulation...");

        // Perform genetic operations in a loop until an optimal is found
        while (true) {
            System.out.println("Generation " + generationNum + " starting...");
            // Create a new generation
            for (int i = 0; i < n / 2; i++) {
                PathChromosome parent1 = null;
                PathChromosome parent2 = null;
                PathChromosome child1 = null;
                PathChromosome child2 = null;

                // Select the children
                if (selectionMethod == 1) {
                    // Elitist selection
                    parent1 = elitistSelection(oldPop);
                    ArrayList<PathChromosome> tempPop = new ArrayList<PathChromosome>(oldPop.size() - 1);
                    tempPop.addAll(oldPop);
                    tempPop.remove(parent1);
                    parent2 = elitistSelection(tempPop);
                } else if (selectionMethod == 2) {
                    // Tournament selection
                    parent1 = tournamentSelection(oldPop);
                    parent2 = tournamentSelection(oldPop);
                } else if (selectionMethod == 3) {
                    do {
                        // Roulette selection
                        parent1 = rouletteWheelSelection(oldPop);
                        parent2 = rouletteWheelSelection(oldPop);
                    } while(parent1 == null || parent2 == null);
                } else {
                    System.out.println("Invalid selection method. Closing the program.");
                    System.exit(0);
                }

//                System.out.println("Parent 1: " + parent1.getFitness() + " " + parent1.getPathList().size());
//                System.out.println("Parent 2: " + parent2.getFitness() + " " + parent2.getPathList().size());

                // End the program if the parents are null
                if (parent1 == null || parent2 == null) {
                    System.out.println("Parents are null. Closing the program.");
                    System.exit(0);
                }
                if (parent1.getPathList().size() == 0 || parent2.getPathList().size() == 0) {
                    System.out.println("Parents are empty. Closing the program.");
                    System.exit(0);
                }

                //Clone the parents
//                child1 = parent1;
//                child2 = parent2;

                // Perform crossover
                if (Math.random() <= probabilityOfCrossover && probabilityOfCrossover != 0) {
                    child1 = crossover(parent1, parent2);
                    child2 = crossover(parent2, parent1);
                } else {
                    child1 = new PathChromosome(parent1.getPathList(), parent1.getStairCount());
                    child2 = new PathChromosome(parent2.getPathList(), parent2.getStairCount());
                } //end crossover

                // End the program if the children are null
                if (child1 == null || child2 == null) {
                    System.out.println("Children are null. Closing the program.");
                    System.exit(0);
                }

                // Perform mutation
                if (Math.random() < probabilityOfMutation) {
                    mutation(child1);
                    mutation(child2);
                } //end mutation

                // Calc fitness
                child1.calcFitness();
                child2.calcFitness();

                // Add the children to the new population
                newPop.add(child1);
                newPop.add(child2);
            } //end modifying the population

            System.out.println("Generation " + generationNum + " completing...");

            // Update the old population
            generationNum++;
            oldPop.clear();
            oldPop.addAll(newPop);
            newPop.clear();

            if (generationNum > NUMBER_OF_GENERATIONS) {
                System.out.println("No solution found after 1000 generations.");
                break;
            }
            if (getMaxFitness(oldPop) >= -22) {
                System.out.println("Solution found after " + generationNum + " generations.");
                break;
            }
        }

        // Print the best solution
        System.out.println("Best fitness: " + getBestSolution(oldPop).getFitness());
        System.out.println("Best solution:");
        printMap(map, generationNum);
        //System.out.println("Path: " + getBestSolution(oldPop).getPathList());
        printWroute(map, getBestSolution(oldPop));
        System.out.println("Best fitness: " + getBestSolution(oldPop).getFitness());
    } //end main

    // Creates an initial population of chromosomes
    public static ArrayList<PathChromosome> createPop(int n, MapTile[][] map) {
        int count = 0;
        ArrayList<PathChromosome> population = new ArrayList<>(n);
        MapTile start = new MapTile(5, 6, "G", null);
        start.setRetraceCount(1);
        MapTile curr;
        ArrayList<MapTile> path1 = new ArrayList<>();
        ArrayList<MapTile> path2 = new ArrayList<>();

        // Create 'n' chromosomes
        for (int i = 0; i < n; i++) {
            // Reset the path from the previous chromosome
            //int retraces = 1;
            curr = start;
            path1.add(curr);
            path2.clear();

            // Create a path by moving randomly
            while (true) {
                //Get the neighbors of the current tile
                ArrayList<MapTile> neighbors = fetchNeighbors(curr, map);

                // Print neighbors debugging
//                System.out.print("Gen [" + i + ", curr: " + curr.getX() + ", " + curr.getY() + "] ");
//                for (int j = 0; j < neighbors.size(); j++) {
//                    System.out.print("Neighbor " + j + " [" + neighbors.get(j).getX() + ", " + neighbors.get(j).getY() + "],\t");
//                }
//                System.out.println();

                // Select a random neighbor but a lower chance of selecting the previous tile
                MapTile nextTile = null;
                int randNum = (int) (Math.random() * 100);
                int randNeighbor = -1;

                if (randNum < CHANCE_TO_AVOID_PREVIOUS ) { // 90% chance of avoiding the previous tile
                    neighbors.remove(curr.getParent());
                    randNeighbor = (int) (Math.random() * (neighbors.size()));
                    nextTile = neighbors.get(randNeighbor);
                } else {
                    randNeighbor = (int) (Math.random() * (neighbors.size()));
                    nextTile = neighbors.get(randNeighbor);
                }
                nextTile.setParent(curr);
                nextTile.setRetraceCount(1);
                //loop over path and check for retrace
//                for (int j = 0; j < path1.size(); j++) {
//                    if (nextTile.getX() == path1.get(j).getX() && nextTile.getY() == path1.get(j).getY()) {
//                        retraces++;
//                    }
//                }
//                nextTile.setRetraceCount(retraces);

                //Add to path
                path1.add(nextTile);
                //System.out.println("Added " + count + " [" + nextTile.getX() + ", " + nextTile.getY() + "] to path.");

                //Check if we have reached the end
                if (path1.size() - 1 >= MAX_ALLOWABLE_LENGTH) {
                    System.out.println("Stopping with path size: " + path1.size());
                    break;
                }
                if (nextTile.getType().equals("G") && Math.random() < CHANCE_TO_FINISH_ON_END) { //80% chance of ending at goal
                    break;
                }

                curr = nextTile;
                neighbors.clear();
                count++;

            }

            // Make sure the path ends at the goal and isnt shorter than the minimum length
            if (!path1.get(path1.size() - 1).getType().equals("G")) {
                i--;
                path1.clear();
                continue;
            } else if (path1.size() < MIN_ALLOWABLE_LENGTH) {
                i--;
                path1.clear();
                continue;
            }

            // Adds the path to a chromosome object
            path2.addAll(path1);
            PathChromosome newChromosome = new PathChromosome();
            newChromosome.setPathList(path2);

            // Get the fitness of it
            newChromosome.calcFitness();
            System.out.println("Chromosome '" + i + "' path size and fitness: " + newChromosome.getPathList().size() + ", " + newChromosome.getFitness());

            // Add chromosome object to list
            population.add(newChromosome);

            // Clear the path list
            path1.clear();
            count = 0;
        }

        return population;
    }

    // TODO: perform mutation
    private static void mutation(PathChromosome child1) {
        // Remove a random part of the path
        int rand = (int) (2 + Math.random() * (child1.getPathList().size() - 2));

        // Make a new path
        ArrayList<MapTile> newPath = new ArrayList<>(rand);

        for (int i = 0; i < rand; i++) {
            newPath.add(child1.getPathList().get(i));
        }

        child1.setPathList(newPath);
    }

    // Perform crossover
    private static PathChromosome crossover(PathChromosome parent1, PathChromosome parent2) {
        if (parent1.getPathList().size() == 0 || parent2.getPathList().size() == 0) {
            System.out.println("ERROR: Cannot perform crossover on empty path.");
            return null;
        }

        // Get a random point in the path
        int rand = (int) (Math.random() * parent1.getPathList().size());
        while (rand < 1) {
            rand = (int) (Math.random() * parent1.getPathList().size());
        }

        // Make a new path
        ArrayList<MapTile> newPath = new ArrayList<>(parent1.getPathList().size());

        for (int i = 0; i < rand; i++) {
            newPath.add(parent1.getPathList().get(i));
        }

        for (int i = rand; i < parent2.getPathList().size(); i++) {
            newPath.add(parent2.getPathList().get(i));
        }

        return new PathChromosome(newPath);
    }

    // 1. Perform elitist selection
    private static PathChromosome elitistSelection(ArrayList<PathChromosome> oldPop) {
        // Find the fittest chromosome in the population
        PathChromosome fittest = oldPop.get(0);
        for (PathChromosome curr : oldPop) {
            if (curr.getFitness() > fittest.getFitness()) {
                fittest = curr;
            }
        }
        return fittest;
    }

    // 2. Perform tournament selection
    public static PathChromosome tournamentSelection(ArrayList<PathChromosome> pop) {
        //Select two random members of the pop
        PathChromosome a = pop.get((int) (Math.random() * pop.size()-1));
        PathChromosome b = pop.get((int) (Math.random() * pop.size()-1));

//        System.out.println("a length: " + a.getPathList().size() + "\tb length: " + b.getPathList().size());
//        System.out.println("a fitness: " + a.getFitness() + "\tb fitness: " + b.getFitness());
//        System.out.println("a path: " + a.getPathList() + "\tb path: " + b.getPathList());

        if (a.getFitness() >= b.getFitness()) {
            return a;
        } else if (a.getFitness() < b.getFitness()) {
            return b;
        } else {
            return null;
        }
    }

    // 3. Selection method: weighted roulette wheel
    public static PathChromosome rouletteWheelSelection(ArrayList<PathChromosome> pop) {
        int totalFitness = 0;
        for (PathChromosome c : pop) {
            totalFitness += c.getFitness();
        }
        int rand = (int) (Math.random() * totalFitness);
        int fin = 0;
        for (PathChromosome c : pop) {
            fin += c.getFitness();
            if (fin > rand) return c;
        }
        return null;
    }

    // Get all the neighbors of the current node
    public static ArrayList<MapTile> fetchNeighbors(MapTile curr, MapTile[][] map) {
        //vars
        ArrayList<MapTile> neighbors = new ArrayList<>();
        int col = curr.getX();
        int row = curr.getY();

        //check up neighbor
        if (col > 0) {
            if (!map[col - 1][row].getType().equals("W")) {
                neighbors.add(map[col - 1][row]);
            }
        }

        //check down neighbor
        if (col < map.length - 1) {
            if (!map[col + 1][row].getType().equals("W")) {
                neighbors.add(map[col + 1][row]);
            }
        }

        //check right neighbor
        if (row < map[0].length - 1) {
            if (!map[col][row + 1].getType().equals("W")) {
                neighbors.add(map[col][row + 1]);
            }
        }

        //check left neighbor
        if (row > 0) {
            if (!map[col][row - 1].getType().equals("W")) {
                neighbors.add(map[col][row - 1]);
            }
        }

        neighbors.remove(curr);

        return neighbors;
    }

    // Creates the map
    public static MapTile[][] initialiseMap(File f) {
        //Initialize variables
        MapTile[][] map = new MapTile[0][0];
        String indicatorChar;
        int xcoord, ycoord;
        int row, col;

        try {
            String info;
            Scanner myReader = new Scanner(f);
            while (myReader.hasNextLine()) {
                // Fetch data from file
                info = myReader.nextLine();
                if (info.equals("E")) break;
                indicatorChar = info.substring(0, info.indexOf(" "));
                info = info.substring(info.indexOf(" ") + 1);
                //System.out.println("info: " + info); //debug

                //Decide what to do
                switch (indicatorChar) {
                    case "M" -> { //map size
                        row = Integer.parseInt(info.substring(0, info.lastIndexOf(" ")));
                        col = Integer.parseInt(info.substring(info.indexOf(" ") + 1, info.length()));
                        //System.out.println("row col: " + row + " " + col); //debug
                        map = new MapTile[row][col];
                    }
                    case "G" -> { //goal
                        xcoord = Integer.parseInt(info.substring(0, info.lastIndexOf(" ")));
                        ycoord = Integer.parseInt(info.substring(info.indexOf(" ") + 1, info.length()));
                        map[xcoord][ycoord] = new MapTile(xcoord, ycoord, "G");
                    }
                    case "S" -> { //stair
                        xcoord = Integer.parseInt(info.substring(0, info.lastIndexOf(" ")));
                        ycoord = Integer.parseInt(info.substring(info.indexOf(" ") + 1, info.length()));
                        map[xcoord][ycoord] = new MapTile(xcoord, ycoord, "S");
                    }
                    case "W" -> { //wall
                        xcoord = Integer.parseInt(info.substring(0, info.lastIndexOf(" ")));
                        ycoord = Integer.parseInt(info.substring(info.indexOf(" ") + 1, info.length()));
                        map[xcoord][ycoord] = new MapTile(xcoord, ycoord, "W");
                    }
                    case "O" -> { //not required tile
                        xcoord = Integer.parseInt(info.substring(0, info.lastIndexOf(" ")));
                        ycoord = Integer.parseInt(info.substring(info.indexOf(" ") + 1, info.length()));
                        map[xcoord][ycoord] = new MapTile(xcoord, ycoord, "O");
                    }
                }
                //System.out.println("Added '" + indicatorChar + "' at [" + info + "]"); //debug
            } //end of loop
            //Close readers
            myReader.close();
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Fill the empty void with node type empty
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                //char type = map[row][col].DisplayType.
                if (map[i][j] == null) {
                    map[i][j] = new MapTile(i, j, "-");
                }
            }
        }

        return map;
    }

    // Get the best solution
    private static PathChromosome getBestSolution(ArrayList<PathChromosome> oldPop) {
        PathChromosome bestSolution = oldPop.get(0);
        for (PathChromosome path : oldPop) {
            if (path.getFitness() < bestSolution.getFitness()) {
                bestSolution = path;
            }
        }
        return bestSolution;
    }

    // Gets the max fitness of the population
    public static double getMaxFitness(ArrayList<PathChromosome> pop) {
        double max = 0;
        for (PathChromosome curr : pop) {
            if (curr.getFitness() > max) {
                max = curr.getFitness();
            }
        }
        return max;
    }

    // Prints the map
    public static void printMap(MapTile[][] map, int timestep) {
        System.out.println("Timestep: " + timestep);
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                MapTile n = map[row][col];
                System.out.print(map[row][col].getType() + " ");
            }
            System.out.println();
        }
        timestep++;
    }

    // Print the path of the best solution in the map step by step
    public static void printWroute(MapTile[][] map, PathChromosome bestSolution) {
        ArrayList<MapTile> path = bestSolution.getPathList();

        // Create a copy of the map
        MapTile[][] mapCopy = new MapTile[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                mapCopy[i][j] = new MapTile(map[i][j].getX(), map[i][j].getY(), map[i][j].getType());
            }
        }

        // Insert path into map one step at a time and print
        for (int i = 0; i < path.size(); i++) {
            mapCopy[path.get(i).getX()][path.get(i).getY()].setType("*");
            printMap(mapCopy, i);
            System.out.println("Moved to: " + path.get(i).getX() + ", " + path.get(i).getY());
            System.out.println("Path length: " + (i + 1));
        }
    }
}