package Solver;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane; 
import java.awt.BorderLayout; 
// import java.awt.FlowLayout; 
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch; 
import java.io.IOException;

public class Main {

    private static BoardPanel boardPanel; 
    private static JFrame frame;
    private static final CountDownLatch guiReadyLatch = new CountDownLatch(1); 

    private static List<Board> boardHistory = new ArrayList<>();
    private static int currentBoardIndex = -1; 

    private static JButton nextButton;
    private static JButton prevButton;

    private static void createAndShowGUI(Board boardForInitialSetup) {
        frame = new JFrame("Rush Hour Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardPanel = new BoardPanel();
        if (boardForInitialSetup != null) {
            boardPanel.setBoard(boardForInitialSetup);
        }

        nextButton = new JButton("Next >");
        prevButton = new JButton("< Prev");

        prevButton.addActionListener(e -> showPreviousBoard());
        nextButton.addActionListener(e -> showNextBoard());

        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER); 
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);  

        frame.pack(); 
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);

        System.out.println("[Main.createAndShowGUI] GUI Frame dibuat dan ditampilkan.");
    }

    public static void updateGUIdisplay(Board newBoard) {
        if (boardPanel != null) {
            SwingUtilities.invokeLater(() -> {
                // System.out.println("[Main.updateGUIdisplay] Akan mengupdate GUI dengan board hash: " + (newBoard != null ? newBoard.hashCode() : "null"));
                // if (newBoard != null && newBoard.getPieceById('P') != null) {
                //     System.out.println("[Main.updateGUIdisplay] Piece P di newBoard: " + newBoard.getPieceById('P'));
                // }
                boardPanel.setBoard(newBoard);
            });
        } else {
            System.err.println("[Main.updateGUIdisplay] Error: boardPanel belum diinisialisasi!");
        }
    }

    private static void updateButtonStates() {
        SwingUtilities.invokeLater(() -> { // Pastikan update GUI di EDT
            if (boardHistory.isEmpty() || nextButton == null || prevButton == null) {
                if(nextButton != null) nextButton.setEnabled(false);
                if(prevButton != null) prevButton.setEnabled(false);
                return;
            }
            prevButton.setEnabled(currentBoardIndex > 0);
            nextButton.setEnabled(currentBoardIndex < boardHistory.size() - 1);
        });
    }

    private static void displayBoardFromHistory(int index) {
        if (index >= 0 && index < boardHistory.size()) {
            currentBoardIndex = index;
            Board boardToShow = boardHistory.get(currentBoardIndex);

            System.out.println("[Main.displayBoardFromHistory] Menampilkan board index: " + currentBoardIndex +
                               ", Hash: " + boardToShow.hashCode());
            if (boardToShow.getPieceById('P') != null) { 
                System.out.println("[Main.displayBoardFromHistory] Detail Piece P: " + boardToShow.getPieceById('P'));
            } else {
                System.out.println("[Main.displayBoardFromHistory] Piece P tidak ditemukan di board ini.");
            }

            updateGUIdisplay(boardToShow); 
            updateButtonStates(); 
        } else {
            System.err.println("[Main.displayBoardFromHistory] Indeks di luar jangkauan: " + index + ", ukuran histori: " + boardHistory.size());
        }
    }

    private static void showPreviousBoard() {
        System.out.println("[Main.showPreviousBoard] Tombol Prev diklik.");
        if (currentBoardIndex > 0) {
            displayBoardFromHistory(currentBoardIndex - 1);
        }
    }

    private static void showNextBoard() {
        System.out.println("[Main.showNextBoard] Tombol Next diklik.");
        if (currentBoardIndex < boardHistory.size() - 1) {
            displayBoardFromHistory(currentBoardIndex + 1);
        }
    }

    private static void addBoardToHistoryAndDisplay(Board board) {
        boardHistory.add(new Board(board)); 
        displayBoardFromHistory(boardHistory.size() - 1);
    }

    public static void main(String[] args) {
        System.out.println("Selamat Datang di Solver Rush Hour!");

        String[] testFilePaths = {
            "../test/sample_k_kanan.txt",
            "../test/sample_k_kiri.txt",
            "../test/sample_k_atas.txt",
            "../test/sample_k_bawah.txt",
            "../test/sample_no_solution.txt"
        };
        String filePathToTest = testFilePaths[0]; 

        System.out.println("\n======= Memproses File: " + filePathToTest + " =======");

        Board initialBoardFromFile;
        try {
            initialBoardFromFile = FileParser.parseFile(filePathToTest);
        } catch (IOException e) {
            System.err.println("Gagal memproses file: " + filePathToTest + " - " + e.getMessage());
            e.printStackTrace();
            final String errorMessage = "Gagal memuat file board: " + filePathToTest + "\nError: " + e.getMessage();
            JOptionPane.showMessageDialog(null, errorMessage, "Error Parsing File", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Board Awal berhasil diparsing dari file:");
        initialBoardFromFile.printBoard(); 

        SwingUtilities.invokeLater(() -> {
            createAndShowGUI(new Board(initialBoardFromFile)); 
            guiReadyLatch.countDown(); 
        });

        try {
            guiReadyLatch.await(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread diinterupsi saat menunggu GUI siap.");
            return;
        }

        boardHistory.clear();
        boardHistory.add(new Board(initialBoardFromFile)); 
        currentBoardIndex = 0;
        displayBoardFromHistory(currentBoardIndex);
        updateButtonStates();


        System.out.println("\n======= Memulai Pencarian Solusi dengan UCS =======");
        RushHourSolver solver = new RushHourSolver();
        RushHourSolver.Solution ucsSolution = solver.solveWithUCS(initialBoardFromFile); 

        System.out.println("\n======= Hasil UCS =======");
        System.out.println("Solusi Ditemukan: " + ucsSolution.EsolusiDitemukan);
        System.out.println("Waktu Eksekusi (ms): " + ucsSolution.executionTimeMillis);
        System.out.println("Node Dikunjungi (diambil dari antrian): " + ucsSolution.nodesVisited);

        if (ucsSolution.EsolusiDitemukan) {
            if (ucsSolution.moves != null) {
                System.out.println("Jumlah Langkah Solusi: " + ucsSolution.moves.size());
                System.out.println("Langkah-langkah (Gerakan):");
                int stepNum = 1;
                for (Move move : ucsSolution.moves) {
                    System.out.println("  Gerakan " + stepNum++ + ": Piece '" + move.getPieceId() + "' ke " + move.getDirection() + " sebanyak " + move.getSteps() + " unit.");
                }
            } else {
                System.out.println("Objek solusi tidak memiliki daftar gerakan (moves).");
            }

            boardHistory.clear(); 
            if (ucsSolution.path != null && !ucsSolution.path.isEmpty()) {
                System.out.println("Jumlah State dalam Path Solusi: " + ucsSolution.path.size());
                for (Board boardStateFromPath : ucsSolution.path) {
                    boardHistory.add(new Board(boardStateFromPath)); 
                }
                currentBoardIndex = -1; 
                if (!boardHistory.isEmpty()) {
                    System.out.println("Menampilkan state awal dari solusi di GUI...");
                    displayBoardFromHistory(0);
                }
            } else {
                 System.out.println("Path board tidak tersedia di objek solusi untuk ditampilkan di GUI.");
            }
        } else {
            System.out.println("Tidak ada solusi yang ditemukan oleh UCS.");
        }
        updateButtonStates(); 

        System.out.println("\n=======================================");
        if (ucsSolution.EsolusiDitemukan) {
            System.out.println("Pencarian UCS selesai. Gunakan tombol Next/Prev di GUI untuk navigasi solusi.");
        } else {
            System.out.println("Pencarian UCS selesai. Tidak ada solusi untuk file: " + filePathToTest);
        }
        System.out.println("Total state dalam histori GUI saat ini: " + boardHistory.size());
    }
}