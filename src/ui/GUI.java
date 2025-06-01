package ui;

import main.CNN;
import data.MINIST;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * モダンでシンプルなCNN手書き数字認識GUI
 */
public class GUI extends JFrame {

    // カラーパレット
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(74, 101, 255);
    private static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);

    // フォント
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);

    // コンポーネント
    private Drawer drawingPanel;
    private Display predictionDisplay;
    private JButton clearButton;
    private JButton trainButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel epochLabel;
    private JLabel lossLabel;
    private JTextArea historyArea;

    // 学習設定
    private static final int epochs = 10;

    // コントローラー
    private TrainingController trainingController;
    private MINIST dataGenerator;

    public GUI() {
        super("Digit Recognition");
        initializeComponents();
        setupUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
    }

    private void initializeComponents() {
        trainingController = new TrainingController(epochs, 32, 0.001, true);
        trainingController.setListener(new TrainingController.TrainingListener() {
            @Override
            public void onStatusChanged(String status) {
                statusLabel.setText(status);
            }

            @Override
            public void onProgressChanged(int progress) {
                progressBar.setValue(progress);
            }

            @Override
            public void onEpochCompleted(int epoch, double loss) {
                // 統計タブに情報を更新
                epochLabel.setText("Epoch: " + epoch + " / " + epochs);
                lossLabel.setText(String.format("Loss: %.4f", loss));

                // 履歴に追加
                String history = String.format("Epoch %d: Loss = %.4f\n", epoch, loss);
                historyArea.append(history);
                historyArea.setCaretPosition(historyArea.getDocument().getLength());
            }

            @Override
            public void onAccuracyUpdated(double accuracy) {
                predictionDisplay.updateAccuracy(accuracy);
            }

            @Override
            public void onTrainingCompleted() {
                trainButton.setText("Train Model");
                progressBar.setValue(0);
                statusLabel.setText("Training completed!");
            }

            @Override
            public void onError(String error) {
                JOptionPane.showMessageDialog(GUI.this, error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dataGenerator = new MINIST();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // ヘッダー
        add(createHeader(), BorderLayout.NORTH);

        // メインコンテンツ
        add(createMainContent(), BorderLayout.CENTER);

        // フッター
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(getWidth(), 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Digit Recognition");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        trainButton = createStyledButton("Train Model", null);
        trainButton.setBackground(SUCCESS_COLOR);
        trainButton.addActionListener(e -> toggleTraining());
        header.add(trainButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 描画パネル（左側）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        content.add(createDrawingCard(), gbc);

        // 右側：タブパネル
        gbc.gridx = 1;
        gbc.gridy = 0;
        content.add(createTabbedPanel(), gbc);

        return content;
    }

    private JTabbedPane createTabbedPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(CARD_COLOR);
        tabbedPane.setFont(LABEL_FONT);

        // 予測タブ
        JPanel predictionTab = createPredictionCard();
        tabbedPane.addTab("Prediction", predictionTab);

        // 統計タブ
        JPanel statisticsTab = createStatisticsCard();
        tabbedPane.addTab("Statistics", statisticsTab);

        return tabbedPane;
    }

    private JPanel createDrawingCard() {
        JPanel card = createCard("Draw a Digit");
        card.setLayout(new BorderLayout());

        // 描画パネル
        drawingPanel = new Drawer();
        drawingPanel.addDrawingListener(() -> onDrawingChanged());

        JPanel drawingContainer = new JPanel(new GridBagLayout());
        drawingContainer.setBackground(CARD_COLOR);
        drawingContainer.add(drawingPanel);

        card.add(drawingContainer, BorderLayout.CENTER);

        // ボタン
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(CARD_COLOR);

        clearButton = createStyledButton("Clear", null);
        clearButton.setBackground(CARD_COLOR);
        clearButton.setForeground(DANGER_COLOR);
        clearButton.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 2));
        clearButton.addActionListener(e -> {
            drawingPanel.clear();
            predictionDisplay.clear();
        });

        JButton generateButton = createStyledButton("Random", null);
        generateButton.setBackground(CARD_COLOR);
        generateButton.setForeground(PRIMARY_COLOR);
        generateButton.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        generateButton.addActionListener(e -> generateSample());

        buttonPanel.add(clearButton);
        buttonPanel.add(generateButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPredictionCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setLayout(new BorderLayout());

        predictionDisplay = new Display();
        card.add(predictionDisplay, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatisticsCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setLayout(new BorderLayout());

        // 統計表示パネル
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(CARD_COLOR);

        // 学習状況
        JLabel trainingStatusLabel = new JLabel("Training Status");
        trainingStatusLabel.setFont(SUBTITLE_FONT);
        trainingStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainingStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statsPanel.add(trainingStatusLabel);

        // エポック情報
        epochLabel = new JLabel("Epoch: 0 / " + epochs);
        epochLabel.setFont(LABEL_FONT);
        epochLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(epochLabel);

        // 損失情報
        lossLabel = new JLabel("Loss: -");
        lossLabel.setFont(LABEL_FONT);
        lossLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(lossLabel);

        statsPanel.add(Box.createVerticalStrut(20));

        // 学習履歴
        JLabel historyLabel = new JLabel("Training History");
        historyLabel.setFont(SUBTITLE_FONT);
        historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        statsPanel.add(historyLabel);

        // 履歴表示エリア
        historyArea = new JTextArea(10, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBackground(LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(scrollPane);

        card.add(statsPanel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(15, CARD_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SUBTITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BACKGROUND_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        footer.setPreferredSize(new Dimension(getWidth(), 60));

        statusLabel = new JLabel("Ready to draw or train");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(SECONDARY_COLOR);
        footer.add(statusLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(300, 25));
        progressBar.setBackground(BACKGROUND_COLOR);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);
        footer.add(progressBar, BorderLayout.EAST);

        return footer;
    }

    private JButton createStyledButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // ホバーエフェクト
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
        });

        return button;
    }

    private void onDrawingChanged() {
        if (!drawingPanel.hasDrawing()) return;

        double[][] image = drawingPanel.getCurrentImage();
        double[][][] input = new double[1][CNN.IMAGE_SIZE][CNN.IMAGE_SIZE];
        input[0] = image;

        TrainingController.PredictionResult result = trainingController.predict(input);
        predictionDisplay.updatePrediction(result.predictedClass, result.probabilities);
    }

    private void generateSample() {
        int digit = (int)(Math.random() * 10);
        double[][] sample = dataGenerator.generateDigit(digit, 0.05);
        drawingPanel.setImage(sample);
        statusLabel.setText("Generated sample: " + digit);
    }

    private void toggleTraining() {
        if (trainingController.isTraining()) {
            trainingController.stopTraining();
            trainButton.setText("Train Model");
        } else {
            trainingController.startTraining();
            trainButton.setText("Stop Training");
        }
    }

    /**
     * 角丸ボーダー
     */
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }
}