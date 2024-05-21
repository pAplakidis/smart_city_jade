package State;

import Tiles.*;
import Tiles.Roads.*;
import Tiles.Buildings.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GridLoader {
    public static Tile[][] loadGridFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            int rows = 0;
            int cols = line.split(" ").length;

            // First, count the number of rows
            while (line != null) {
                rows++;
                line = br.readLine();
            }

            // Now, read the file again to fill the grid
            int[][] intGrid = new int[rows][cols];
            br.close();

            BufferedReader br2 = new BufferedReader(new FileReader(filename));
            int row = 0;
            line = br2.readLine();
            while (line != null) {
                String[] values = line.split(" ");
                for (int col = 0; col < cols; col++) {
                    intGrid[row][col] = Integer.parseInt(values[col]);
                }
                row++;
                line = br2.readLine();
            }
            br2.close();
            Tile[][] grid = new Tile[intGrid.length][intGrid[0].length];
            for (row = 0; row < intGrid.length; row++) {
                for (int col = 0; col < intGrid[row].length; col++) {
                    switch (intGrid[row][col]) {
                        case -3:
                        case -2:
                        case -1:
                            grid[row][col] = new RoadTile(row, col, intGrid[row][col]);
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            grid[row][col] = new BuildingTile(row, col, intGrid[row][col]);
                            break;
                        default:
                            System.out.println("Invalid value in grid file: " + intGrid[row][col]);
                            grid[row][col] = new Tile(row, col, intGrid[row][col]);
                    }
                }
            }
            return grid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
