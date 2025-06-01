package ui;

import main.CNN;
import data.MINIST;

import javax.swing.*;
import java.awt.*;

/**
 * リファクタリングされたCNN手書き数字認識GUI
 * コンポーネントを分離してモジュール化
 */
public class GUI extends JFrame implements TrainingController.TrainingListener {

    // UIコンポーネント
    private DrawingPanel drawingPanel;
    private PredictionPanel predictionPanel;
    private JButton clearButton;
    private JButton generateButton;
    private JButton trainButton;
    private JButton evaluateButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // コントローラーとデータ生成器
    private TrainingController trainingController;
    private MINIST dataGenerator;

    // 学習設定
    private static final int EPOCHS = 20;
    private static final int BATCH_SIZE = 32;
    private static final double INITIAL_LEARNING_RATE = 0.001;
    private static final boolean USE_DATA_AUGMENTATION = true;

    public GUI() {
        super("Jeeplearning - CNN Digit Recognition");
        initializeComponents();
        initializeUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    /**
     * コンポーネントの初期化
     */
    private void initializeComponents() {
        // 学習コントローラーの初期化
        trainingController = new TrainingController(
                EPOCHS, BATCH_SIZE, INITIAL_LEARNING_RATE, USE_DATA_AUGMENTATION
        );
        trainingController.setListener(this);

        // データ生成器
        dataGenerator = new MINIST();
    }

    /**
     * UIの初期化
     */
    private void initializeUI() {
        setLayout(new BorderLayout());

        // メインパネル（左右分割）
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.4);

        // 左側：描画エリア
        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        // 右側：予測結果エリア
        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // 下部：ステータスバー
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * 左側パネルの作成（描画エリア）
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Draw a Digit"));

        // 使い方の説明
        JPanel instructionPanel = new JPanel(new BorderLayout());
        instructionPanel.setBackground(new Color(240, 240, 240));
        instructionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea instructions = new JTextArea(
                "Instructions:\n" +
                        "• Draw a digit (0-9) with your mouse\n" +
                        "• The model will predict in real-time\n" +
                        "• Click 'Clear' to erase and start over\n" +
                        "• Click 'Generate Sample' to see examples"
        );
        instructions.setEditable(false);
        instructions.setBackground(instructionPanel.getBackground());
        instructions.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionPanel.add(instructions, BorderLayout.CENTER);

        panel.add(instructionPanel, BorderLayout.NORTH);

        // 描画パネル
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        drawingPanel = new DrawingPanel();
        drawingPanel.addDrawingListener(this::onDrawingChanged);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(drawingPanel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout());

        clearButton = new JButton("Clear");
        clearButton.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
        clearButton.addActionListener(e -> {
            drawingPanel.clear();
            predictionPanel.clearPrediction();
        });
        buttonPanel.add(clearButton);

        generateButton = new JButton("Generate Sample");
        generateButton.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        generateButton.addActionListener(e -> generateSampleDigit());
        buttonPanel.add(generateButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 右側パネルの作成（予測結果と統計）
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Prediction & Statistics"));

        // 予測結果パネル
        predictionPanel = new PredictionPanel();
        panel.add(predictionPanel, BorderLayout.CENTER);

        // コントロールパネル
        JPanel controlPanel = createControlPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * コントロールパネルの作成
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 学習設定の表示
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Training Settings"));
        settingsPanel.setMaximumSize(new Dimension(300, 120));

        settingsPanel.add(new JLabel("Epochs:"));
        settingsPanel.add(new JLabel(String.valueOf(EPOCHS)));

        settingsPanel.add(new JLabel("Batch Size:"));
        settingsPanel.add(new JLabel(String.valueOf(BATCH_SIZE)));

        settingsPanel.add(new JLabel("Learning Rate:"));
        settingsPanel.add(new JLabel(String.valueOf(INITIAL_LEARNING_RATE)));

        settingsPanel.add(new JLabel("Data Augmentation:"));
        settingsPanel.add(new JLabel(USE_DATA_AUGMENTATION ? "Enabled" : "Disabled"));

        panel.add(settingsPanel);
        panel.add(Box.createVerticalStrut(15));

        // ボタン
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setMaximumSize(new Dimension(200, 80));

        trainButton = new JButton("Train Model");
        trainButton.setIcon(UIManager.getIcon("FileView.computerIcon"));
        trainButton.addActionListener(e -> toggleTraining());
        buttonPanel.add(trainButton);

        evaluateButton = new JButton("Evaluate Model");
        evaluateButton.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        evaluateButton.addActionListener(e -> evaluateModel());
        buttonPanel.add(evaluateButton);

        JPanel buttonContainer = new JPanel();
        buttonContainer.add(buttonPanel);
        panel.add(buttonContainer);

        return panel;
    }

    /**
     * ステータスパネルの作成
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Ready - Draw a digit or train the model");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        panel.add(progressBar, BorderLayout.EAST);

        return panel;
    }

    /**
     * 描画が変更されたときの処理
     */
    private void onDrawingChanged() {
        if (!drawingPanel.hasDrawing()) {
            return;
        }

        // 28x28の画像を取得
        double[][] image = drawingPanel.getCurrentImage();

        // 3Dテンソルに変換
        double[][][] input = new double[1][CNN.IMAGE_SIZE][CNN.IMAGE_SIZE];
        input[0] = image;

        // 予測
        TrainingController.PredictionResult result = trainingController.predict(input);

        // 結果を表示
        predictionPanel.updatePrediction(result.predictedClass, result.probabilities);
    }

    /**
     * サンプル数字を生成
     */
    private void generateSampleDigit() {
        int digit = (int)(Math.random() * 10);
        double[][] sample = dataGenerator.generateDigit(digit, 0.05);

        // 描画パネルに表示
        drawingPanel.setImage(sample);

        statusLabel.setText("Generated sample digit: " + digit);
    }

    /**
     * 学習の開始/停止を切り替え
     */
    private void toggleTraining() {
        if (trainingController.isTraining()) {
            trainingController.stopTraining();
            trainButton.setText("Train Model");
        } else {
            trainingController.startTraining();
            trainButton.setText("Stop Training");
            evaluateButton.setEnabled(false);
        }
    }

    /**
     * モデルを評価
     */
    private void evaluateModel() {
        new Thread(() -> {
            statusLabel.setText("Evaluating model...");
            evaluateButton.setEnabled(false);

            TrainingController.EvaluationResult result = trainingController.evaluateDetailed();

            SwingUtilities.invokeLater(() -> {
                // 結果を表示
                String message = String.format(
                        "Overall Accuracy: %.2f%%\n\n" +
                                "Per-class accuracy:\n%s",
                        result.accuracy * 100,
                        formatClassAccuracies(result.classAccuracies)
                );

                JOptionPane.showMessageDialog(this, message,
                        "Evaluation Result", JOptionPane.INFORMATION_MESSAGE);

                evaluateButton.setEnabled(true);
            });
        }).start();
    }

    /**
     * クラスごとの精度をフォーマット
     */
    private String formatClassAccuracies(double[] accuracies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < accuracies.length; i++) {
            sb.append(String.format("Digit %d: %.2f%%\n", i, accuracies[i] * 100));
        }
        return sb.toString();
    }

    // TrainingListener インターフェースの実装
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
        predictionPanel.addTrainingHistory(epoch, loss);
    }

    @Override
    public void onAccuracyUpdated(double accuracy) {
        predictionPanel.updateAccuracy(accuracy);
    }

    @Override
    public void onTrainingCompleted() {
        trainButton.setText("Train Model");
        evaluateButton.setEnabled(true);
        statusLabel.setText("Training completed");
        progressBar.setValue(100);
    }

    @Override
    public void onError(String error) {
        JOptionPane.showMessageDialog(this, error, "Training Error", JOptionPane.ERROR_MESSAGE);
        trainButton.setText("Train Model");
        evaluateButton.setEnabled(true);
    }
}