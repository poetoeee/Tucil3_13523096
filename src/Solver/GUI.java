package Solver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI {
    private BoardPanel boardPanel;
    private JFrame frame;
    private List<Board> boardHistory = new ArrayList<>();
    private int currentBoardIndex = -1;
    private JButton nextButton;
    private JButton prevButton;
    private JButton solveButton;
    private JComboBox<String> algorithmChooser;
    private JComboBox<String> heuristicChooser;
    private JLabel nodesVisitedLabel;
    private JLabel executionTimeLabel;
    private JLabel solutionStepsLabel;
    private Board initialBoardForSolving;

    public GUI() {
        createAndShow();
    }

    private void createAndShow() {
        frame = new JFrame("13523096 Solver"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardPanel = new BoardPanel();
        
        // Panel Kontrol Atas
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loadFileButton = new JButton("Load Test File");
        loadFileButton.addActionListener(e -> loadNewTestFile()); 

        String[] algorithms = {"UCS", "Greedy BFS", "A*", "IDS"};
        algorithmChooser = new JComboBox<>(algorithms);
        
        topControlPanel.add(loadFileButton);
        topControlPanel.add(algorithmChooser);
        
        // topControlPanel.add(new JLabel("Heuristic (GBFS/A*):"));
        String[] heuristics = {"Blocking Pieces","Manhattan Distance"};
        heuristicChooser = new JComboBox<>(heuristics);
        topControlPanel.add(heuristicChooser);

        algorithmChooser.addActionListener(e -> {
            String selectedAlgorithm = (String) algorithmChooser.getSelectedItem();
            if ("UCS".equals(selectedAlgorithm) || "IDS".equals(selectedAlgorithm)) {
                heuristicChooser.setEnabled(false);
            } else {
                heuristicChooser.setEnabled(true);
            }
        });

        String initialSelectedAlgorithm = (String) algorithmChooser.getSelectedItem();
        if ("UCS".equals(initialSelectedAlgorithm) || "IDS".equals(initialSelectedAlgorithm)) {
            heuristicChooser.setEnabled(false);
        } else {
            heuristicChooser.setEnabled(true);
        }
        
        solveButton = new JButton("Solve!");
        solveButton.addActionListener(e -> solveCurrentBoard()); 
        solveButton.setEnabled(initialBoardForSolving != null);
        topControlPanel.add(solveButton);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevButton = new JButton("< Prev Step");
        nextButton = new JButton("Next Step >");
        prevButton.addActionListener(e -> showPreviousBoard()); 
        nextButton.addActionListener(e -> showNextBoard());   
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);

        // Panel Statistik Paling Bawah
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nodesVisitedLabel = new JLabel("Nodes Visited: -");
        executionTimeLabel = new JLabel("Time (ms): -");
        solutionStepsLabel = new JLabel("Solution Steps: -");

        statsPanel.add(nodesVisitedLabel);
        statsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        statsPanel.add(executionTimeLabel);
        statsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        statsPanel.add(solutionStepsLabel);

        JPanel bottomContainerPanel = new JPanel(new BorderLayout());
        bottomContainerPanel.add(navigationPanel, BorderLayout.NORTH);
        bottomContainerPanel.add(statsPanel, BorderLayout.SOUTH);

        frame.getContentPane().setLayout(new BorderLayout(5, 5));
        frame.getContentPane().add(topControlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        frame.getContentPane().add(bottomContainerPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setSize(Math.max(frame.getWidth(), 750), Math.max(frame.getHeight(), 750)); // Sesuaikan ukuran
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updateButtonStates(); 
        displayInitialMessage(); 
    }

    private void displayInitialMessage() {
        if (boardPanel != null && initialBoardForSolving == null) {
            boardPanel.setBoard(null); 
        }
    }

    private void loadNewTestFile() {
        JFileChooser fileChooser = new JFileChooser();
        File testDir = new File("../test"); 
        if (!testDir.exists() || !testDir.isDirectory()) {
            testDir = new File("test");
        }
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
            try {
                initialBoardForSolving = FileParser.parseFile(filePathToTest);
                initialBoardForSolving.printBoard();

                boardHistory.clear();
                boardHistory.add(new Board(initialBoardForSolving));
                currentBoardIndex = 0;
                displayBoardFromHistory(currentBoardIndex); 
                // updateButtonStates(); 
                solveButton.setEnabled(true); 

                SwingUtilities.invokeLater(() -> {
                    nodesVisitedLabel.setText("Nodes Visited: -");
                    executionTimeLabel.setText("Time (ms): -");
                    solutionStepsLabel.setText("Solution Steps: -");
                });

            } catch (IOException ex) {
                System.err.println("Gagal memproses file: " + filePathToTest + " - " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, "Gagal memuat file board: " + filePathToTest + "\nError: " + ex.getMessage(), "Error Parsing File", JOptionPane.ERROR_MESSAGE);
                solveButton.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    nodesVisitedLabel.setText("Nodes Visited: -");
                    executionTimeLabel.setText("Time (ms): -");
                    solutionStepsLabel.setText("Solution Steps: -");
                });
            }
        }
    }

    private void solveCurrentBoard() {
        if (initialBoardForSolving == null) {
            JOptionPane.showMessageDialog(frame, "No board loaded to solve.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String selectedAlgorithm = (String) algorithmChooser.getSelectedItem(); 
        String selectedHeuristic = (String) heuristicChooser.getSelectedItem();
        if (selectedAlgorithm == null) {
            JOptionPane.showMessageDialog(frame, "Please select an algorithm.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedHeuristic == null && (selectedAlgorithm.equals("Greedy BFS") || selectedAlgorithm.equals("A*"))) {
            JOptionPane.showMessageDialog(frame, "Please select a heuristic for " + selectedAlgorithm + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            nodesVisitedLabel.setText("Nodes Visited: Solving...");
            executionTimeLabel.setText("Time (ms): Solving...");
            solutionStepsLabel.setText("Solution Steps: Solving...");
        });

        new Thread(() -> {
            RushHourSolver solver = new RushHourSolver();
            RushHourSolver.Solution solution = null;
            Board boardToSolve = new Board(initialBoardForSolving);

            if ("UCS".equals(selectedAlgorithm)) {
                solution = solver.solveWithUCS(boardToSolve);
            } else if ("Greedy BFS".equals(selectedAlgorithm)) {
                solution = solver.solveWithGreedyBFS(boardToSolve, selectedHeuristic);
            } else if ("A*".equals(selectedAlgorithm)) {
                solution = solver.solveWithAStar(boardToSolve, selectedHeuristic);
            } else if ("IDS".equals(selectedAlgorithm)) {
                solution = solver.solveWithIDS(boardToSolve);
            } else {
                final String errorMsg = "Algoritma tidak dikenal: " + selectedAlgorithm;
                System.err.println(errorMsg);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, errorMsg, "Error", JOptionPane.ERROR_MESSAGE));
                return;
            }

            System.out.println("\n======= Hasil " + selectedAlgorithm + " =======");
            if (solution == null) {
                System.err.println("Objek solusi null, terjadi kesalahan pada solver.");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Terjadi kesalahan internal pada solver.", "Error", JOptionPane.ERROR_MESSAGE);
                    nodesVisitedLabel.setText("Nodes Visited: Error");
                    executionTimeLabel.setText("Time (ms): Error");
                    solutionStepsLabel.setText("Solution Steps: Error");
                });
                return;
            }

            final RushHourSolver.Solution finalSolution = solution;
            SwingUtilities.invokeLater(() -> {
                nodesVisitedLabel.setText("Nodes Visited: " + finalSolution.nodesVisited);
                executionTimeLabel.setText("Time (ms): " + finalSolution.executionTimeMillis);
                if (finalSolution.EsolusiDitemukan) {
                    solutionStepsLabel.setText("Solution Steps: " + finalSolution.moves.size());
                } else {
                    solutionStepsLabel.setText("Solution Steps: N/A");
                }
            });

            System.out.println("Solusi Ditemukan: " + finalSolution.EsolusiDitemukan);

            if (finalSolution.EsolusiDitemukan) {
                final String solutionMsg = "Solusi berhasil ditemukan dengan " + selectedAlgorithm + ".";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, solutionMsg, "Solusi Ditemukan", JOptionPane.INFORMATION_MESSAGE));
                boardHistory.clear();
                if (finalSolution.path != null && !finalSolution.path.isEmpty()) {
                    for (Board boardStateFromPath : finalSolution.path) {
                        boardHistory.add(new Board(boardStateFromPath));
                    }
                    currentBoardIndex = -1;
                    if (!boardHistory.isEmpty()) {
                        displayBoardFromHistory(0);
                    }
                } else {
                    boardHistory.add(new Board(initialBoardForSolving));
                    currentBoardIndex = 0;
                    displayBoardFromHistory(0);
                }
            } else {
                final String noSolutionMsg = "Tidak ada solusi yang ditemukan oleh " + selectedAlgorithm + ".";
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, noSolutionMsg, "Solusi Tidak Ditemukan", JOptionPane.INFORMATION_MESSAGE));
                boardHistory.clear();
                boardHistory.add(new Board(initialBoardForSolving));
                currentBoardIndex = 0;
                displayBoardFromHistory(0);
            }
            SwingUtilities.invokeLater(this::updateButtonStates); 
        }).start();
    }

    private void updateGUIdisplay(Board newBoard) { 
        if (boardPanel != null) {
            SwingUtilities.invokeLater(() -> boardPanel.setBoard(newBoard));
        } else {
            System.err.println("[GUI.updateGUIdisplay] Error: boardPanel belum diinisialisasi!");
        }
    }

    private void updateButtonStates() { 
        SwingUtilities.invokeLater(() -> {
            if (nextButton == null || prevButton == null) return;
            boolean historyAvailable = !boardHistory.isEmpty();
            prevButton.setEnabled(historyAvailable && currentBoardIndex > 0);
            nextButton.setEnabled(historyAvailable && currentBoardIndex < boardHistory.size() - 1);
        });
    }

    private void displayBoardFromHistory(int index) { 
        if (index >= 0 && index < boardHistory.size()) {
            currentBoardIndex = index;
            Board boardToShow = boardHistory.get(currentBoardIndex);
            updateGUIdisplay(boardToShow);
            updateButtonStates();
        }
    }

    private void showPreviousBoard() { 
        if (currentBoardIndex > 0) {
            displayBoardFromHistory(currentBoardIndex - 1);
        }
    }

    private void showNextBoard() { 
        if (currentBoardIndex < boardHistory.size() - 1) {
            displayBoardFromHistory(currentBoardIndex + 1);
        }
    }
}