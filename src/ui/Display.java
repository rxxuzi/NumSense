package ui;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel {

    // カラー
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(74, 101, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);

    // フォント
    private static final Font DIGIT_FONT = new Font("Arial", Font.BOLD, 72);
    private static final Font CONFIDENCE_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font ACCURACY_FONT = new Font("Arial", Font.BOLD, 14);

    // コンポーネント
    private JLabel digitLabel;
    private JLabel confidenceLabel;
    private ProbabilityBars probabilityBars;
    private JLabel accuracyLabel;

    // アニメーション用
    private Timer animationTimer;
    private int currentDigit = -1;
    private double currentConfidence = 0;
    private double targetConfidence = 0;

    public Display() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        initializeComponents();
        setupAnimationTimer();
    }

    private void initializeComponents() {
        // 上部：予測数字と信頼度
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        digitLabel = new JLabel("-");
        digitLabel.setFont(DIGIT_FONT);
        digitLabel.setForeground(TEXT_COLOR);
        digitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        confidenceLabel = new JLabel("Draw a digit");
        confidenceLabel.setFont(CONFIDENCE_FONT);
        confidenceLabel.setForeground(new Color(108, 117, 125));
        confidenceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(digitLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(confidenceLabel);

        add(topPanel, BorderLayout.NORTH);

        // 中央：確率バー
        probabilityBars = new ProbabilityBars();
        add(probabilityBars, BorderLayout.CENTER);

        // 下部：精度表示
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(LIGHT_GRAY);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        accuracyLabel = new JLabel("Model not trained");
        accuracyLabel.setFont(ACCURACY_FONT);
        accuracyLabel.setForeground(new Color(108, 117, 125));

        bottomPanel.add(accuracyLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(16, e -> {
            if (Math.abs(currentConfidence - targetConfidence) > 0.1) {
                currentConfidence += (targetConfidence - currentConfidence) * 0.15;
                updateConfidenceDisplay();
            }
        });
        animationTimer.start();
    }

    public void updatePrediction(int digit, double[] probabilities) {
        currentDigit = digit;
        targetConfidence = probabilities[digit] * 100;

        // 数字を更新（アニメーション付き）
        digitLabel.setText(String.valueOf(digit));

        // 色を信頼度に応じて変更
        Color digitColor;
        if (targetConfidence >= 90) {
            digitColor = SUCCESS_COLOR;
        } else if (targetConfidence >= 70) {
            digitColor = WARNING_COLOR;
        } else {
            digitColor = DANGER_COLOR;
        }

        // フェードインアニメーション
        SwingUtilities.invokeLater(() -> {
            digitLabel.setForeground(digitColor);
            probabilityBars.updateProbabilities(probabilities);
        });
    }

    private void updateConfidenceDisplay() {
        confidenceLabel.setText(String.format("Confidence: %.1f%%", currentConfidence));
    }

    public void updateAccuracy(double accuracy) {
        String text = String.format("Model Accuracy: %.1f%%", accuracy * 100);
        accuracyLabel.setText(text);

        // 精度に応じて色を変更
        if (accuracy >= 0.9) {
            accuracyLabel.setForeground(SUCCESS_COLOR);
        } else if (accuracy >= 0.7) {
            accuracyLabel.setForeground(WARNING_COLOR);
        } else {
            accuracyLabel.setForeground(DANGER_COLOR);
        }
    }

    public void clear() {
        currentDigit = -1;
        currentConfidence = 0;
        targetConfidence = 0;
        digitLabel.setText("-");
        digitLabel.setForeground(TEXT_COLOR);
        confidenceLabel.setText("Draw a digit");
        probabilityBars.clear();
    }

    /**
     * 確率バーの表示
     */
    private class ProbabilityBars extends JPanel {
        private double[] probabilities = new double[10];
        private double[] displayProbabilities = new double[10];
        private Timer updateTimer;

        public ProbabilityBars() {
            setBackground(BACKGROUND_COLOR);
            setPreferredSize(new Dimension(300, 200));

            // スムーズなアニメーション用タイマー
            updateTimer = new Timer(16, e -> {
                boolean needsRepaint = false;
                for (int i = 0; i < 10; i++) {
                    if (Math.abs(displayProbabilities[i] - probabilities[i]) > 0.001) {
                        displayProbabilities[i] += (probabilities[i] - displayProbabilities[i]) * 0.15;
                        needsRepaint = true;
                    }
                }
                if (needsRepaint) {
                    repaint();
                }
            });
            updateTimer.start();
        }

        public void updateProbabilities(double[] probs) {
            this.probabilities = probs.clone();
        }

        public void clear() {
            probabilities = new double[10];
            displayProbabilities = new double[10];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 30;
            int barSpacing = 5;
            int totalBarWidth = width - 2 * margin;
            int barWidth = (totalBarWidth - 9 * barSpacing) / 10;
            int maxBarHeight = height - 2 * margin - 20;

            // 各数字のバーを描画
            for (int i = 0; i < 10; i++) {
                int x = margin + i * (barWidth + barSpacing);
                int barHeight = (int)(displayProbabilities[i] * maxBarHeight);
                int y = height - margin - 20 - barHeight;

                // バーの色（予測数字は強調）
                if (i == currentDigit) {
                    g2.setColor(PRIMARY_COLOR);
                } else {
                    g2.setColor(new Color(226, 232, 240));
                }

                // 角丸の四角形
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                // 数字ラベル
                g2.setColor(TEXT_COLOR);
                g2.setFont(LABEL_FONT);
                String label = String.valueOf(i);
                FontMetrics fm = g2.getFontMetrics();
                int labelX = x + (barWidth - fm.stringWidth(label)) / 2;
                g2.drawString(label, labelX, height - margin + 2);

                // 確率値（5%以上のみ表示）
                if (displayProbabilities[i] > 0.05) {
                    String percent = String.format("%.0f%%", displayProbabilities[i] * 100);
                    int percentX = x + (barWidth - fm.stringWidth(percent)) / 2;
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString(percent, percentX, y - 5);
                }
            }
        }
    }
}