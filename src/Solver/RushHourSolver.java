package Solver;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList; 
import java.util.Collections; 

public class RushHourSolver {

    public static class Solution {
        public final List<Move> moves;
        public final List<Board> path; 
        public final int nodesVisited;
        public final long executionTimeMillis;
        public final boolean EsolusiDitemukan;

        public Solution(List<Move> moves, List<Board> path, int nodesVisited, long executionTimeMillis, boolean EsolusiDitemukan) {
            this.moves = moves;
            this.path = path;
            this.nodesVisited = nodesVisited;
            this.executionTimeMillis = executionTimeMillis;
            this.EsolusiDitemukan = EsolusiDitemukan;
        }
    }

    public Solution solveWithUCS(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        int nodesVisitedCount = 0;

        PriorityQueue<SolverNode> openSet = new PriorityQueue<>();
        Set<Board> closedSet = new HashSet<>(); 

        SolverNode startNode = new SolverNode(initialBoard);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            SolverNode currentNode = openSet.poll(); 
            nodesVisitedCount++; 

            if (closedSet.contains(currentNode.getBoardState())) {
                continue; 
            }
            closedSet.add(currentNode.getBoardState());
            
            if (currentNode.getBoardState().isGoalState()) {
                long endTime = System.currentTimeMillis();
                List<Move> movesToSolution = currentNode.getMovesToSolution(); 
                List<Board> pathToSolution = currentNode.getPathToSolution();
                
                return new Solution(movesToSolution, pathToSolution, nodesVisitedCount, endTime - startTime, true);
            }

            List<Move> possibleMoves = currentNode.getBoardState().getAllPossibleMoves();

            for (Move move : possibleMoves) {
                Board nextBoardState = currentNode.getBoardState().generateNewBoardState(move);
                if (!closedSet.contains(nextBoardState)) { 
                    int newGCost = currentNode.getGCost() + 1; 
                    SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, newGCost);
                    openSet.add(successorNode);
                } 
            }
        }

        // Tidak ada solusi ditemukan
        long endTime = System.currentTimeMillis();
        System.out.println("TIDAK ADA SOLUSI.");
        return new Solution(Collections.emptyList(), Collections.emptyList(), nodesVisitedCount, endTime - startTime, false);
    }
}