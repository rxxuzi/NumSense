package ui;

import models.ImprovedCNN;
import models.ModelSerializer;
import data.MINIST;
import javax.swing.*;
import java.util.Random;
import java.io.File;

/**
 * CNNモデルの学習を制御するクラス（完全版）
 */
public class TrainingController {

    // モデルとデータ生成器
    private ImprovedCNN model;
    private MINIST dataGenerator;

    // 学習パラメータ
    private int epochs;
    private int batchSize;
    private final double learningRate;
    private boolean useDataAugmentation;

    // 学習状態
    private volatile boolean isTraining = false;
    private volatile boolean stopRequested = false;
    private Thread trainingThread;

    // プログレスリスナー
    private TrainingListener listener;

    /**
     * 学習進捗リスナーインターフェース
     * GUIやコンソールなど、様々な場所から学習の進捗を監視できる
     */
    public interface TrainingListener {
        void onStatusChanged(String status);           // ステータスメッセージが変更されたとき
        void onProgressChanged(int progress);          // 進捗率が変更されたとき（0-100）
        void onEpochCompleted(int epoch, double loss); // エポックが完了したとき
        void onAccuracyUpdated(double accuracy);       // 精度が更新されたとき
        void onTrainingCompleted();                    // 学習が完了したとき
        void onError(String error);                    // エラーが発生したとき
        void onModelSaved(String filepath);            // モデルが保存されたとき
        void onModelLoaded(String filepath);           // モデルが読み込まれたとき
    }

    /**
     * コンストラクタ
     */
    public TrainingController(int epochs, int batchSize, double learningRate, boolean useDataAugmentation) {
        this.epochs = epochs;
        this.batchSize = batchSize;
        this.learningRate = learningRate;
        this.useDataAugmentation = useDataAugmentation;

        this.model = new ImprovedCNN(learningRate);
        this.dataGenerator = new MINIST();
    }

    /**
     * リスナーを設定
     */
    public void setListener(TrainingListener listener) {
        this.listener = listener;
    }

    /**
     * モデルを取得
     */
    public ImprovedCNN getModel() {
        return model;
    }

    /**
     * 学習を開始
     */
    public void startTraining() {
        if (isTraining) {
            return;
        }

        isTraining = true;
        stopRequested = false;

        trainingThread = new Thread(this::runTraining);
        trainingThread.setName("CNN-Training-Thread");
        trainingThread.start();
    }

    /**
     * 学習を停止
     */
    public void stopTraining() {
        stopRequested = true;
    }

    /**
     * 学習中かどうか
     */
    public boolean isTraining() {
        return isTraining;
    }

