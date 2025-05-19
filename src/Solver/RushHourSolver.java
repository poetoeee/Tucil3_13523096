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

    private int idaNodesVisitedTotal;
    private static final int IDA_FOUND = -1; 
    private static final int IDA_NOT_FOUND_INCREASE_LIMIT = -2; 

    public Solution solveWithIDAStar(Board initialBoard, String heuristicType) {
        long startTime = System.currentTimeMillis();
        idaNodesVisitedTotal = 0;

        // Hitung h-cost awal
        int initialHCost;
        if ("Manhattan Distance".equals(heuristicType)) {
            initialHCost = initialBoard.calculateManhattanDistanceHeuristic();
        } else { 
            initialHCost = initialBoard.calculateBlockingPiecesHeuristic();
        }

        int fLimit = initialHCost;
        SolverNode startNode = new SolverNode(initialBoard, null, null, 0, initialHCost);

        final int MAX_IDA_ITERATIONS = 100; 
        final int MAX_NODES_VISITED_IDA = 7000000; 

        for (int iteration = 0; iteration < MAX_IDA_ITERATIONS; iteration++) {
            System.out.println("IDA*: Iterasi " + (iteration + 1) + ", fLimit = " + fLimit + ", Total Nodes Sejauh Ini: " + idaNodesVisitedTotal);
            Set<Board> pathCurrentlyExploring = new HashSet<>();
            Object[] searchResult = ida_search(startNode, fLimit, heuristicType, pathCurrentlyExploring);
            SolverNode solutionNode = (SolverNode) searchResult[0];
            int nextFLimitCandidate = (Integer) searchResult[1];

            if (solutionNode != null) { 
                long endTime = System.currentTimeMillis();
                List<Move> movesToSolution = solutionNode.getMovesToSolution();
                List<Board> pathToSolution = solutionNode.getPathToSolution();
                System.out.println("IDA* menemukan solusi dengan fCost = " + solutionNode.getFCost() + " (g=" + solutionNode.getGCost() + ", h=" + solutionNode.getHCost() + "), Total Nodes: " + idaNodesVisitedTotal);
                return new Solution(movesToSolution, pathToSolution, idaNodesVisitedTotal, endTime - startTime, true);
            }

            if (nextFLimitCandidate == Integer.MAX_VALUE) {
                System.out.println("IDA*: Tidak ada node lagi yang bisa dieksplorasi (nextFLimit adalah MAX_VALUE). Tidak ada solusi.");
                break;
            }
            fLimit = nextFLimitCandidate;
            if (idaNodesVisitedTotal >= MAX_NODES_VISITED_IDA) {
                System.out.println("IDA*: Batas total node (" + MAX_NODES_VISITED_IDA + ") tercapai.");
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("IDA*: Gagal menemukan solusi setelah semua iterasi atau batas tercapai. Total Nodes: " + idaNodesVisitedTotal);
        return new Solution(Collections.emptyList(), Collections.emptyList(), idaNodesVisitedTotal, endTime - startTime, false);
    }

    private Object[] ida_search(SolverNode currentNode, int fLimit, String heuristicType, Set<Board> pathCurrentlyExploring) {
        idaNodesVisitedTotal++;
        Board currentBoardState = currentNode.getBoardState();
        int currentFCost = currentNode.getFCost();

        if (currentFCost > fLimit) {
            return new Object[]{null, currentFCost};
        }

        if (currentBoardState.isGoalState()) {
            return new Object[]{currentNode, currentFCost}; 
        }

        if (pathCurrentlyExploring.contains(currentBoardState)) {
            return new Object[]{null, Integer.MAX_VALUE}; 
        }
        pathCurrentlyExploring.add(currentBoardState);

        int minNextFLimit = Integer.MAX_VALUE; 
        List<Move> possibleMoves = currentBoardState.getAllPossibleMoves();

        for (Move move : possibleMoves) {
            Board nextBoardState = currentBoardState.generateNewBoardState(move);
            int gCostSuccessor = currentNode.getGCost() + 1;
            int hCostSuccessor;

            if ("Manhattan Distance".equals(heuristicType)) {
                hCostSuccessor = nextBoardState.calculateManhattanDistanceHeuristic();
            } else {
                hCostSuccessor = nextBoardState.calculateBlockingPiecesHeuristic();
            }

            SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, gCostSuccessor, hCostSuccessor);

            Object[] searchResult = ida_search(successorNode, fLimit, heuristicType, pathCurrentlyExploring);
            SolverNode foundSolution = (SolverNode) searchResult[0];
            int potentialNextFLimit = (Integer) searchResult[1];

            if (foundSolution != null) { 
                pathCurrentlyExploring.remove(currentBoardState); 
                return new Object[]{foundSolution, 0}; 
            }

            if (potentialNextFLimit < minNextFLimit) {
                minNextFLimit = potentialNextFLimit;
            }
        }

        pathCurrentlyExploring.remove(currentBoardState); 
        return new Object[]{null, minNextFLimit}; 
    }


    private int idsNodesVisitedTotal;
    public Solution solveWithIDS(Board initialBoard) {
        long startTime = System.currentTimeMillis();
        idsNodesVisitedTotal = 0; 
        SolverNode startNode = new SolverNode(initialBoard, 0); 
        for (int depthLimit = 0; ; depthLimit++) { 
            System.out.println("IDS: Mencoba dengan depthLimit = " + depthLimit);
            Set<Board> visitedInCurrentDls = new HashSet<>();

            SolverNode solutionNode = dls(startNode, depthLimit, 0, visitedInCurrentDls);

            if (solutionNode != null) {
                long endTime = System.currentTimeMillis();
                List<Move> movesToSolution = solutionNode.getMovesToSolution();
                List<Board> pathToSolution = solutionNode.getPathToSolution();
                System.out.println("IDS menemukan solusi pada kedalaman: " + solutionNode.getGCost());
                return new Solution(movesToSolution, pathToSolution, idsNodesVisitedTotal, endTime - startTime, true);
            }

            if (idsNodesVisitedTotal > 2000000 && depthLimit > 30) { // Batas pengaman sementara
                System.out.println("IDS: Melebihi batas iterasi/node, kemungkinan tidak ada solusi atau solusi sangat dalam.");
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        return new Solution(Collections.emptyList(), Collections.emptyList(), idsNodesVisitedTotal, endTime - startTime, false);
    }

    private SolverNode dls(SolverNode currentNode, int depthLimit, int currentDepth, Set<Board> visitedInCurrentDls) {
        idsNodesVisitedTotal++; 

        Board currentBoardState = currentNode.getBoardState();

        if (currentBoardState.isGoalState()) {
            return currentNode; 
        }

        if (currentDepth >= depthLimit) {
            return null; 
        }

        if (visitedInCurrentDls.contains(currentBoardState)) {
            return null; 
        }
        visitedInCurrentDls.add(currentBoardState);

        List<Move> possibleMoves = currentBoardState.getAllPossibleMoves();

        for (Move move : possibleMoves) {
            Board nextBoardState = currentBoardState.generateNewBoardState(move);
            SolverNode successorNode = new SolverNode(nextBoardState, currentNode, move, currentDepth + 1, 0); 

            SolverNode resultNode = dls(successorNode, depthLimit, currentDepth + 1, visitedInCurrentDls);

            if (resultNode != null) {
                visitedInCurrentDls.remove(currentBoardState); 
                return resultNode;
            }
        }

        visitedInCurrentDls.remove(currentBoardState); 
        return null; 
    }
}