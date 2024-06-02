package Tiles;

public class RoadTile extends Tile {
    private boolean isPriorityRoad = false;

    public RoadTile(int x, int y, int value) {
        super(x, y, value);
    }

    public RoadTile(Tile tile) {
        super(tile);
    }

    public void setPriority(boolean b) {
        this.isPriorityRoad = b;
    }

    public boolean hasPriority() {
        return isPriorityRoad;
    }
}