    /**
     * 学習の実行
     */
    private void runTraining() {
        try {
            // 訓練データの生成
            notifyStatus("Generating training data...");
            TrainingData trainingData = generateTrainingData();

            // データ拡張用のRandom
            Random augmentRand = new Random();

            // エポックごとの学習
            for (int epoch = 0; epoch < epochs && !stopRequested; epoch++) {
                notifyStatus("Training epoch " + (epoch + 1) + "/" + epochs);
                notifyProgress((epoch * 100) / epochs);

                // データをシャッフル
                shuffleData(trainingData.images, trainingData.labels);

                // ミニバッチ学習
                double epochLoss = trainEpoch(trainingData, augmentRand, epoch);

                // エポック終了処理
                model.endEpoch();
                notifyEpochCompleted(epoch + 1, epochLoss);

                // 定期的に評価
                if ((epoch + 1) % 5 == 0) {
                    evaluateModel();
                }
            }

            // 最終評価
            if (!stopRequested) {
                notifyStatus("Final evaluation...");
                evaluateModel();
            }

        } catch (Exception e) {
            notifyError("Training error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 学習完了後に自動保存
            if (!stopRequested) {
                notifyStatus("Saving model...");
                saveModel("./outputs/cnn.jnn");
            }

            isTraining = false;
            notifyTrainingCompleted();
        }
    }

    /**
     * 訓練データを生成
     */
    private TrainingData generateTrainingData() {
        int trainSize = 6000;
        double[][][][] images = new double[trainSize][1][32][32];  // 32x32画像
        int[] labels = new int[trainSize];

        // 各数字を均等に生成
        for (int i = 0; i < trainSize; i++) {
            int digit = i % 10;
            images[i][0] = dataGenerator.generateDigit(digit, 0.1);
            labels[i] = digit;

            // 進捗更新
            if (i % 100 == 0) {
                notifyProgress((i * 10) / trainSize);  // 最初の10%
            }
        }

        return new TrainingData(images, labels);
    }

    /**
     * 1エポックの学習
     */
    private double trainEpoch(TrainingData data, Random augmentRand, int epochIndex) {
        double epochLoss = 0.0;
        int numBatches = data.images.length / batchSize;

        for (int batch = 0; batch < numBatches && !stopRequested; batch++) {
            double batchLoss = trainBatch(data, batch, augmentRand, epochIndex);
            epochLoss += batchLoss;

            // バッチごとの進捗更新
            int currentProgress = ((epochIndex * numBatches + batch) * 100) / (epochs * numBatches);
            notifyProgress(currentProgress);
        }

        return epochLoss / numBatches;
    }

    /**
     * 1バッチの学習
     */
    private double trainBatch(TrainingData data, int batchIndex, Random augmentRand, int epochIndex) {
        double batchLoss = 0.0;
        int start = batchIndex * batchSize;
        int end = Math.min(start + batchSize, data.images.length);

        for (int i = start; i < end; i++) {
            // データ拡張
            double[][][] image = data.images[i];
            if (useDataAugmentation && epochIndex < epochs - 2) {
                image = ImprovedCNN.augmentImage(image, augmentRand);
            }

            // 学習
            double loss = model.train(image, data.labels[i]);
            batchLoss += loss;
        }

        return batchLoss / (end - start);
    }

    /**
     * モデルを評価
     */
    public double evaluateModel() {
        // テストデータの生成
        int testSize = 1000;
        double[][][][] testImages = new double[testSize][1][32][32];
        int[] testLabels = new int[testSize];

        for (int i = 0; i < testSize; i++) {
            int digit = i % 10;
            testImages[i][0] = dataGenerator.generateDigit(digit, 0.05);  // ノイズ少なめ
            testLabels[i] = digit;
        }

        // 評価
        int correct = 0;
        for (int i = 0; i < testSize; i++) {
            int predicted = model.predict(testImages[i]);
            if (predicted == testLabels[i]) {
                correct++;
            }
        }

        double accuracy = (double) correct / testSize;
        notifyAccuracyUpdated(accuracy);

        return accuracy;
    }

    /**
     * 詳細な評価（混同行列付き）
     */
    public EvaluationResult evaluateDetailed() {
        int testSize = 1000;
        double[][][][] testImages = new double[testSize][1][32][32];
        int[] testLabels = new int[testSize];
        int[][] confusionMatrix = new int[10][10];

        // テストデータ生成
        for (int i = 0; i < testSize; i++) {
            int digit = i % 10;
            testImages[i][0] = dataGenerator.generateDigit(digit, 0.05);
            testLabels[i] = digit;
        }

        // 評価
        int correct = 0;
        for (int i = 0; i < testSize; i++) {
            int predicted = model.predict(testImages[i]);
            if (predicted == testLabels[i]) {
                correct++;
            }
            confusionMatrix[testLabels[i]][predicted]++;
        }

        double accuracy = (double) correct / testSize;

        // 各クラスの精度計算
        double[] classAccuracies = new double[10];
        for (int i = 0; i < 10; i++) {
            int classTotal = 0;
            int classCorrect = confusionMatrix[i][i];
            for (int j = 0; j < 10; j++) {
                classTotal += confusionMatrix[i][j];
            }
            classAccuracies[i] = classTotal > 0 ? (double) classCorrect / classTotal : 0;
        }

        return new EvaluationResult(accuracy, confusionMatrix, classAccuracies);
    }

    /**
     * 予測（確率付き）
     */
    public PredictionResult predict(double[][][] input) {
        double[] probabilities = model.forward(input);
        int predictedClass = alg.Softmax.argmax(probabilities);
        return new PredictionResult(predictedClass, probabilities);
    }

    /**
     * モデルを保存
     */
    public void saveModel(String filepath) {
        try {
            ModelSerializer.saveModel(model, filepath);
            if (listener != null) {
                SwingUtilities.invokeLater(() -> listener.onModelSaved(filepath));
            }
        } catch (Exception e) {
            notifyError("Failed to save model: " + e.getMessage());
        }
    }

    /**
     * モデルを読み込み
     */
    public void loadModel(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                notifyError("Model file not found: " + filepath);
                return;
            }

            model = ModelSerializer.loadModel(filepath);
            if (listener != null) {
                SwingUtilities.invokeLater(() -> listener.onModelLoaded(filepath));
            }

            // 読み込み後に評価
            evaluateModel();
        } catch (Exception e) {
            notifyError("Failed to load model: " + e.getMessage());
        }
    }

    /**
     * モデルファイルが存在するか確認
     */
    public boolean modelFileExists(String filepath) {
        return new File(filepath).exists();
    }

    /**
     * データのシャッフル
     */
    private void shuffleData(double[][][][] images, int[] labels) {
        Random rand = new Random();
        for (int i = images.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);

            // 画像の交換
            double[][][] tempImage = images[i];
            images[i] = images[j];
            images[j] = tempImage;

            // ラベルの交換
            int tempLabel = labels[i];
            labels[i] = labels[j];
            labels[j] = tempLabel;
        }
    }

    // ========== 通知メソッド（リスナーパターンの実装） ==========

    private void notifyStatus(String status) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onStatusChanged(status));
        }
    }

    private void notifyProgress(int progress) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onProgressChanged(progress));
        }
    }

    private void notifyEpochCompleted(int epoch, double loss) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onEpochCompleted(epoch, loss));
        }
    }

    private void notifyAccuracyUpdated(double accuracy) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onAccuracyUpdated(accuracy));
        }
    }

    private void notifyTrainingCompleted() {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onTrainingCompleted());
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onError(error));
        }
    }

    private void notifyModelSaved(String filepath) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onModelSaved(filepath));
        }
    }

    private void notifyModelLoaded(String filepath) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onModelLoaded(filepath));
        }
    }

    // ========== 内部クラス ==========

    private static class TrainingData {
        final double[][][][] images;
        final int[] labels;

        TrainingData(double[][][][] images, int[] labels) {
            this.images = images;
            this.labels = labels;
        }
    }

    /**
     * 予測結果
     */
    public static class PredictionResult {
        public final int predictedClass;
        public final double[] probabilities;

        public PredictionResult(int predictedClass, double[] probabilities) {
            this.predictedClass = predictedClass;
            this.probabilities = probabilities;
        }

        public double getConfidence() {
            return probabilities[predictedClass];
        }
    }

    /**
     * 評価結果
     */
    public static class EvaluationResult {
        public final double accuracy;
        public final int[][] confusionMatrix;
        public final double[] classAccuracies;

        public EvaluationResult(double accuracy, int[][] confusionMatrix, double[] classAccuracies) {
            this.accuracy = accuracy;
            this.confusionMatrix = confusionMatrix;
            this.classAccuracies = classAccuracies;
        }
    }
}