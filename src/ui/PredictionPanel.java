package ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 予測結果と統計を表示するパネル
 */
public class PredictionPanel extends JPanel {

    // 予測結果表示
    private JLabel predictedDigitLabel;
    private JLabel confidenceLabel;
    private ProbabilityBarChart probabilityChart;

    // 統計情報
    private JLabel accuracyLabel;
    private LossChart lossChart;
    private ConfusionMatrix confusionMatrix;

    // 履歴データ
    private List<Double> lossHistory;
    private int[][] confusionData;

    public PredictionPanel() {
        setLayout(new BorderLayout());
        initialize();
    }

    private void initialize() {
        // メインパネルを3つのセクションに分割
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 1. 予測結果セクション
        JPanel predictionSection = createPredictionSection();
        mainPanel.add(predictionSection);
        mainPanel.add(Box.createVerticalStrut(20));

        // 2. 確率分布セクション
        JPanel probabilitySection = createProbabilitySection();
        mainPanel.add(probabilitySection);
        mainPanel.add(Box.createVerticalStrut(20));

        // 3. 学習統計セクション
        JPanel statisticsSection = createStatisticsSection();
        mainPanel.add(statisticsSection);

        // スクロール可能にする
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // データの初期化
        lossHistory = new ArrayList<>();
        confusionData = new int[10][10];
    }

    private JPanel createPredictionSection() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Current Prediction"));
        panel.setLayout(new GridLayout(2, 1, 5, 5));

        predictedDigitLabel = new JLabel("Draw a digit", SwingConstants.CENTER);
        predictedDigitLabel.setFont(new Font("Arial", Font.BOLD, 48));
        panel.add(predictedDigitLabel);

        confidenceLabel = new JLabel("Confidence: -", SwingConstants.CENTER);
        confidenceLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        panel.add(confidenceLabel);

