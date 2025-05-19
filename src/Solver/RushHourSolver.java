package Solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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
    
        PriorityQueue<SolverNode> openSet = new PriorityQueue<>(new SolverNode.UcsComparator());
        Set<Board> closedSet = new HashSet<>();
    
        int startHCost = initialBoard.calculateBlockingPiecesHeuristic(); 
        SolverNode startNode = new SolverNode(initialBoard, startHCost); 
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
                    int nextHCost = nextBoardState.calculateBlockingPiecesHeuristic(); 
                    SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, newGCost, nextHCost);
                    openSet.add(successorNode);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        return new Solution(Collections.emptyList(), Collections.emptyList(), nodesVisitedCount, endTime - startTime, false);
    }

    public Solution solveWithGreedyBFS(Board initialBoard, String heuristicType) {
        long startTime = System.currentTimeMillis();
        int nodesVisitedCount = 0;

        PriorityQueue<SolverNode> openSet = new PriorityQueue<>(new SolverNode.GreedyBfsComparator());
        Set<Board> closedSet = new HashSet<>();

        // int startHCost = initialBoard.calculateBlockingPiecesHeuristic();
        int startHCost;
        if (heuristicType.equals("Blocking Pieces")) {
            startHCost = initialBoard.calculateBlockingPiecesHeuristic();
        } else {
            startHCost = initialBoard.calculateManhattanDistanceHeuristic();
        } 

        SolverNode startNode = new SolverNode(initialBoard, startHCost); 
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            SolverNode currentNode = openSet.poll(); 
            nodesVisitedCount++;
            // currentNode.getBoardState().printBoard();

            if (closedSet.contains(currentNode.getBoardState())) {
                continue;
            }
            closedSet.add(currentNode.getBoardState());

            if (currentNode.getBoardState().isGoalState()) {
                long endTime = System.currentTimeMillis();
                List<Move> movesToSolution = currentNode.getMovesToSolution();
                List<Board> pathToSolution = currentNode.getPathToSolution();
                System.out.println("GBFS menemukan solusi dengan gCost (langkah aktual): " + currentNode.getGCost());
                return new Solution(movesToSolution, pathToSolution, nodesVisitedCount, endTime - startTime, true);
            }

            List<Move> possibleMoves = currentNode.getBoardState().getAllPossibleMoves();
            for (Move move : possibleMoves) {
                Board nextBoardState = currentNode.getBoardState().generateNewBoardState(move);

                if (!closedSet.contains(nextBoardState)) {
                    int newGCost = currentNode.getGCost() + 1; // Tetap hitung gCost untuk informasi
                    int nextHCost;
                    if (heuristicType.equals("Blocking Pieces")) {
                        nextHCost = nextBoardState.calculateBlockingPiecesHeuristic();
                    } else {
                        nextHCost = nextBoardState.calculateManhattanDistanceHeuristic();
                    }
                    SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, newGCost, nextHCost);
                    openSet.add(successorNode);
                } 
            }
        }

        long endTime = System.currentTimeMillis();
        return new Solution(Collections.emptyList(), Collections.emptyList(), nodesVisitedCount, endTime - startTime, false);
    }

    public Solution solveWithAStar(Board initialBoard, String heuristicType) {
        long startTime = System.currentTimeMillis();
        int nodesVisitedCount = 0;

        PriorityQueue<SolverNode> openSet = new PriorityQueue<>(new SolverNode.AStarComparator());
        Set<Board> closedSet = new HashSet<>();
        
        int startHCost;
        if (heuristicType.equals("Blocking Pieces")) {
            startHCost = initialBoard.calculateBlockingPiecesHeuristic();
        } else {
            startHCost = initialBoard.calculateManhattanDistanceHeuristic();
        }
        SolverNode startNode = new SolverNode(initialBoard, startHCost);
        openSet.add(startNode);
        // if (usingOpenSetMap) openSetMap.put(initialBoard, startNode);

        while (!openSet.isEmpty()) {
            SolverNode currentNode = openSet.poll();
            nodesVisitedCount++;

            if (closedSet.contains(currentNode.getBoardState())) {
                continue;
            }
            closedSet.add(currentNode.getBoardState());

            if (currentNode.getBoardState().isGoalState()) {
                long endTime = System.currentTimeMillis();
                return new Solution(currentNode.getMovesToSolution(), currentNode.getPathToSolution(), nodesVisitedCount, endTime - startTime, true);
            }

            List<Move> possibleMoves = currentNode.getBoardState().getAllPossibleMoves();
            for (Move move : possibleMoves) {
                Board nextBoardState = currentNode.getBoardState().generateNewBoardState(move);

                if (closedSet.contains(nextBoardState)) {
                    continue; 
                }

                int newGCost = currentNode.getGCost() + 1;
                int nextHCost;
                if (heuristicType.equals("Blocking Pieces")) {
                    nextHCost = nextBoardState.calculateBlockingPiecesHeuristic();
                } else {
                    nextHCost = nextBoardState.calculateManhattanDistanceHeuristic();
                }
                SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, newGCost, nextHCost);

                openSet.add(successorNode);
            }
        }
        long endTime = System.currentTimeMillis();
        return new Solution(Collections.emptyList(), Collections.emptyList(), nodesVisitedCount, endTime - startTime, false);
    }
}