package Solver;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // try {
        //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //     // for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        //     //     if ("Nimbus".equals(info.getName())) {
        //     //         UIManager.setLookAndFeel(info.getClassName());
        //     //         break;
        //     //     }
        //     // }
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        System.out.println("Selamat Datang di Solver Rush Hour!");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GUI(); 
            }
        });
    }
}