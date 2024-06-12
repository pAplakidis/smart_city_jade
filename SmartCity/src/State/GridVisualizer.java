package State;

import Tiles.BuildingTile;
import Tiles.RoadTile;
import Tiles.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GridVisualizer extends JPanel {
    private Tile[][] grid;
    private List<Tile> path;
    private int cellSize = 30; // Size of each cell in the grid

    public GridVisualizer(Tile[][] grid) {
        this.grid = grid;
        this.setPreferredSize(new Dimension(grid[0].length * cellSize + 20, grid.length * cellSize + 20));
    }

    public void updateGrid(Tile[][] newGrid) {
        this.grid = newGrid;
        repaint();
    }

    public void setPath(List<Tile> path) {
        this.path = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grid == null) return;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int cellValue = grid[y][x].getValue();
                int carValue = grid[y][x].getCarId();

                // TODO: color based on car type
                if (carValue != 0) {
//                    System.out.println("Car value: " + carValue);
                    g.setColor(Color.darkGray);
                } else {
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
                            if (((BuildingTile) grid[y][x]).isOnFire()) {
                                g.setColor(Color.RED);
                            } else {
                                g.setColor(Color.LIGHT_GRAY);
                            }
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
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY); // Grid lines color
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Draw text annotations for special buildings
                g.setColor(Color.BLACK);
                String text;
                if (carValue != 0) {
//                    text = "C";
                    text = Integer.toString(carValue);
                } else {
                    switch (cellValue) {
                        case -1:
                            text = "I";
                            break;
                        case -2:
                            text = "R";
                            break;
                        case -3:
                            text = "L";
                            break;
                        case 1:
                            if (((BuildingTile) grid[y][x]).isOnFire()) {
                                text = "\uD83D\uDD25";
                            } else {
                                text = "";
                            }
                            break;
                        case 2:
                            text = "H";
                            break;
                        case 3:
                            text = "F";
                            break;
                        case 4:
                            text = "P";
                            break;
                        default:
                            text = "";
                            break;
                    }
                }
//                String text = cellValue == -1 ? "I" : cellValue == -2 ? "R" : cellValue == -3 ? "L" : cellValue == 2 ? "H" : cellValue == 3 ? "F" : cellValue == 4 ? "P" : "";
                g.drawString(text, x * cellSize + cellSize / 2 - 4, y * cellSize + cellSize / 2 + 4);
            }
        }

        // Draw the path with a gradient from green to dark green and numbers
        if (path != null && !path.isEmpty()) {
            for (int i = 0; i < path.size(); i++) {
                Tile tile = path.get(i);
                int x = tile.getX();
                int y = tile.getY();
                if(grid[x][y].getCarId() != 0){
                    continue;
                }

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

    public void printGrid(Tile[][] grid) {
        for (Tile[] row : grid) {
            for (Tile element : row) {
                if (element.getCarId() != 0) {
                    System.out.print(element.getCarId() + "\t");
                } else {
                    System.out.print(element.getValue() + "\t");
                }
            }
            System.out.println();  // Move to the next line after each row
        }
        System.out.println();
    }

    private Color blendColors(Color c1, Color c2, float ratio) {
        int red = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int green = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int blue = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(red, green, blue);
    }

    public static void main(String[] args) {
        Tile[][] grid = GridLoader.loadGridFromFile("grid_world.txt");

        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile instanceof RoadTile) {
                    System.out.print(((RoadTile) tile).hasPriority() ? "V" : "H");
                } else {
                    System.out.print(" ");
                }
                System.out.print(tile.getValue() + "\t");
            }
            System.out.println();
        }

        // Set a random tile on fire
        ((BuildingTile) grid[5][5]).setOnFire(true);


        JFrame frame = new JFrame("Grid World Visualizer");
        GridVisualizer visualizer = new GridVisualizer(grid);
        frame.add(visualizer);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Example usage:
        Navigator navigator = new Navigator(grid);

        assert grid != null;
//        Moving from 0 2 to 14 25
        RoadTile start = (RoadTile) grid[4][0];
        RoadTile end = (RoadTile) grid[3][1];

        List<Tile> path = navigator.findPath(start, end);
        visualizer.setPath(path);

        for (Tile tile : path) {
            System.out.println("Path: (" + tile.getX() + ", " + tile.getY() + ")");
        }
    }
}