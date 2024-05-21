package State;

import Tiles.Tile;

public class GlobalState {
    private Tile[][] grid;
    private static GlobalState instance;

    private GlobalState(){
        grid = GridLoader.loadGridFromFile("grid_world.txt");
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
        grid[y][x].setCarId(id);
        System.out.println("[State.GlobalState] Grid state changed");
    }

    public void moveCar(int oldX, int oldY, int x, int y, int id){
        grid[oldY][oldX].setCarId(0);
        grid[y][x].setCarId(id);
        System.out.println("[State.GlobalState] Grid state changed");
    }

    public void showGrid(){
        for (Tile[] row : grid) {
            for (Tile element : row) {
                if(element.getCarId() != 0){
                    System.out.print(element.getCarId() + "\t");
                }else{
                    System.out.print(element.getValue() + "\t");
                }
            }
            System.out.println();  // Move to the next line after each row
        }
        System.out.println();
    }
}
