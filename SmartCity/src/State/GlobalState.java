package State;

import Tiles.RoadTile;
import Tiles.Tile;

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

    public void setCarLocation(int x, int y, int id){
        if(grid != null) {
            grid[y][x].setCarId(id);
            System.out.println("[State.GlobalState] Grid state changed");
            updateGridDisplay();
        }
    }

    // TODO: use setCarLocation
    public void moveCar(int oldX, int oldY, int x, int y, int id){
        if(grid != null) {
            grid[oldY][oldX].setCarId(0);
            grid[y][x].setCarId(id);
            System.out.println("[State.GlobalState] Grid state changed");
            updateGridDisplay();
        }
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
