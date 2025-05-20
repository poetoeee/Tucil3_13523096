package Solver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import javax.swing.JPanel; 

public class BoardPanel extends JPanel {
    private Board currentBoard;
    private int dynamicCellSize = 50; 
    private final int DEFAULT_CELL_SIZE = 70;
    private final int PADDING = 1; 

    private final int MIN_BOARD_DIM_TARGET = 350;
    private final int MAX_BOARD_DIM_TARGET = 500;

    private Font messageFont = new Font("Arial", Font.BOLD, 15);

    public BoardPanel() {}

    public void setBoard(Board board) {
        this.currentBoard = board;
        calculateAndSetDynamicCellSize(); 
        this.revalidate(); 
        this.repaint();
    }

    private void calculateAndSetDynamicCellSize() {
        if (currentBoard == null) {
            this.dynamicCellSize = DEFAULT_CELL_SIZE;
            return;
        }

        int numCols = currentBoard.getCols();
        int numRows = currentBoard.getRows();

        if (numCols <= 0 || numRows <= 0) {
            this.dynamicCellSize = DEFAULT_CELL_SIZE; 
            return;
        }
        int tempCellSize = DEFAULT_CELL_SIZE;
        int boardWidthWithDefaultCellSize = numCols * tempCellSize;
        int boardHeightWithDefaultCellSize = numRows * tempCellSize;

        if (boardWidthWithDefaultCellSize > MAX_BOARD_DIM_TARGET || boardHeightWithDefaultCellSize > MAX_BOARD_DIM_TARGET) {
            int csForMaxWidth = MAX_BOARD_DIM_TARGET / numCols;
            int csForMaxHeight = MAX_BOARD_DIM_TARGET / numRows;
            tempCellSize = Math.min(csForMaxWidth, csForMaxHeight);
        }

        int boardWidthAfterMaxAdjust = numCols * tempCellSize;
        int boardHeightAfterMaxAdjust = numRows * tempCellSize;

        if (boardWidthAfterMaxAdjust < MIN_BOARD_DIM_TARGET && boardHeightAfterMaxAdjust < MIN_BOARD_DIM_TARGET) {
            int csToMakeWidthMin = MIN_BOARD_DIM_TARGET / numCols;
            int csToMakeHeightMin = MIN_BOARD_DIM_TARGET / numRows;

            int potentialNewCs = tempCellSize; 

            if (numCols >= numRows) { 
                if (numRows * csToMakeWidthMin <= MAX_BOARD_DIM_TARGET) { 
                    potentialNewCs = csToMakeWidthMin;
                }
            } else { 
                if (numCols * csToMakeHeightMin <= MAX_BOARD_DIM_TARGET) { 
                    potentialNewCs = csToMakeHeightMin;
                }
            }
            if (potentialNewCs > tempCellSize) {
                tempCellSize = potentialNewCs;
            }
        }
        if (tempCellSize <= 0) {
            tempCellSize = 1; // Ukuran minimal absolut
        }
        this.dynamicCellSize = tempCellSize;
    }


    @Override
    public Dimension getPreferredSize() {
        if (currentBoard != null) {
            int boardPixelWidth = currentBoard.getCols() * dynamicCellSize;
            int boardPixelHeight = currentBoard.getRows() * dynamicCellSize;
            return new Dimension(boardPixelWidth + 2 * PADDING, boardPixelHeight + 2 * PADDING);
        }
        return new Dimension(MIN_BOARD_DIM_TARGET + 2 * PADDING, MIN_BOARD_DIM_TARGET + 2 * PADDING);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (currentBoard == null || dynamicCellSize <= 0) {
            String message = "Please load a test file to start.";

            g2d.setFont(messageFont); 
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(message);
            int stringHeight = fm.getAscent() - fm.getDescent(); 

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int x = (panelWidth - stringWidth) / 2;
            int y = (panelHeight - stringHeight) / 2 + fm.getAscent(); 

            g2d.setColor(Color.DARK_GRAY); 
            g2d.drawString(message, x, y);
            return; 
        }

        int numCols = currentBoard.getCols();
        int numRows = currentBoard.getRows();
        int boardDisplayWidth = numCols * dynamicCellSize;
        int boardDisplayHeight = numRows * dynamicCellSize;

        int availableWidthForBoard = getWidth() - 2 * PADDING;
        int availableHeightForBoard = getHeight() - 2 * PADDING;

        int offsetX = PADDING + Math.max(0, (availableWidthForBoard - boardDisplayWidth) / 2);
        int offsetY = PADDING + Math.max(0, (availableHeightForBoard - boardDisplayHeight) / 2);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(offsetX, offsetY, boardDisplayWidth, boardDisplayHeight);

        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= numRows; i++) { // Garis horizontal
            g2d.drawLine(offsetX, offsetY + i * dynamicCellSize, offsetX + boardDisplayWidth, offsetY + i * dynamicCellSize);
        }
        for (int i = 0; i <= numCols; i++) { // Garis vertikal
            g2d.drawLine(offsetX + i * dynamicCellSize, offsetY, offsetX + i * dynamicCellSize, offsetY + boardDisplayHeight);
        }

