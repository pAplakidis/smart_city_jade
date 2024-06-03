package State;

import Tiles.RoadTile;
import Tiles.Tile;
import Vehicles.CarAgent;

import javax.swing.*;
import java.util.List;

public class GlobalState {
    private Tile[][] grid;
    private static GlobalState instance;
    private GridVisualizer visualizer;
    private JFrame frame;

    private GlobalState(){
        grid = GridLoader.loadGridFromFile("grid_world.txt");
        initDisplay();
    }

    public static synchronized  GlobalState getInstance(){
        if(instance == null){
            instance = new GlobalState();
        }
        return instance;
    }

    public Tile[][] getGrid(){
        return grid;
    }

    public void setCarLocation(int x, int y, CarAgent agent){
        if(grid != null) {
            grid[y][x].setAgent(agent);
            System.out.println("[State.GlobalState] Grid state changed");
            updateGridDisplay();
        }
    }

    // States
    // 0: no grid
    // 1: moved normally
    // 2: crashed
    public int moveCar(int oldX, int oldY, int x, int y, CarAgent agent){
        int status = 0;

        if(grid == null) {
            return 0;
        }

        // check for crash
        int possibleCrashId = grid[y][x].getCarId();
        if(possibleCrashId == 0){
            grid[oldY][oldX].setAgent(null);
            grid[y][x].setAgent(agent);
            status = 1;
        }else{
            // TODO: stop car we crashed into as well
            // get the agent of that location and set a boolean for it to stop
            System.out.printf("[GlobalState] Crash detected: Car%d[%d,%d] - Car%d[%d,%d]\n", agent.getId(), oldY, oldX, possibleCrashId, y, x);
            status = 2;
        }

        updateGridDisplay();
        return status;
    }

    public void initDisplay(){
        visualizer = new GridVisualizer(grid);
        frame = new JFrame("Grid World Visualizer");
        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        showGrid();
    }

    public void showGrid(){
        if(visualizer != null && grid != null) {
            visualizer.printGrid(grid);
        }
    }

    public void updateGridDisplay(){
        if(visualizer != null && grid != null && frame != null){
            visualizer.updateGrid(grid);
            showGrid();
        }
    }

    public void updatePathDisplay(List<Tile> path){
        if(visualizer != null && grid != null && frame != null){
            visualizer.setPath(path);
            showGrid();
        }
    }
}
