import State.GridLoader;
import Tiles.RoadTile;
import Tiles.Tile;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class GridVisualizer extends JPanel {
    private int[][] grid;
    private List<Tile> path;
    private int cellSize = 30; // Size of each cell in the grid

    public GridVisualizer(String filename) {
        grid = loadGridFromFile(filename);
        this.setPreferredSize(new Dimension(grid[0].length * cellSize + 20, grid.length * cellSize + 20));
    }

    public void setPath(List<Tile> path) {
        this.path = path;
        repaint();
    }

    private int[][] loadGridFromFile(String filename) {
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
            int[][] grid = new int[rows][cols];
            br.close();

            BufferedReader br2 = new BufferedReader(new FileReader(filename));
            int row = 0;
            line = br2.readLine();
            while (line != null) {
                String[] values = line.split(" ");
                for (int col = 0; col < cols; col++) {
                    grid[row][col] = Integer.parseInt(values[col]);
                }
                row++;
                line = br2.readLine();
            }
            br2.close();
            return grid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grid == null) return;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int cellValue = grid[y][x];
                switch (cellValue) {
                    case -3:
                        g.setColor(Color.MAGENTA);
                        break;
                    case -2:
                        g.setColor(Color.PINK);
                        break;
                    case -1:
                        g.setColor(Color.ORANGE);
                        break;
                    case 0:
                        g.setColor(Color.BLACK);
                        break; // Roads
                    case 1:
                        g.setColor(Color.LIGHT_GRAY);
                        break; // General Buildings
                    case 2:
                        g.setColor(Color.WHITE);
                        break;   // Hospitals
                    case 3:
                        g.setColor(Color.RED);
                        break;  // Fire Departments
                    case 4:
                        g.setColor(Color.BLUE);
                        break; // Police Stations
                    default:
                        g.setColor(Color.GREEN);
                        break; // Undefined
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY); // Grid lines color
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw text annotations for special buildings
                g.setColor(Color.BLACK);
                String text = cellValue == -1 ? "I" : cellValue == -2 ? "R" : cellValue == -3 ? "L" : cellValue == 2 ? "H" : cellValue == 3 ? "F" : cellValue == 4 ? "P" : "";
                g.drawString(text, x * cellSize + cellSize / 2 - 4, y * cellSize + cellSize / 2 + 4);
            }
        }

        // Draw the path with a gradient from green to dark green and numbers
        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size(); i++) {
                Tile tile = path.get(i);
                int x = tile.getX();
                int y = tile.getY();
                float ratio = (float) i / path.size();
                Color color = blendColors(Color.GREEN, new Color(0, 100, 0), ratio);
                g.setColor(color);
                g.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(i + 1), y * cellSize + cellSize / 2 - 4, x * cellSize + cellSize / 2 + 4); // Draw the order number
            }
        }

        // Draw axis labels
        g.setColor(Color.BLACK);
        for (int i = 0; i < grid.length; i++) {
            g.drawString(String.valueOf(i), 0, i * cellSize + 10);
        }
        for (int j = 0; j < grid[0].length; j++) {
            g.drawString(String.valueOf(j), j * cellSize, 10);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (grid != null) {
            return new Dimension(grid[0].length * cellSize, grid.length * cellSize);
        } else {
            return super.getPreferredSize();
        }
    }

    private Color blendColors(Color c1, Color c2, float ratio) {
        int red = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int green = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int blue = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(red, green, blue);
    }

    public static void main(String[] args) {
        String filename = "grid_world.txt"; // Path to your grid file

        JFrame frame = new JFrame("Grid World Visualizer");
        GridVisualizer visualizer = new GridVisualizer(filename);
        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Example usage:
        Tile[][] grid = GridLoader.loadGridFromFile(filename);

        Navigator navigator = new Navigator(grid);
        assert grid != null;
        RoadTile start = null;
        RoadTile end = null;
        while (start == null) {
            Tile tile = grid[(int) (Math.random() * grid.length)][(int) (Math.random() * grid[0].length)];
            if (tile instanceof RoadTile) {
                start = (RoadTile) tile;
            }
        }
        while (end == null) {
            Tile tile = grid[(int) (Math.random() * grid.length)][(int) (Math.random() * grid[0].length)];
            if (tile instanceof RoadTile) {
                end = (RoadTile) tile;
            }
        }
        System.out.println("Start: (" + start.getX() + ", " + start.getY() + ", " + start.getValue() + ")");
        System.out.println("End: (" + end.getX() + ", " + end.getY() + ", " + end.getValue() + ")");

        List<Tile> path = navigator.findPath(start, end);
        visualizer.setPath(path);

        for (Tile tile : path) {
            System.out.println("Path: (" + tile.getX() + ", " + tile.getY() + ")");
        }
    }
}