        Map<Character, Piece> pieces = currentBoard.getAllPieces();
        for (Piece piece : pieces.values()) {
            drawPiece(g2d, piece, offsetX, offsetY);
        }

        Piece primaryPiece = currentBoard.getPieceById('P');
        if (primaryPiece != null) {
             drawExitMarker(g2d, primaryPiece, offsetX, offsetY, boardDisplayWidth, boardDisplayHeight);
        }
    }

    private void drawPiece(Graphics2D g2d, Piece piece, int boardOffsetX, int boardOffsetY) {
        int pieceX = boardOffsetX + piece.getX() * dynamicCellSize;
        int pieceY = boardOffsetY + piece.getY() * dynamicCellSize;
        int pieceWidth, pieceHeight;

        if (piece.getOrientation() == Orientation.HORIZONTAL) {
            pieceWidth = piece.getLength() * dynamicCellSize;
            pieceHeight = dynamicCellSize;
        } else { // VERTICAL
            pieceWidth = dynamicCellSize;
            pieceHeight = piece.getLength() * dynamicCellSize;
        }

        if (piece.isPrimary()) {
            g2d.setColor(new Color(84,119,146, 255));
        } else {
            g2d.setColor(new Color(33,52,72, 255)); 
        }
        g2d.fillRect(pieceX, pieceY, pieceWidth, pieceHeight);

        g2d.setColor(Color.WHITE); // Warna outline dan teks ID
        g2d.drawRect(pieceX, pieceY, pieceWidth, pieceHeight);

        String pieceIdStr = String.valueOf(piece.getId());
        g2d.setFont(new Font("Arial", Font.BOLD, dynamicCellSize / 4)); // Ukuran font dinamis
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(pieceIdStr);
        int stringAscent = fm.getAscent();

        // Pusatkan teks ID di dalam piece
        g2d.drawString(pieceIdStr,
                       pieceX + (pieceWidth - stringWidth) / 2,
                       pieceY + (pieceHeight - fm.getHeight()) / 2 + stringAscent);
    }

    private void drawExitMarker(Graphics2D g2dOriginal, Piece primaryPiece, int boardOffsetX, int boardOffsetY, int boardPixelWidth, int boardPixelHeight) {
        Graphics2D g2d = (Graphics2D) g2dOriginal.create(); 

        int rectWidth = 0, rectHeight = 0;
        int markerX = 0, markerY = 0;

        if (primaryPiece.getOrientation() == Orientation.VERTICAL) {
            rectWidth = dynamicCellSize; 
            rectHeight = 15;
            markerX = boardOffsetX + (currentBoard.getExitX() * dynamicCellSize);
            if (currentBoard.getExitY() == 0) { // Pintu keluar di ATAS
                markerY = boardOffsetY - rectHeight; 
            } else { // Pintu keluar di BAWAH 
                markerY = boardOffsetY + boardPixelHeight; 
            }
        } else { // Orientasi HORIZONTAL
            rectHeight = dynamicCellSize; 
            rectWidth = 15;
            markerY = boardOffsetY + (currentBoard.getExitY() * dynamicCellSize);
            if (currentBoard.getExitX() == 0) { // Pintu keluar di KIRI
                markerX = boardOffsetX - rectWidth; 
            } else { // Pintu keluar di KANAN 
                markerX = boardOffsetX + boardPixelWidth; 
            }
        }
        g2d.setColor(new Color(191, 146, 100, 255));
        g2d.fillRect(markerX, markerY, rectWidth, rectHeight);
        g2d.dispose();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        boolean sizeChanged = (getWidth() != width || getHeight() != height);
        super.setBounds(x, y, width, height);
        if (currentBoard != null && sizeChanged && width > 0 && height > 0) {
            calculateAndSetDynamicCellSize();
        }
    }
}