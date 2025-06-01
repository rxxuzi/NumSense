package alg;

public class CrossEntropy {

    private static final double EPSILON = 1e-8; // 数値安定性のための小さな値

    /**
     * クロスエントロピー損失を計算
     * @param predicted 予測確率分布（Softmax出力）
     * @param targetClass 正解クラスのインデックス
     * @return 損失値
     */
    public static double calculate(double[] predicted, int targetClass) {
        // 数値安定性のためEPSILONを追加
        return -Math.log(predicted[targetClass] + EPSILON);
    }

    /**
     * One-hotベクトルを使用したクロスエントロピー損失
     * @param predicted 予測確率分布
     * @param targetOneHot One-hotエンコードされた正解ラベル
     * @return 損失値
     */
    public static double calculate(double[] predicted, double[] targetOneHot) {
        double loss = 0.0;
        for (int i = 0; i < predicted.length; i++) {
            if (targetOneHot[i] > 0) {
                loss -= targetOneHot[i] * Math.log(predicted[i] + EPSILON);
            }
        }
        return loss;
    }

    /**
     * バッチ全体の平均クロスエントロピー損失
     * @param predicted バッチの予測確率分布 [batchSize][numClasses]
     * @param targetClasses 正解クラスの配列 [batchSize]
     * @return 平均損失値
     */
    public static double calculateBatch(double[][] predicted, int[] targetClasses) {
        double totalLoss = 0.0;
        for (int i = 0; i < predicted.length; i++) {
            totalLoss += calculate(predicted[i], targetClasses[i]);
        }
        return totalLoss / predicted.length;
    }

    /**
     * バッチ全体の平均クロスエントロピー損失（One-hot版）
     * @param predicted バッチの予測確率分布 [batchSize][numClasses]
     * @param targetOneHot One-hotエンコードされた正解ラベル [batchSize][numClasses]
     * @return 平均損失値
     */
    public static double calculateBatch(double[][] predicted, double[][] targetOneHot) {
        double totalLoss = 0.0;
        for (int i = 0; i < predicted.length; i++) {
            totalLoss += calculate(predicted[i], targetOneHot[i]);
        }
        return totalLoss / predicted.length;
    }

    /**
     * バイナリクロスエントロピー損失（2クラス分類用）
     * @param predicted 予測確率（0-1の範囲）
     * @param target 正解ラベル（0または1）
     * @return 損失値
     */
    public static double binaryCalculate(double predicted, double target) {
        predicted = Math.max(EPSILON, Math.min(1 - EPSILON, predicted));
        return -(target * Math.log(predicted) + (1 - target) * Math.log(1 - predicted));
    }

    /**
     * カテゴリカル精度を計算（評価指標）
     * @param predicted 予測確率分布
     * @param targetClass 正解クラス
     * @return 正解なら1.0、不正解なら0.0
     */
    public static double categoricalAccuracy(double[] predicted, int targetClass) {
        int predictedClass = Softmax.argmax(predicted);
        return predictedClass == targetClass ? 1.0 : 0.0;
    }

    /**
     * バッチ全体の精度を計算
     * @param predicted バッチの予測確率分布
     * @param targetClasses 正解クラスの配列
     * @return 精度（0.0-1.0）
     */
    public static double batchAccuracy(double[][] predicted, int[] targetClasses) {
        double correct = 0.0;
        for (int i = 0; i < predicted.length; i++) {
            correct += categoricalAccuracy(predicted[i], targetClasses[i]);
        }
        return correct / predicted.length;
    }

    /**
     * Top-k精度を計算
     * @param predicted 予測確率分布
     * @param targetClass 正解クラス
     * @param k 上位k個まで正解とみなす
     * @return Top-k精度
     */
    public static double topKAccuracy(double[] predicted, int targetClass, int k) {
        int[] topK = Softmax.topK(predicted, k);
        for (int idx : topK) {
            if (idx == targetClass) {
                return 1.0;
            }
        }
        return 0.0;
    }

    /**
     * One-hotエンコーディング
     * @param classIndex クラスインデックス
     * @param numClasses クラス数
     * @return One-hotベクトル
     */
    public static double[] oneHotEncode(int classIndex, int numClasses) {
        double[] oneHot = new double[numClasses];
        oneHot[classIndex] = 1.0;
        return oneHot;
    }

    /**
     * バッチOne-hotエンコーディング
     * @param classIndices クラスインデックスの配列
     * @param numClasses クラス数
     * @return One-hot行列
     */
    public static double[][] oneHotEncodeBatch(int[] classIndices, int numClasses) {
        double[][] oneHot = new double[classIndices.length][numClasses];
        for (int i = 0; i < classIndices.length; i++) {
            oneHot[i][classIndices[i]] = 1.0;
        }
        return oneHot;
    }
}