package State;

import Tiles.BuildingTile;
import Tiles.RoadTile;
import Tiles.Tile;
import Vehicles.AmbulanceAgent;
import Vehicles.CarAgent;
import Vehicles.FiretruckAgent;

import javax.swing.*;
import java.util.*;

public class GlobalState {
    private Tile[][] grid;
    private static GlobalState instance;
    private GridVisualizer visualizer;
    private JFrame frame;

    private GlobalState() {
        grid = GridLoader.loadGridFromFile("grid_world.txt");
        initDisplay();
        System.out.println("[GlobalState] initialized grid and display");
    }

    public static synchronized GlobalState getInstance() {
        if (instance == null) {
            instance = new GlobalState();
        }
        return instance;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public List<BuildingTile> getBuildings() {
        List<BuildingTile> buildingTiles = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                // check if the tile is a building and if it's next to a road
                if (grid[i][j] instanceof BuildingTile && grid[i][j].getValue() == 1 &&
                        (grid[i - 1][j] instanceof RoadTile ||
                                grid[i + 1][j] instanceof RoadTile ||
                                grid[i][j - 1] instanceof RoadTile ||
                                grid[i][j + 1] instanceof RoadTile)) {
                    buildingTiles.add((BuildingTile) grid[i][j]);
                }
            }
        }
        return buildingTiles;
    }

    public void setCarLocation(int x, int y, CarAgent agent) {
        if (grid != null) {
            grid[y][x].setAgent(agent);
            System.out.println("[State.GlobalState] Grid state changed");
            updateGridDisplay();
        }
    }

    public void setOnFire(int x, int y) {
        if (grid != null) {
            ((BuildingTile) grid[y][x]).setOnFire(true);
            System.out.println("[State.GlobalState] Grid state changed");
            updateGridDisplay();
        }
    }

    public boolean isOnFire(int x, int y) {
        if (grid != null) {
            return ((BuildingTile) grid[y][x]).isOnFire();
        }
        return false;
    }

    public void moveCar(int oldX, int oldY, int x, int y, FiretruckAgent agent) {
        if (grid == null) {
            return;
        }

        if (agent == null) {
            return;
        }

        if (grid[y][x].getAgent() != null && grid[oldY][oldX].getAgent() != null && grid[oldY][oldX].getAgent() != agent) {
            updateGridDisplay();
            return;
        }

        if (grid[y][x].getAgent() != null) {
            grid[oldY][oldX].setAgent(null);
            updateGridDisplay();
            return;
        }
        if (grid[oldY][oldX].getAgent() != null && grid[oldY][oldX].getAgent() != agent) {
            grid[y][x].setAgent(agent);
            updateGridDisplay();
            return;
        }

        grid[oldY][oldX].setAgent(null);
        grid[y][x].setAgent(agent);
        updateGridDisplay();
    }

    public void moveCar(int oldX, int oldY, int x, int y, AmbulanceAgent agent) {
        if (grid == null) {
            return;
        }

        if (agent == null) {
            return;
        }

        if (grid[y][x].getAgent() != null && grid[y][x].getAgent().getCrashed() && grid[oldY][oldX].getAgent() != null && grid[oldY][oldX].getAgent() != agent && grid[oldY][oldX].getAgent().getCrashed()) {
            updateGridDisplay();
            return;
        }

        if (grid[y][x].getAgent() != null && grid[y][x].getAgent().getCrashed()) {
            grid[oldY][oldX].setAgent(null);
            updateGridDisplay();
            return;
        }
        if (grid[oldY][oldX].getAgent() != null && grid[oldY][oldX].getAgent() != agent && grid[oldY][oldX].getAgent().getCrashed()) {
            grid[y][x].setAgent(agent);
            updateGridDisplay();
            return;
        }

        grid[oldY][oldX].setAgent(null);
        grid[y][x].setAgent(agent);
        updateGridDisplay();
    }

    // States
    // 0: no grid
    // 1: moved normally
    // 2: crashed
    public int moveCar(int oldX, int oldY, int x, int y, CarAgent agent) {
        int status = 0;

        if (agent == null) {
            return 0;
        }

        if (grid == null) {
            return 0;
        }

        // check for crash
        CarAgent possibleCrashAgent = grid[y][x].getAgent();
        int possibleCrashId = grid[y][x].getCarId();
        if (possibleCrashId == 0 || possibleCrashAgent instanceof AmbulanceAgent || possibleCrashAgent instanceof FiretruckAgent) {
            grid[oldY][oldX].setAgent(null);
            grid[y][x].setAgent(agent);
            status = 1;
        } else {
            // TODO: (test) stop car we crashed into as well
            // get the agent of that location and set a boolean for it to stop
            Random rand = new Random();
            double prob = 0.05;
            double randVal = rand.nextDouble();

            if(randVal < prob){
                System.out.printf("[GlobalState] Crash detected: Car%d[%d,%d] - Car%d[%d,%d]\n", agent.getId(), oldY, oldX, possibleCrashId, y, x);
                possibleCrashAgent.setCrashed(true);
                status = 2;
            }else{
                status = 3;
            }
        }

        updateGridDisplay();
        return status;
    }

    public void initDisplay() {
        visualizer = new GridVisualizer(grid);
        frame = new JFrame("Grid World Visualizer");
        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        showGrid();
    }

    public void showGrid() {
        if (visualizer != null && grid != null) {
//            visualizer.printGrid(grid);
        }
    }

    public void updateGridDisplay() {
        if (visualizer != null && grid != null && frame != null) {
            visualizer.updateGrid(grid);
            showGrid();
        }
    }

    public void updatePathDisplay(List<Tile> path) {
        if (visualizer != null && grid != null && frame != null) {
            visualizer.setPath(path);
            showGrid();
        }
    }

    // FIXME: what if left, right or forward is out of bounds?
    // given an intersection, check if there are other cars in the radius of the intersection (relatively to the car)
    public boolean checkCarsInIntersection(int x, int y, int carId, int carX, int carY, int radius) {
        boolean checkLeft = false;
        boolean checkRight = false;
        boolean checkForward = false;

        // moving right
//        System.out.printf("[Car %d] moving (%d,%d) => (%d,%d)\n", carId, carX, carY, x, y);
        if (x > carX) {
//            System.out.printf("[Car %d] moving right\n", carId);
            // check tiles of interseciton + radius
            for (int i = 0; i < radius + 1; i++) {
                // check left (x stays the same)
                if(!(y-i < 0)){
                    checkLeft = (grid[y - i][x].getCarId() != 0);
                }

                // check right (x + 1)
                if(!(y+i >= grid.length || x+1 >= grid[y].length)){
                    checkRight = (grid[y + i][x + 1].getCarId() != 0);
                }

                // check forward (not if greedy)
                if(!(y - 1 < 0 || x + 1 + i >= grid[y].length)) {
                    checkForward = (radius >= 2 && grid[y - 1][x + 1 + i].getCarId() != 0);
                }

                if (checkLeft || checkRight || checkForward) {
                    return true;
                }
            }
            // moving left
        } else if (x < carX) {
//            System.out.printf("[Car %d] moving left\n", carId);
            // check tiles of interseciton + radius
            for (int i = 0; i < radius + 1; i++) {
                // check left (y stays the same)
                if(!(y+i >= grid.length)) {
                    checkLeft = (grid[y + i][x].getCarId() != 0);
                }

                // check right (y + 1)
                if(!(y-i < 0 || x - 1 < 0)) {
                    checkRight = (grid[y - i][x - 1].getCarId() != 0);
                }

                // check forward (not if greedy)
                if(!(y+i >= grid.length || x - 1 - i <  0)) {
                    checkForward = (radius >= 2 && grid[y + 1][x - 1 - i].getCarId() != 0);
                }

                if (checkLeft || checkRight || checkForward) {
                    return true;
                }
            }
            // moving up
        } else if (y < carY) {
//            System.out.printf("[Car %d] moving up\n", carId);
            // check tiles of interseciton + radius
            for (int i = 0; i < radius + 1; i++) {
                // check left (y stays the same)
                if(!(x-i < 0)) {
                    checkLeft = (grid[y][x - i].getCarId() != 0);
                }

                // check right (y + 1)
                if(!(y-1 < 0 || x+1 >= grid[y].length)) {
                    checkRight = (grid[y - 1][x + i].getCarId() != 0);
                }

                // check forward (not if greedy)
                if(!(y - 1 - i < 0 || x-1 < 0)) {
                    checkForward = (radius >= 2 && grid[y - 1 - i][x - 1].getCarId() != 0);
                }

//                if(checkRight){
//                    System.out.printf("[Car %d]: detected car to the right\n", carId);
//                }else if(checkLeft){
//                    System.out.printf("[Car %d]: detected car to the left\n", carId);
//                }else if(checkForward){
//                    System.out.printf("[Car %d]: detected car forward\n", carId);
//                }

                if (checkLeft || checkRight || checkForward) {
                    return true;
                }
            }
            // moving down
        } else if (y > carY) {
//            System.out.printf("[Car %d] moving down\n", carId);
            // check tiles of interseciton + radius
            for (int i = 0; i < radius + 1; i++) {
                // check left (y stays the same)
                if(!(x+i >= grid[y].length)) {
                    checkLeft = (grid[y][x + i].getCarId() != 0);
                }

                // check right (y + 1)
                if(!(y+i >= grid.length || x-i < 0)) {
                    checkRight = (grid[y + 1][x - i].getCarId() != 0);
                }

                // check forward (not if greedy)
                if(!(y + 1 + i >= grid.length || x + 1 >= grid[y].length)) {
                    checkForward = (radius >= 2 && grid[y + 1 + i][x + 1].getCarId() != 0);
                }

                if (checkLeft || checkRight || checkForward) {
                    return true;
                }
            }
        }

        return false;
    }
}
