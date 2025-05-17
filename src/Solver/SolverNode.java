package Solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List; 

public class SolverNode {
    private Board boardState;
    private SolverNode parent;
    private Move moveThatLedToThisState;
    private int gCost;
    private int hCost;
    // private int fCost;

    public SolverNode(Board initialBoardState, int hCost) {
        this.boardState = initialBoardState;
        this.parent = null;
        this.moveThatLedToThisState = null;
        this.gCost = 0; 
        this.hCost = hCost;
    }

    public SolverNode(Board boardState, SolverNode parent, Move moveThatLedToThisState, int gCost, int hCost) {
        this.boardState = boardState;
        this.parent = parent;
        this.moveThatLedToThisState = moveThatLedToThisState;
        this.gCost = gCost;
        this.hCost = hCost;
    }

    public Board getBoardState() {
        return boardState;
    }

    public SolverNode getParent() {
        return parent;
    }

    public Move getMoveThatLedToThisState() {
        return moveThatLedToThisState;
    }

    public int getGCost() {
        return gCost;
    }

    public int getHCost() {
        return hCost;
    }

    public List<Board> getPathToSolution() {
        List<Board> path = new ArrayList<>();
        SolverNode currentNode = this; 

        while (currentNode != null) {
            path.add(currentNode.getBoardState()); 
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        return path;
    }
    
    public List<Move> getMovesToSolution() {
        List<Move> moves = new ArrayList<>();
        SolverNode currentNode = this; 

        while (currentNode != null && currentNode.getParent() != null) { 
            if (currentNode.getMoveThatLedToThisState() != null) {
                moves.add(currentNode.getMoveThatLedToThisState()); 
            }
            currentNode = currentNode.getParent(); 
        }
        Collections.reverse(moves);
        return moves;
    }

    // @Override
    // public int compareTo(SolverNode other) {
    //     return Integer.compare(this.gCost, other.gCost);
    // }

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     SolverNode that = (SolverNode) o;
    //     return boardState.equals(that.boardState); 
    // }

    // @Override
    // public int hashCode() {
    //     return boardState.hashCode(); 
    // }

    @Override
    public String toString() {
        return "SolverNode{" +
               "boardStateHash=" + (boardState != null ? boardState.hashCode() : "null") +
               ", gCost=" + gCost +
               ", hCost=" + hCost +
               ", parentExists=" + (parent != null) +
               (moveThatLedToThisState != null ? ", move=" + moveThatLedToThisState.getPieceId() + "-" + moveThatLedToThisState.getDirection() : "") +
               '}';
    }

    // Comparator untuk UCS (berdasarkan gCost)
    public static class UcsComparator implements Comparator<SolverNode> {
        @Override
        public int compare(SolverNode node1, SolverNode node2) {
            return Integer.compare(node1.getGCost(), node2.getGCost());
        }
    }

    // Comparator untuk Greedy BFS (berdasarkan hCost)
    public static class GreedyBfsComparator implements Comparator<SolverNode> {
        @Override
        public int compare(SolverNode node1, SolverNode node2) {
            return Integer.compare(node1.getHCost(), node2.getHCost());
        }
    }
}