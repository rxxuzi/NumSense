package main;

import ui.GUI;
import javax.swing.*;

public class Main {

    private final GUI gui;

    public Main() {
        this.gui = new GUI();
    }

    public void launch() {
        // システムのルックアンドフィールを設定
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new Main().launch();
    }
}