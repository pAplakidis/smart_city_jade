package Vehicles;

import Tiles.Tile;

public class Vehicle {
    private Tile currentTile;

    public Vehicle(Tile currentTile) {
        this.currentTile = currentTile;
    }

    public Tile getCurrentTile() {
        return currentTile;
    }
}
