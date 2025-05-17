package Solver; 

import java.util.Objects;

public class Move {
    public final char pieceId;
    public final Direction direction; 
    public final int steps;

    public Move(char pieceId, Direction direction, int steps) {
        if (steps <= 0) {
            throw new IllegalArgumentException("Jumlah langkah (steps) harus positif.");
        }
        this.pieceId = pieceId;
        this.direction = direction;
        this.steps = steps;
    }

    public char getPieceId() {
        return pieceId;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "Move{" +
               "pieceId=" + pieceId +
               ", direction=" + direction +
               ", steps=" + steps +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return pieceId == move.pieceId &&
               steps == move.steps &&
               direction == move.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceId, direction, steps);
    }
}