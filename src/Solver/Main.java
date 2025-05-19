package Solver;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Selamat Datang di Solver Rush Hour!");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GUI(); 
            }
        });
    }
}