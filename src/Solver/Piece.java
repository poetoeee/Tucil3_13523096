package Solver; 

import java.util.Objects;

public class Piece {
    private final char id;
    private int x; 
    private int y; 
    private final int length;
    private final Orientation orientation;
    private final boolean isPrimary;

    public Piece(char id, int x, int y, int length, Orientation orientation, boolean isPrimary) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.length = length;
        this.orientation = orientation;
        this.isPrimary = isPrimary;
    }

    public char getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLength() {
        return length;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Piece copy() {
        return new Piece(this.id, this.x, this.y, this.length, this.orientation, this.isPrimary);
    }

    @Override
    public String toString() {
        return "Piece{" +
               "id=" + id +
               ", x=" + x +
               ", y=" + y +
               ", length=" + length +
               ", orientation=" + orientation +
               ", isPrimary=" + isPrimary +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return id == piece.id &&
               x == piece.x &&
               y == piece.y &&
               length == piece.length &&
               orientation == piece.orientation &&
               isPrimary == piece.isPrimary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, length, orientation, isPrimary);
    }
}