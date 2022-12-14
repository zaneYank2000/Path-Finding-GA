import java.util.ArrayList;
import java.util.stream.Collectors;

public class PathChromosome
{
    // Instance variables
    private double fitness;
    private ArrayList<MapTile> pathList;
    private int stairCount;

    final int REQUIRED_TILES = 54; //make number base on map

    public PathChromosome(ArrayList<MapTile> pathList, int stairCount){
        this.stairCount = stairCount;
        this.pathList = pathList;
    }

    public PathChromosome(int stairCount){
        this.stairCount = stairCount;
    }

    public PathChromosome(ArrayList<MapTile> pathList){
        this.pathList = pathList;
    }

    public PathChromosome() {

    }

    public double getFitness() {
        return fitness;
    }

    public void setStairCount(int stairCount) {
        this.stairCount = stairCount;
    }

    // Determine the number of stairs used in the path
    public void calcStairs() {
        int stairCount = 0;
        for (MapTile mapTile : pathList) {
            if (mapTile.getType().equals("S")) {
                stairCount++;
            }
        }
        this.stairCount = stairCount;
    }

    // Determine the retrace count of each tile in the map
    public void calcRetraceCount() {
        for (int i = 0; i < pathList.size(); i++) {
            MapTile curr = pathList.get(i);
            for (int j = 0; j < pathList.size(); j++) {
                MapTile next = pathList.get(j);
                if (curr.getX() == next.getX() && curr.getY() == next.getY()) {
                    curr.setRetraceCount(curr.getRetraceCount() + 1);
                }
            }
        }
    }

    public ArrayList<MapTile> getPathList() {
        return pathList;
    }

    public void setPathList(ArrayList<MapTile> pathList) {
        this.pathList = pathList;
    }

    // Determine the fitness of the path
    public void calcFitness() {
        calcStairs();
        //calcRetraceCount();
        //System.out.println("1. CALC FITNESS PATH SIZE: " + pathList.size());
        //Checks the number of stairs used and awards bonus/penalty
        if (stairCount >= 0 && stairCount <= 3) {
            fitness -= 100;
        } else if (stairCount >= 4 && stairCount <= 7) {
            fitness += 5;
        } else {
            fitness -= 3;
        }

        if (pathList.size() < REQUIRED_TILES) {
            fitness -= 100;
            System.out.println("PENALIZED: Too few tiles");
        } else if (pathList.size() > 110) {
            fitness -= 10;
        }

        //Removes 7 points for every required tile not used
        int used = 0;
        for (MapTile curr : pathList) {
            if (curr.getRetraceCount() > 0 && !(curr.getType().equals("S") || curr.getType().equals("O"))) {
                used++;
            }
        }
        fitness -= Math.abs(REQUIRED_TILES - used) * 7;
        System.out.println("PENALIZED: Unused tiles: " + Math.abs(REQUIRED_TILES - used) * 7);

        // Removes one point for every tile reused
        used = 0;
//        ArrayList<MapTile> usedTiles = new ArrayList<>();
//        ArrayList<MapTile> pathListCopy = new ArrayList<>(pathList);
//        pathListCopy.addAll(pathList);
//        for (int i = 0; i < pathListCopy.size(); i++) {
//            MapTile curr = pathListCopy.get(i);
//
//            for (int j = 1; j < pathListCopy.size(); j++) {
//                MapTile next = pathListCopy.get(j);
//
//                if (curr.getX() == next.getX() && curr.getY() == next.getY()) {
//
//                    usedTiles.add(curr);
//                    pathListCopy.remove(j);
//                }
//            }
//        }
        //usedTiles = pathListCopy.stream().distinct().collect(Collectors.toList());
//        fitness -= usedTiles.size();
//        System.out.println("PENALIZED: Reused tiles: " + usedTiles.size());

        if (!pathList.get(pathList.size() - 1).getType().equals("G")) {
            fitness -= 100;
            System.out.println("PENALIZED: Path does not end at goal");
        }

        //System.out.println("CALC FITNESS " + fitness);
    }

    public int getStairCount() {
        return stairCount;
    }
}
