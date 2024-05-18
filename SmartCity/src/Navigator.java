import Tiles.Tile;
import Tiles.Roads.RoadTile;

import java.util.*;

public class Navigator {
    private Tile[][] grid;

    public Navigator(Tile[][] grid) {
        this.grid = grid;
    }

    private class Node implements Comparable<Node> {
        Tile tile;
        Node parent;
        double gCost;
        double hCost;

        public Node(Tile tile, Node parent, double gCost, double hCost) {
            this.tile = tile;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        public double getFCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getFCost(), other.getFCost());
        }
    }

    public List<Tile> findPath(RoadTile start, RoadTile end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Tile> closedSet = new HashSet<>();

        openSet.add(new Node(start, null, 0, getHeuristicCost(start, end)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.tile.equals(end)) {
                return reconstructPath(current);
            }

            closedSet.add(current.tile);

            System.out.println("Current: " + current.tile.getX() + " " + current.tile.getY() + " " + current.tile.getValue());

            for (Tile neighbor : getNeighbors(current.tile)) {
                System.out.println("Neighbor: " + neighbor.getX() + " " + neighbor.getY() + " " + neighbor.getValue() + " " + closedSet.size());
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGCost = current.gCost + getDistance(current.tile, neighbor);

                Node neighborNode = new Node(neighbor, current, tentativeGCost, getHeuristicCost(neighbor, end));

                if (!openSet.contains(neighborNode)) {
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList();  // No path found
    }

    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();
        int x = tile.getX();
        int y = tile.getY();

        if (isRoadTile(x - 1, y)) neighbors.add(grid[x - 1][y]);
        if (isRoadTile(x + 1, y)) neighbors.add(grid[x + 1][y]);
        if (isRoadTile(x, y - 1)) neighbors.add(grid[x][y - 1]);
        if (isRoadTile(x, y + 1)) neighbors.add(grid[x][y + 1]);

        return neighbors;
    }

    private boolean isRoadTile(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length && grid[x][y] instanceof RoadTile;
    }

    private double getHeuristicCost(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());  // Manhattan distance
    }

    private double getDistance(Tile a, Tile b) {
        return 1;  // Assume uniform cost for moving to a neighbor
    }

    private List<Tile> reconstructPath(Node node) {
        List<Tile> path = new ArrayList<>();
        while (node != null) {
            path.add(node.tile);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        // Example usage:
        Tile[][] grid = GridLoader.loadGridFromFile("grid_world.txt");

        Navigator navigator = new Navigator(grid);
        RoadTile start = (RoadTile) grid[0][0];
        RoadTile end = (RoadTile) grid[10][5];

        List<Tile> path = navigator.findPath(start, end);

        for (Tile tile : path) {
            System.out.println("Path: (" + tile.getX() + ", " + tile.getY() + ", " + tile.getValue() + ")");
        }
    }
}
