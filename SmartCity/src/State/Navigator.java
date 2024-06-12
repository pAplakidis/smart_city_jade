package State;

import Tiles.Tile;
import Tiles.RoadTile;

import javax.swing.*;
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

    public RoadTile getRandomRoadTile() {
        Random rand = new Random();
        int x = rand.nextInt(grid.length);
        int y = rand.nextInt(grid[0].length);
        while (!(grid[x][y] instanceof RoadTile) && grid[x][y].getValue() != -1) {
            x = rand.nextInt(grid.length);
            y = rand.nextInt(grid[0].length);
        }
        return (RoadTile) grid[x][y];
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

            if (current.tile.getValue() != -1)
                closedSet.add(current.tile);

            for (Tile neighbor : getNeighbors(current)) {
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

    private List<Tile> getNeighbors(Node current) {
        List<Tile> neighbors = new ArrayList<>();
        int x = current.tile.getX();
        int y = current.tile.getY();

        switch (current.tile.getValue()) {
            case -3:
                if (isRoadTile(x + 1, y, -1) || isRoadTile(x + 1, y, -3)) neighbors.add(grid[x + 1][y]);
                if (isRoadTile(x, y - 1, -1) || isRoadTile(x, y - 1, -3)) neighbors.add(grid[x][y - 1]);
                break;
            case -2:
                if (isRoadTile(x - 1, y, -1) || isRoadTile(x - 1, y, -2)) neighbors.add(grid[x - 1][y]);
                if (isRoadTile(x, y + 1, -1) || isRoadTile(x, y + 1, -2)) neighbors.add(grid[x][y + 1]);
                break;
            case -1:
                Node enterNode = current.parent;
                while (enterNode != null && isRoadTile(enterNode.tile.getX(), enterNode.tile.getY(), -1)) enterNode = enterNode.parent;
                RoadTile enterTile = enterNode == null ? null : (RoadTile) enterNode.tile;
                int nextX = x;
                int nextY = y;
                //TODO: Just do something better. This is a mess. And probably doesn't work with larger intersections.
                while (((isRoadTile(nextX, nextY + 1, -1) || isRoadTile(nextX, nextY + 1, -2))
                        && availableMove(enterTile, grid[nextX][nextY + 1])) || (nextY + 1 == grid[0].length && y + 1 != grid[0].length)) {
                    if (isRoadTile(nextX, nextY + 1, -2) || (nextY + 1 == grid[0].length)) {
                        neighbors.add(grid[x][y + 1]);
                        break;
                    }
                    nextY++;
                }
                nextX = x;
                nextY = y;
                while (((isRoadTile(nextX, nextY - 1, -1) || isRoadTile(nextX, nextY - 1, -3))
                        && availableMove(enterTile, grid[nextX][nextY - 1])) || nextY - 1 == -1 && y - 1 != -1) {
                    if (isRoadTile(nextX, nextY - 1, -3) || (nextY - 1 == -1)) {
                        neighbors.add(grid[x][y - 1]);
                        break;
                    }
                    nextY--;
                }
                nextX = x;
                nextY = y;
                while (((isRoadTile(nextX + 1, nextY, -1) || isRoadTile(nextX + 1, nextY, -3))
                        && availableMove(enterTile, grid[nextX + 1][nextY])) || nextX + 1 == grid.length) {
                    if (isRoadTile(nextX + 1, nextY, -3) || (nextX + 1 == grid.length && x + 1 != grid.length)) {
                        neighbors.add(grid[x + 1][y]);
                        break;
                    }
                    nextX++;
                }
                nextX = x;
                nextY = y;
                while (((isRoadTile(nextX - 1, nextY, -1) || isRoadTile(nextX - 1, nextY, -2))
                        && availableMove(enterTile, grid[nextX - 1][nextY])) || (nextX - 1 == -1 && x - 1 != -1)) {
                    if (isRoadTile(nextX - 1, nextY, -2) || (nextX - 1 == -1)) {
                        neighbors.add(grid[x - 1][y]);
                        break;
                    }
                    nextX--;
                }
                break;
            default:
                if (isRoadTile(x - 1, y)) neighbors.add(grid[x - 1][y]);
                if (isRoadTile(x + 1, y)) neighbors.add(grid[x + 1][y]);
                if (isRoadTile(x, y - 1)) neighbors.add(grid[x][y - 1]);
                if (isRoadTile(x, y + 1)) neighbors.add(grid[x][y + 1]);
        }

//        if (isRoadTile(x - 1, y)) neighbors.add(grid[x - 1][y]);
//        if (isRoadTile(x + 1, y)) neighbors.add(grid[x + 1][y]);
//        if (isRoadTile(x, y - 1)) neighbors.add(grid[x][y - 1]);
//        if (isRoadTile(x, y + 1)) neighbors.add(grid[x][y + 1]);
//
        return neighbors;
    }

    private boolean availableMove(RoadTile enterTile, Tile nextTile) {
        if (enterTile != null && enterTile.getValue() != -1 && nextTile.getValue() != -1 && enterTile.getValue() != nextTile.getValue()) {
            return enterTile.getX() != nextTile.getX() && enterTile.getY() != nextTile.getY();
        }
        return true;
    }

    private boolean isRoadTile(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length && grid[x][y] instanceof RoadTile;
    }

    private boolean isRoadTile(int x, int y, int value) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length && grid[x][y] instanceof RoadTile && grid[x][y].getValue() == value;
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

        assert grid != null;

        Navigator navigator = new Navigator(grid);
        RoadTile start = (RoadTile) grid[2][0];
        RoadTile end = (RoadTile) grid[3][19];

        List<Tile> path = navigator.findPath(start, end);

        System.out.println("Path length: " + path.size());

        for (Tile tile : path) {
            System.out.println("Path: (" + tile.getX() + ", " + tile.getY() + ", " + tile.getValue() + ")");
        }
    }
}
