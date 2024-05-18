package Tiles.Buildings;

import Tiles.Tile;

public class BuildingTile extends Tile {
    public BuildingTile(int x, int y, int value) {
        super(x, y, value);
    }

    public BuildingTile(Tile tile) {
        super(tile);
    }
}