        return panel;
    }

    private JPanel createProbabilitySection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Probability Distribution"));

        probabilityChart = new ProbabilityBarChart();
        probabilityChart.setPreferredSize(new Dimension(400, 200));
        panel.add(probabilityChart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatisticsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Training Statistics"));

        // 精度表示
        accuracyLabel = new JLabel("Model Accuracy: Not evaluated", SwingConstants.CENTER);
        accuracyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(accuracyLabel, BorderLayout.NORTH);

        // 損失グラフ
        lossChart = new LossChart();
        lossChart.setPreferredSize(new Dimension(400, 150));
        panel.add(lossChart, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 予測結果を更新
     */
    public void updatePrediction(int predictedClass, double[] probabilities) {
        SwingUtilities.invokeLater(() -> {
            // 予測数字を更新
            predictedDigitLabel.setText(String.valueOf(predictedClass));

            // 信頼度を更新
            double confidence = probabilities[predictedClass] * 100;
            confidenceLabel.setText(String.format("Confidence: %.1f%%", confidence));

            // 信頼度に応じて色を変更
            if (confidence >= 90) {
                predictedDigitLabel.setForeground(new Color(0, 150, 0));  // 緑
            } else if (confidence >= 70) {
                predictedDigitLabel.setForeground(new Color(200, 150, 0));  // 黄
            } else {
                predictedDigitLabel.setForeground(new Color(200, 0, 0));  // 赤
            }

            // 確率分布を更新
            probabilityChart.updateProbabilities(probabilities);
        });
    }

    /**
     * 予測をクリア
     */
    public void clearPrediction() {
        SwingUtilities.invokeLater(() -> {
            predictedDigitLabel.setText("Draw a digit");
            predictedDigitLabel.setForeground(Color.BLACK);
            confidenceLabel.setText("Confidence: -");
            probabilityChart.clear();
        });
    }

    /**
     * 学習履歴を追加
     */
    public void addTrainingHistory(int epoch, double loss) {
        SwingUtilities.invokeLater(() -> {
            lossHistory.add(loss);
            lossChart.updateLoss(lossHistory);
        });
    }

    /**
     * 精度を更新
     */
    public void updateAccuracy(double accuracy) {
        SwingUtilities.invokeLater(() -> {
            accuracyLabel.setText(String.format("Model Accuracy: %.2f%%", accuracy * 100));
        });
    }

    /**
     * 確率分布のバーチャート
     */
    private static class ProbabilityBarChart extends JPanel {
        private double[] probabilities;
        private static final Color[] COLORS = {
                new Color(52, 152, 219),   // 青
                new Color(46, 204, 113),   // 緑
                new Color(155, 89, 182),   // 紫
                new Color(241, 196, 15),   // 黄
                new Color(230, 126, 34),   // オレンジ
                new Color(231, 76, 60),    // 赤
                new Color(149, 165, 166),  // グレー
                new Color(52, 73, 94),     // 濃紺
                new Color(26, 188, 156),   // ターコイズ
                new Color(192, 57, 43)     // 深紅
        };

        public ProbabilityBarChart() {
            setBackground(Color.WHITE);
            probabilities = new double[10];
        }

        public void updateProbabilities(double[] probs) {
            this.probabilities = probs.clone();
            repaint();
        }

        public void clear() {
            probabilities = new double[10];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 20;
            int barWidth = (width - 2 * margin) / 10;
            int maxBarHeight = height - 2 * margin - 20;

            // グリッド線を描画
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 10; i++) {
                int y = margin + (int)(maxBarHeight * (1 - i / 10.0));
                g2.drawLine(margin, y, width - margin, y);
            }

            // バーを描画
            for (int i = 0; i < 10; i++) {
                int x = margin + i * barWidth;
                int barHeight = (int)(probabilities[i] * maxBarHeight);
                int y = height - margin - 20 - barHeight;

                // バーの描画
                g2.setColor(COLORS[i]);
                g2.fillRect(x + 5, y, barWidth - 10, barHeight);

                // 数字ラベル
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String label = String.valueOf(i);
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (barWidth - labelWidth) / 2, height - margin);

                // 確率値
                if (probabilities[i] > 0.01) {
                    String prob = String.format("%.1f%%", probabilities[i] * 100);
                    int probWidth = g2.getFontMetrics().stringWidth(prob);
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString(prob, x + (barWidth - probWidth) / 2, y - 5);
                }
            }
        }
    }

    /**
     * 損失グラフ
     */
    private static class LossChart extends JPanel {
        private List<Double> losses;

        public LossChart() {
            setBackground(Color.WHITE);
            losses = new ArrayList<>();
        }

        public void updateLoss(List<Double> lossHistory) {
            this.losses = new ArrayList<>(lossHistory);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (losses.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 20;
            int graphWidth = width - 2 * margin;
            int graphHeight = height - 2 * margin;

            // 最大値と最小値を見つける
            double maxLoss = losses.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            double minLoss = losses.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

            // 軸を描画
            g2.setColor(Color.BLACK);
            g2.drawLine(margin, margin, margin, height - margin);
            g2.drawLine(margin, height - margin, width - margin, height - margin);

            // グリッド線
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 1; i < 5; i++) {
                int y = margin + (int)(graphHeight * i / 5.0);
                g2.drawLine(margin, y, width - margin, y);
            }

            // 損失曲線を描画
            g2.setColor(new Color(52, 152, 219));
            g2.setStroke(new BasicStroke(2));

            for (int i = 1; i < losses.size(); i++) {
                int x1 = margin + (int)((i - 1) * graphWidth / (double)(losses.size() - 1));
                int x2 = margin + (int)(i * graphWidth / (double)(losses.size() - 1));

                double normalized1 = (losses.get(i - 1) - minLoss) / (maxLoss - minLoss);
                double normalized2 = (losses.get(i) - minLoss) / (maxLoss - minLoss);

                int y1 = height - margin - (int)(normalized1 * graphHeight);
                int y2 = height - margin - (int)(normalized2 * graphHeight);

                g2.drawLine(x1, y1, x2, y2);
            }

            // ラベル
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("Epoch", width / 2 - 20, height - 5);
            g2.drawString("Loss", 5, margin - 5);

            // 最終損失値を表示
            if (!losses.isEmpty()) {
                double lastLoss = losses.get(losses.size() - 1);
                String lossStr = String.format("Final: %.4f", lastLoss);
                g2.drawString(lossStr, width - margin - 80, margin - 5);
            }
        }
    }

    /**
     * 混同行列（将来の拡張用）
     */
    private static class ConfusionMatrix extends JPanel {
        private int[][] matrix;

        public ConfusionMatrix() {
            matrix = new int[10][10];
            setPreferredSize(new Dimension(300, 300));
        }

        public void updateMatrix(int[][] data) {
            this.matrix = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // TODO: 混同行列の描画実装
        }
    }
}