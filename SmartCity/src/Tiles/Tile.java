package Tiles;
//TODO: Use ENUM for tile types
public class Tile {
    private final int x;
    private final int y;
    private int value;

    private int carId = 0;

    public Tile(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public Tile(Tile tile) {
        this.x = tile.x;
        this.y = tile.y;
        this.value = tile.value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int val){
        value = val;
    }

    public int getCarId(){return carId;}

    public void setCarId(int id){
        carId = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tile tile = (Tile) obj;
        return x == tile.x && y == tile.y;
    }

    @Override
    public int hashCode() {
        return (int) Math.pow(2, 16) * x + y;
    }

}
