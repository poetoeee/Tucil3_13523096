package Solver;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox; 
import java.awt.BorderLayout;
import java.awt.FlowLayout; 
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.io.IOException;
import java.io.File; 
import javax.swing.JFileChooser; 
import javax.swing.filechooser.FileNameExtensionFilter; 


public class Main {

    private static BoardPanel boardPanel;
    private static JFrame frame;
    private static final CountDownLatch guiReadyLatch = new CountDownLatch(1);

    private static List<Board> boardHistory = new ArrayList<>();
    private static int currentBoardIndex = -1;

    private static JButton nextButton;
    private static JButton prevButton;
    private static JButton solveButton; 
    private static JComboBox<String> algorithmChooser; 

    private static Board initialBoardForSolving; 

    private static void createAndShowGUI(Board boardForInitialDisplay) {
        frame = new JFrame("Rush Hour Solver - Stima Kelompok Anda"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardPanel = new BoardPanel();
        if (boardForInitialDisplay != null) {
            boardPanel.setBoard(boardForInitialDisplay);
            initialBoardForSolving = new Board(boardForInitialDisplay); 
        }

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadFileButton = new JButton("Load Test File");
        loadFileButton.addActionListener(e -> loadNewTestFile());

        String[] algorithms = {"UCS", "Greedy BFS"}; // Nanti bisa tambah A*
        algorithmChooser = new JComboBox<>(algorithms);

        solveButton = new JButton("Solve!");
        solveButton.addActionListener(e -> solveCurrentBoard());
        solveButton.setEnabled(initialBoardForSolving != null); 

        controlPanel.add(loadFileButton);
        controlPanel.add(new javax.swing.JLabel("Algorithm:"));
        controlPanel.add(algorithmChooser);
        controlPanel.add(solveButton);

        JPanel navigationPanel = new JPanel(); 
        prevButton = new JButton("< Prev Step");
        nextButton = new JButton("Next Step >");
        prevButton.addActionListener(e -> showPreviousBoard());
        nextButton.addActionListener(e -> showNextBoard());
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        frame.getContentPane().add(navigationPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);

        System.out.println("[Main.createAndShowGUI] GUI Frame dibuat dan ditampilkan.");
    }

    private static void loadNewTestFile() {
        JFileChooser fileChooser = new JFileChooser();
        File testDir = new File("../test"); 
        if (testDir.exists() && testDir.isDirectory()) {
            fileChooser.setCurrentDirectory(testDir);
        } else {
            fileChooser.setCurrentDirectory(new File(".")); 
        }
        fileChooser.setDialogTitle("Select Rush Hour Test File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePathToTest = selectedFile.getAbsolutePath();
            System.out.println("\n======= Memproses File Baru: " + filePathToTest + " =======");
            try {
                initialBoardForSolving = FileParser.parseFile(filePathToTest); // Simpan untuk solving
                System.out.println("Board Awal berhasil diparsing dari file:");
                initialBoardForSolving.printBoard();

                boardHistory.clear();
                boardHistory.add(new Board(initialBoardForSolving)); 
                currentBoardIndex = 0;
                displayBoardFromHistory(currentBoardIndex);
                updateButtonStates();
                if(solveButton != null) solveButton.setEnabled(true); 

            } catch (IOException ex) {
                System.err.println("Gagal memproses file: " + filePathToTest + " - " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Gagal memuat file board: " + filePathToTest + "\nError: " + ex.getMessage(), "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                if(solveButton != null) solveButton.setEnabled(false);
            }
        }
    }


    private static void solveCurrentBoard() {
        if (initialBoardForSolving == null) {
            JOptionPane.showMessageDialog(frame, "No board loaded to solve.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedAlgorithm = (String) algorithmChooser.getSelectedItem();
        if (selectedAlgorithm == null) {
            JOptionPane.showMessageDialog(frame, "Please select an algorithm.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("\n======= Memulai Pencarian Solusi dengan " + selectedAlgorithm + " =======");
        RushHourSolver solver = new RushHourSolver();
        RushHourSolver.Solution solution = null;

        Board boardToSolve = new Board(initialBoardForSolving);

        if ("UCS".equals(selectedAlgorithm)) {
            solution = solver.solveWithUCS(boardToSolve);
        } else if ("Greedy BFS".equals(selectedAlgorithm)) {
            solution = solver.solveWithGreedyBFS(boardToSolve);
        } else {
            System.err.println("Algoritma tidak dikenal: " + selectedAlgorithm);
            JOptionPane.showMessageDialog(frame, "Algoritma tidak dikenal: " + selectedAlgorithm, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("\n======= Hasil " + selectedAlgorithm + " =======");
        if (solution == null) {
            System.err.println("Objek solusi null, terjadi kesalahan pada solver.");
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan internal pada solver.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Solusi Ditemukan: " + solution.EsolusiDitemukan);
        System.out.println("Waktu Eksekusi (ms): " + solution.executionTimeMillis);
        System.out.println("Node Dikunjungi (diambil dari antrian): " + solution.nodesVisited);

        if (solution.EsolusiDitemukan) {
            if (solution.moves != null) {
                System.out.println("Jumlah Langkah Solusi: " + solution.moves.size());
                System.out.println("Langkah-langkah (Gerakan):");
                int stepNum = 1;
                for (Move move : solution.moves) {
                    System.out.println("  Gerakan " + stepNum++ + ": Piece '" + move.getPieceId() + "' ke " + move.getDirection() + " sebanyak " + move.getSteps() + " unit.");
                }
            } else {
                System.out.println("Objek solusi tidak memiliki daftar gerakan (moves).");
            }

            // Update GUI dengan path solusi
            boardHistory.clear();
            if (solution.path != null && !solution.path.isEmpty()) {
                System.out.println("Jumlah State dalam Path Solusi: " + solution.path.size());
                for (Board boardStateFromPath : solution.path) {
                    boardHistory.add(new Board(boardStateFromPath)); 
                }
                currentBoardIndex = -1;
                if (!boardHistory.isEmpty()) {
                    System.out.println("Menampilkan state awal dari solusi di GUI...");
                    displayBoardFromHistory(0);
                }
            } else {
                System.out.println("Path board tidak tersedia di objek solusi untuk ditampilkan di GUI.");
                boardHistory.add(new Board(initialBoardForSolving));
                currentBoardIndex = 0;
                displayBoardFromHistory(0);
            }
        } else {
            System.out.println("Tidak ada solusi yang ditemukan oleh " + selectedAlgorithm + ".");
            // Tampilkan board awal di GUI jika tidak ada solusi
            boardHistory.clear();
            boardHistory.add(new Board(initialBoardForSolving));
            currentBoardIndex = 0;
            displayBoardFromHistory(0);
            JOptionPane.showMessageDialog(frame, "Tidak ada solusi yang ditemukan oleh " + selectedAlgorithm + ".", "Solusi Tidak Ditemukan", JOptionPane.INFORMATION_MESSAGE);
        }
        updateButtonStates();

        System.out.println("\n=======================================");
        if (solution.EsolusiDitemukan) {
            System.out.println("Pencarian " + selectedAlgorithm + " selesai. Gunakan tombol Next/Prev di GUI untuk navigasi solusi.");
        } else {
            System.out.println("Pencarian " + selectedAlgorithm + " selesai. Tidak ada solusi.");
        }
        System.out.println("Total state dalam histori GUI saat ini: " + boardHistory.size());
    }


    public static void updateGUIdisplay(Board newBoard) {
        if (boardPanel != null) {
            SwingUtilities.invokeLater(() -> {
                boardPanel.setBoard(newBoard);
            });
        } else {
            System.err.println("[Main.updateGUIdisplay] Error: boardPanel belum diinisialisasi!");
        }
    }

    private static void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            if (nextButton == null || prevButton == null) return;
            boolean historyAvailable = !boardHistory.isEmpty();
            prevButton.setEnabled(historyAvailable && currentBoardIndex > 0);
            nextButton.setEnabled(historyAvailable && currentBoardIndex < boardHistory.size() - 1);
        });
    }

    private static void displayBoardFromHistory(int index) {
        if (index >= 0 && index < boardHistory.size()) {
            currentBoardIndex = index;
            Board boardToShow = boardHistory.get(currentBoardIndex);
            // System.out.println("[Main.displayBoardFromHistory] Menampilkan board index: " + currentBoardIndex); // Kurangi verbosity
            updateGUIdisplay(boardToShow);
            updateButtonStates();
        }
    }

    private static void showPreviousBoard() {
        if (currentBoardIndex > 0) {
            displayBoardFromHistory(currentBoardIndex - 1);
        }
    }

    private static void showNextBoard() {
        if (currentBoardIndex < boardHistory.size() - 1) {
            displayBoardFromHistory(currentBoardIndex + 1);
        }
    }
    public static void main(String[] args) {
        System.out.println("Selamat Datang di Solver Rush Hour!");
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI(null); 
            updateButtonStates(); 
        });
    }
}