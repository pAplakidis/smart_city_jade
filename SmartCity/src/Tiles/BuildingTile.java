package Tiles;

public class BuildingTile extends Tile {
    private int buildingId;
    private boolean onFire = false;

    public BuildingTile(int x, int y, int value) {
        super(x, y, value);
    }

    public BuildingTile(Tile tile) {
        super(tile);
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public boolean isOnFire() {
        return onFire;
    }

    public void setOnFire(boolean onFire) {
        this.onFire = onFire;
    }


}
