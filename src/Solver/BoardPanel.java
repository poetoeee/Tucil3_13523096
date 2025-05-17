package Solver; 

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;

public class BoardPanel extends JPanel {
    private Board currentBoard;
    private final int CELL_SIZE = 50; 
    private final int PADDING = 50;   

    public BoardPanel() {}

    
    public void setBoard(Board board) {
        System.out.println("[BoardPanel.setBoard] Hash board LAMA: " + (this.currentBoard != null ? this.currentBoard.hashCode() + " | Pieces: " + this.currentBoard.getAllPieces().values() : "null")); // DEBUG
        this.currentBoard = board;
        System.out.println("[BoardPanel.setBoard] Hash board BARU: " + (this.currentBoard != null ? this.currentBoard.hashCode() + " | Pieces: " + this.currentBoard.getAllPieces().values() : "null")); // DEBUG
        if (this.currentBoard != null && this.currentBoard.getPieceById('P') != null) { // Cek piece P sebagai contoh
             System.out.println("[BoardPanel.setBoard] Piece P di board BARU: " + this.currentBoard.getPieceById('P'));
        }
        this.repaint(); 
        System.out.println("[BoardPanel.setBoard] repaint() dipanggil."); // DEBUG
    }

    @Override
    public Dimension getPreferredSize() {
        // Hitung ukuran yang diinginkan berdasarkan board (jika ada)
        if (currentBoard != null) {
            int width = currentBoard.getCols() * CELL_SIZE + 2 * PADDING;
            int height = currentBoard.getRows() * CELL_SIZE + 2 * PADDING;
            return new Dimension(width, height);
        }
        return new Dimension(400, 400); // Ukuran default jika board belum diset
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Penting untuk membersihkan background
        Graphics2D g2d = (Graphics2D) g;

        if (currentBoard == null) {
            g2d.drawString("Tidak ada papan untuk ditampilkan.", PADDING, PADDING + 15);
            return;
        }

        int boardPixelWidth = currentBoard.getCols() * CELL_SIZE;
        int boardPixelHeight = currentBoard.getRows() * CELL_SIZE;

        // Gambar latar belakang papan (opsional)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(PADDING, PADDING, boardPixelWidth, boardPixelHeight);

        // Gambar garis grid
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= currentBoard.getRows(); i++) { // Baris horizontal
            g2d.drawLine(PADDING, PADDING + i * CELL_SIZE, PADDING + boardPixelWidth, PADDING + i * CELL_SIZE);
        }
        for (int i = 0; i <= currentBoard.getCols(); i++) { // Kolom vertikal
            g2d.drawLine(PADDING + i * CELL_SIZE, PADDING, PADDING + i * CELL_SIZE, PADDING + boardPixelHeight);
        }

        // Gambar semua piece
        Map<Character, Piece> pieces = currentBoard.getAllPieces(); // Dapat salinan map pieces
        for (Piece piece : pieces.values()) {
            drawPiece(g2d, piece);
        }
        
        // Tandai Pintu KELUAR (di luar grid utama, bersebelahan dengan sel exitY, exitX)
        if (currentBoard != null) {
            Piece primaryPiece = currentBoard.getPieceById('P'); // Dapatkan primary piece untuk orientasi
            if (primaryPiece == null) return; // Jika tidak ada primary piece, jangan gambar exit

            Graphics2D g2dExit = (Graphics2D) g.create(); // Buat konteks grafis baru agar tidak mengganggu setting g2d utama
            
            // Atur warna dan font untuk EXIT
            Color exitBackgroundColor = new Color(0, 205, 0, 200); 
            
            int outerMargin = 0;   // Jarak marker dari papan

            int rectWidth = 0, rectHeight = 0;
            int markerX = 0, markerY = 0; // Posisi kiri atas marker
            // int textX = 0, textY = 0;    // Posisi baseline teks

            if (primaryPiece.getOrientation() == Orientation.VERTICAL) { // K di ATAS atau BAWAH
                rectWidth = 50; // Lebar marker sedikit lebih kecil dari sel
                rectHeight = 10; 
                markerX = PADDING + (currentBoard.getExitX() * CELL_SIZE);
                
                if (currentBoard.getExitY() == 0) { // K di ATAS
                    markerY = PADDING - rectHeight - outerMargin;
                } else { // K di BAWAH (exitY == currentBoard.getRows() - 1)
                    markerY = PADDING + currentBoard.getRows() * CELL_SIZE + outerMargin;
                }
                // textX = markerX + (rectWidth - strWidth) / 2;
                // textY = markerY + (rectHeight - strHeight) / 2 + strAscent; // Penyesuaian untuk baseline
            } else { // primaryPiece.getOrientation() == Orientation.HORIZONTAL, K di KIRI atau KANAN
                rectHeight = 50; // Tinggi marker sedikit lebih kecil dari sel
                rectWidth = 10; // Beri ruang lebih untuk teks horizontal 
                markerY = PADDING + (currentBoard.getExitY() * CELL_SIZE);

                if (currentBoard.getExitX() == 0) { // K di KIRI
                    markerX = PADDING - rectWidth - outerMargin;
                } else { // K di KANAN (exitX == currentBoard.getCols() - 1)
                    markerX = PADDING + currentBoard.getCols() * CELL_SIZE + outerMargin;
                }
                // textX = markerX + (rectWidth - strWidth) / 2;
                // textY = markerY + (rectHeight - strHeight) / 2 + strAscent; // Penyesuaian untuk baseline
            }

            g2dExit.setColor(exitBackgroundColor);
            g2dExit.fillRect(markerX, markerY, rectWidth, rectHeight);
            
            // g2dExit.setColor(exitTextColor);
            // g2dExit.drawString(exitStr, textX, textY);
            
            g2dExit.dispose();
        }
    }

    private void drawPiece(Graphics2D g2d, Piece piece) {
        int pieceX = PADDING + piece.getX() * CELL_SIZE;
        int pieceY = PADDING + piece.getY() * CELL_SIZE;
        int pieceWidth, pieceHeight;

        if (piece.getOrientation() == Orientation.HORIZONTAL) {
            pieceWidth = piece.getLength() * CELL_SIZE;
            pieceHeight = CELL_SIZE;
        } else { // VERTICAL
            pieceWidth = CELL_SIZE;
            pieceHeight = piece.getLength() * CELL_SIZE;
        }

        if (piece.isPrimary()) {
            g2d.setColor(Color.BLUE);
        } else {
            int r = 0; 
            int g = 0;
            int b = 102;
            g2d.setColor(new Color(r, g, b));
        }

        g2d.fillRect(pieceX, pieceY, pieceWidth, pieceHeight);

        // Gambar outline piece dan ID-nya
        g2d.setColor(Color.WHITE);
        g2d.drawRect(pieceX, pieceY, pieceWidth, pieceHeight);
        
        // Pusatkan ID piece
        String pieceIdStr = String.valueOf(piece.getId());
        int stringWidth = g2d.getFontMetrics().stringWidth(pieceIdStr);
        int stringHeight = g2d.getFontMetrics().getAscent() - g2d.getFontMetrics().getDescent(); // Perkiraan tinggi
        
        g2d.drawString(pieceIdStr, pieceX + (pieceWidth - stringWidth) / 2, pieceY + (pieceHeight + stringHeight) / 2);
    }
}