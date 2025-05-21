package Solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Board {
    private final int rows;
    private final int cols;
    private final Map<Character, Piece> pieces; 
    private final int exitX; 
    private final int exitY; 
    private char[][] gridRepresentation; 

    public Board(int rows, int cols, Map<Character, Piece> piecesFromParser, int exitX, int exitY) { 
        this.rows = rows;
        this.cols = cols;
        this.pieces = new HashMap<>();
        for (Map.Entry<Character, Piece> entry : piecesFromParser.entrySet()) { 
            this.pieces.put(entry.getKey(), entry.getValue().copy()); 
        }
        this.exitX = exitX;
        this.exitY = exitY;
        this.initializeGrid(); 
    }

    public Board(Board otherBoard) {
        this.rows = otherBoard.rows;
        this.cols = otherBoard.cols;
        this.exitX = otherBoard.exitX;
        this.exitY = otherBoard.exitY;

        this.pieces = new HashMap<>();
        for (Map.Entry<Character, Piece> entry : otherBoard.pieces.entrySet()) {
            this.pieces.put(entry.getKey(), entry.getValue().copy()); // Benar, menggunakan copy()
        }
        this.initializeGrid(); 
    }

    private void initializeGrid() {
        this.gridRepresentation = new char[this.rows][this.cols];
        for (int i = 0; i < this.rows; i++) {
            Arrays.fill(this.gridRepresentation[i], '.');
        }

        for (Piece piece : this.pieces.values()) {
            placePieceOnGrid(piece);
        }
    }

    private void placePieceOnGrid(Piece piece) {
        if (piece.getOrientation() == Orientation.HORIZONTAL) {
            for (int i = 0; i < piece.getLength(); i++) {
                if (isValidCoordinate(piece.getY(), piece.getX() + i)) {
                    this.gridRepresentation[piece.getY()][piece.getX() + i] = piece.getId();
                }
            }
        } else { 
            for (int i = 0; i < piece.getLength(); i++) {
                if (isValidCoordinate(piece.getY() + i, piece.getX())) {
                    this.gridRepresentation[piece.getY() + i][piece.getX()] = piece.getId();
                }
            }
        }
    }

    private boolean isValidCoordinate(int r, int c) {
        return r >= 0 && r < this.rows && c >= 0 && c < this.cols;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Piece getPieceById(char id) {
        return this.pieces.get(id); 
    }

    public Map<Character, Piece> getAllPieces() {
        Map<Character, Piece> piecesCopy = new HashMap<>();
         for (Map.Entry<Character, Piece> entry : this.pieces.entrySet()) {
            piecesCopy.put(entry.getKey(), entry.getValue().copy());
        }
        return piecesCopy;
    }
    
    public List<Piece> getPiecesAsList() {
        List<Piece> pieceList = new ArrayList<>();
        for (Piece p : this.pieces.values()) {
            pieceList.add(p.copy()); 
        }
        return pieceList;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitY() {
        return exitY;
    }

    public char[][] getGridRepresentation() {
        char[][] copyGrid = new char[this.rows][this.cols];
        for (int i = 0; i < this.rows; i++) {
            copyGrid[i] = Arrays.copyOf(this.gridRepresentation[i], this.cols);
        }
        return copyGrid;
    }
    
    public char getCellContent(int r, int c) {
        if (isValidCoordinate(r, c)) {
            return this.gridRepresentation[r][c]; 
        }
        throw new IllegalArgumentException("Koordinat (" + r + "," + c + ") di luar batas papan."); 
    }

    public void printBoard() {
        System.out.println("Papan (" + rows + "x" + cols + "): Exit di sel (" + exitY + "," + exitX + ")");
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (i == exitY && j == exitX && this.gridRepresentation[i][j] == '.') {
                    System.out.print(this.gridRepresentation[i][j]);
                } else {
                    System.out.print(this.gridRepresentation[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println("---");
    }

    private boolean isCellOccupied(int r, int c) {
        if (!isValidCoordinate(r, c)) {
            return true; 
        }
        return this.gridRepresentation[r][c] != '.';
    }

    public List<Move> getPossibleMovesForPiece(char pieceId) {
        List<Move> possibleMoves = new ArrayList<>();
        Piece pieceToMove = this.getPieceById(pieceId); 

        if (pieceToMove == null) {
            System.err.println("Peringatan: Piece dengan ID '" + pieceId + "' tidak ditemukan untuk getPossibleMovesForPiece.");
            return possibleMoves;
        }

        if (pieceToMove.getOrientation() == Orientation.HORIZONTAL) {
            for (int steps = 1; ; steps++) {
                int headXAfterMove = pieceToMove.getX() + pieceToMove.getLength() - 1 + steps;
                
                if (!isValidCoordinate(pieceToMove.getY(), headXAfterMove)) { 
                    break; 
                }
                int cellToClearX = pieceToMove.getX() + pieceToMove.getLength() + (steps - 1);
                
                if (isCellOccupied(pieceToMove.getY(), cellToClearX)) { 
                    break;
                }
                if (pieceToMove.isPrimary() && 
                    pieceToMove.getY() == this.exitY && 
                    headXAfterMove == this.exitX && 
                    this.exitX == this.cols -1) { 
                        possibleMoves.add(new Move(pieceId, Direction.RIGHT, steps));
                } else {
                     possibleMoves.add(new Move(pieceId, Direction.RIGHT, steps));
                }
            }

            for (int steps = 1; ; steps++) {
                int headXAfterMove = pieceToMove.getX() - steps;

                if (!isValidCoordinate(pieceToMove.getY(), headXAfterMove)) { 
                    break;
                }
                int cellToClearX = pieceToMove.getX() - steps; 

                if (isCellOccupied(pieceToMove.getY(), cellToClearX)) {
                    break;
                }
                 if (pieceToMove.isPrimary() && 
                    pieceToMove.getY() == this.exitY && 
                    headXAfterMove == this.exitX && this.exitX == 0) { 
                        possibleMoves.add(new Move(pieceId, Direction.LEFT, steps));
                } else {
                    possibleMoves.add(new Move(pieceId, Direction.LEFT, steps));
                }
            }

        } else { 
            for (int steps = 1; ; steps++) {
                int headYAfterMove = pieceToMove.getY() + pieceToMove.getLength() - 1 + steps;
                if (!isValidCoordinate(headYAfterMove, pieceToMove.getX())) {
                    break;
                }
                int cellToClearY = pieceToMove.getY() + pieceToMove.getLength() + (steps - 1);
                if (isCellOccupied(cellToClearY, pieceToMove.getX())) {
                    break;
                }
                if (pieceToMove.isPrimary() && 
                    pieceToMove.getX() == this.exitX && 
                    headYAfterMove == this.exitY &&
                    this.exitY == this.rows -1) { 
                        possibleMoves.add(new Move(pieceId, Direction.DOWN, steps));
                } else {
                    possibleMoves.add(new Move(pieceId, Direction.DOWN, steps));
                }
            }

            for (int steps = 1; ; steps++) {
                int headYAfterMove = pieceToMove.getY() - steps;
                if (!isValidCoordinate(headYAfterMove, pieceToMove.getX())) {
                    break;
                }
                int cellToClearY = pieceToMove.getY() - steps;
                if (isCellOccupied(cellToClearY, pieceToMove.getX())) {
                    break;
                }
                 if (pieceToMove.isPrimary() && 
                    pieceToMove.getX() == this.exitX && 
                    headYAfterMove == this.exitY &&
                    this.exitY == 0) { 
                        possibleMoves.add(new Move(pieceId, Direction.UP, steps));
                } else {
                    possibleMoves.add(new Move(pieceId, Direction.UP, steps));
                }
            }
        }
        return possibleMoves;
    }

    public List<Move> getAllPossibleMoves() {
        List<Move> allMoves = new ArrayList<>();
        for (char pieceId : this.pieces.keySet()) {
            Piece p = this.pieces.get(pieceId);
            if (p.isPrimary() && isGoalState()) { 
               continue;
            }
            allMoves.addAll(getPossibleMovesForPiece(pieceId));
        }
        return allMoves;
    }

    public Board generateNewBoardState(Move move) {
        Board newBoard = new Board(this); 
        Piece pieceToMoveInNewBoard = newBoard.getPieceById(move.pieceId);

        if (pieceToMoveInNewBoard == null) {
            throw new IllegalArgumentException("Piece dengan ID '" + move.pieceId + "' tidak ditemukan di papan untuk digerakkan.");
        }

        switch (move.direction) {
            case UP:
                pieceToMoveInNewBoard.setY(pieceToMoveInNewBoard.getY() - move.steps);
                break;
            case DOWN:
                pieceToMoveInNewBoard.setY(pieceToMoveInNewBoard.getY() + move.steps);
                break;
            case LEFT:
                pieceToMoveInNewBoard.setX(pieceToMoveInNewBoard.getX() - move.steps);
                break;
            case RIGHT:
                pieceToMoveInNewBoard.setX(pieceToMoveInNewBoard.getX() + move.steps);
                break;
        }
        
        newBoard.initializeGrid(); 

        return newBoard;
    }

    public boolean isGoalState() {
        Piece primaryPiece = null;
        for (Piece p : this.pieces.values()) {
            if (p.isPrimary()) {
                primaryPiece = p;
                break;
            }
        }

        if (primaryPiece == null) {
            return false;  
        }

        if (primaryPiece.getOrientation() == Orientation.HORIZONTAL) {
            if (primaryPiece.getY() == this.exitY) {
                if (this.exitX == 0) { 
                    return primaryPiece.getX() == 0; 
                } else if (this.exitX == this.cols - 1) { 
                    return (primaryPiece.getX() + primaryPiece.getLength()) == this.cols;
                }
            }
        } else { 
            if (primaryPiece.getX() == this.exitX) {
                if (this.exitY == 0) { 
                    return primaryPiece.getY() == 0; 
                } else if (this.exitY == this.rows - 1) { 
                    return (primaryPiece.getY() + primaryPiece.getLength()) == this.rows;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        if (rows != board.rows || cols != board.cols || exitX != board.exitX || exitY != board.exitY) {
            return false;
        }
        if (this.pieces.size() != board.pieces.size()) { 
            return false;
        }
        for (Map.Entry<Character, Piece> entry : this.pieces.entrySet()) {
            Piece thisPiece = entry.getValue();
            Piece otherPiece = board.pieces.get(entry.getKey()); 
            if (otherPiece == null || !thisPiece.equals(otherPiece)) { 
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rows, cols, exitX, exitY);
        result = 31 * result + pieces.hashCode(); // Piece.hashCode() harus konsisten dengan Piece.equals()
        return result;
    }

    public int calculateBlockingPiecesHeuristic() {
        Piece primaryPiece = this.getPieceById('P');
        if (primaryPiece == null) {
            System.err.println("Error di heuristik: Primary piece 'P' tidak ditemukan!");
            return Integer.MAX_VALUE; 
        }

        if (this.isGoalState()) {
            return 0;
        }

        Set<Character> blockingPieces = new HashSet<>();
        char[][] grid = this.gridRepresentation; 

        if (primaryPiece.getOrientation() == Orientation.HORIZONTAL) {
            if (this.exitX == 0) { // Pintu keluar di kiri
                for (int c = primaryPiece.getX() - 1; c >= 0; c--) {
                    if (grid[primaryPiece.getY()][c] != '.' && grid[primaryPiece.getY()][c] != primaryPiece.getId()) {
                        blockingPieces.add(grid[primaryPiece.getY()][c]);
                    }
                }
            } else { // Pintu keluar di kanan (this.exitX == this.cols - 1)
                for (int c = primaryPiece.getX() + primaryPiece.getLength(); c < this.cols; c++) {
                    if (grid[primaryPiece.getY()][c] != '.' && grid[primaryPiece.getY()][c] != primaryPiece.getId()) {
                        blockingPieces.add(grid[primaryPiece.getY()][c]);
                    }
                }
            }
        } else { // Primary Piece VERTIKAL
            if (this.exitY == 0) { // Pintu keluar di atas
                for (int r = primaryPiece.getY() - 1; r >= 0; r--) {
                    if (grid[r][primaryPiece.getX()] != '.' && grid[r][primaryPiece.getX()] != primaryPiece.getId()) {
                        blockingPieces.add(grid[r][primaryPiece.getX()]);
                    }
                }
            } else { // Pintu keluar di bawah (this.exitY == this.rows - 1)
                for (int r = primaryPiece.getY() + primaryPiece.getLength(); r < this.rows; r++) {
                    if (grid[r][primaryPiece.getX()] != '.' && grid[r][primaryPiece.getX()] != primaryPiece.getId()) {
                        blockingPieces.add(grid[r][primaryPiece.getX()]);
                    }
                }
            }
        }
        int hValue = blockingPieces.size();
        if (hValue == 0 && !this.isGoalState()) {
            hValue = 1;
        }
        return hValue;
    }

    public int calculateManhattanDistanceHeuristic() {
        Piece primaryPiece = this.getPieceById('P');

        if (primaryPiece == null) {
            System.err.println("Error di Manhattan Distance heuristic: Primary piece 'P' tidak ditemukan!");
            return Integer.MAX_VALUE; 
        }

        if (this.isGoalState()) {
        }

        int distance = 0;

        if (primaryPiece.getOrientation() == Orientation.HORIZONTAL) {
            if (this.exitX > (primaryPiece.getX() + primaryPiece.getLength() - 1)) {
                distance = this.exitX - (primaryPiece.getX() + primaryPiece.getLength() - 1);
            }
            else if (this.exitX < primaryPiece.getX()) {
                distance = primaryPiece.getX() - this.exitX;
            }
            else if (this.exitX == 0) { // Pintu keluar di paling kiri
                distance = primaryPiece.getX(); 
            } else if (this.exitX == this.cols - 1) { // Pintu keluar di paling kanan
                distance = this.exitX - (primaryPiece.getX() + primaryPiece.getLength() - 1);
            } else {
                if(this.exitX >= primaryPiece.getX() + primaryPiece.getLength() - 1){
                    distance = this.exitX - (primaryPiece.getX() + primaryPiece.getLength() - 1);
                }
                else if (this.exitX <= primaryPiece.getX()){
                    distance = primaryPiece.getX() - this.exitX;
                } else {
                    distance = 0;
                }
            }

        } else { // Primary piece is VERTICAL
            if (this.exitY > (primaryPiece.getY() + primaryPiece.getLength() - 1)) {
                distance = this.exitY - (primaryPiece.getY() + primaryPiece.getLength() - 1);
            }
            else if (this.exitY < primaryPiece.getY()) {
                distance = primaryPiece.getY() - this.exitY;
            }
            else if (this.exitY == 0) { // Pintu keluar di paling atas
                distance = primaryPiece.getY(); 
            } else if (this.exitY == this.rows - 1) { // Pintu keluar di paling bawah
                distance = this.exitY - (primaryPiece.getY() + primaryPiece.getLength() - 1);
            } else {
                if(this.exitY >= primaryPiece.getY() + primaryPiece.getLength() - 1){
                    distance = this.exitY - (primaryPiece.getY() + primaryPiece.getLength() - 1);
                }
                else if (this.exitY <= primaryPiece.getY()){
                    distance = primaryPiece.getY() - this.exitY;
                } else {
                    distance = 0;
                }
            }
        }
        return Math.max(0, distance);
    }
}