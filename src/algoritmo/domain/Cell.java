package algoritmo.domain;

public class Cell {
    private int x;
    private int y;

    private Cell(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public static Cell create(int x, int y) { return new Cell(x, y); }

    @Override
    public String toString() {
        return "x=" + x + ",y=" + y;
    }
}
