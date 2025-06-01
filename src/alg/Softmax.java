package alg;

public class Softmax {

    /**
     * Softmax関数を適用
     * @param x 入力ベクトル
     * @return Softmax適用後のベクトル（確率分布）
     */
    public static double[] apply(double[] x) {
        // オーバーフロー対策として最大値を引く
        double max = Double.NEGATIVE_INFINITY;
        for (double val : x) {
            max = Math.max(max, val);
        }

        // exp(x - max)を計算
        double[] exp = new double[x.length];
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            exp[i] = Math.exp(x[i] - max);
            sum += exp[i];
        }

        // 正規化
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = exp[i] / sum;
        }

        return result;
    }

    /**
     * バッチ処理用のSoftmax（各行に対して適用）
     * @param x 入力行列 [batchSize][numClasses]
     * @return Softmax適用後の行列
     */
    public static double[][] apply(double[][] x) {
        int batchSize = x.length;
        double[][] result = new double[batchSize][];

        for (int i = 0; i < batchSize; i++) {
            result[i] = apply(x[i]);
        }

        return result;
    }

    /**
     * Softmaxとクロスエントロピー損失の組み合わせの勾配
     * （計算効率のため組み合わせて実装）
     * @param softmaxOutput Softmaxの出力
     * @param targetClass 正解クラスのインデックス
     * @return 勾配ベクトル
     */
    public static double[] gradientWithCrossEntropy(double[] softmaxOutput, int targetClass) {
        double[] gradient = new double[softmaxOutput.length];

        for (int i = 0; i < softmaxOutput.length; i++) {
            if (i == targetClass) {
                gradient[i] = softmaxOutput[i] - 1.0;
            } else {
                gradient[i] = softmaxOutput[i];
            }
        }

        return gradient;
    }

    /**
     * One-hotベクトルを使用した勾配計算
     * @param softmaxOutput Softmaxの出力
     * @param targetOneHot One-hotエンコードされた正解ラベル
     * @return 勾配ベクトル
     */
    public static double[] gradientWithCrossEntropy(double[] softmaxOutput, double[] targetOneHot) {
        double[] gradient = new double[softmaxOutput.length];

        for (int i = 0; i < softmaxOutput.length; i++) {
            gradient[i] = softmaxOutput[i] - targetOneHot[i];
        }

        return gradient;
    }

    /**
     * 温度付きSoftmax（Temperature Scaling）
     * @param x 入力ベクトル
     * @param temperature 温度パラメータ（1.0が通常、大きいほど平滑化）
     * @return Softmax適用後のベクトル
     */
    public static double[] applyWithTemperature(double[] x, double temperature) {
        double[] scaledX = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            scaledX[i] = x[i] / temperature;
        }
        return apply(scaledX);
    }

    /**
     * 最大確率のクラスインデックスを取得
     * @param probabilities Softmaxの出力（確率分布）
     * @return 最大確率のクラスインデックス
     */
    public static int argmax(double[] probabilities) {
        int maxIndex = 0;
        double maxProb = probabilities[0];

        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    /**
     * Top-k確率のクラスインデックスを取得
     * @param probabilities Softmaxの出力
     * @param k 取得する上位クラス数
     * @return Top-kクラスのインデックス配列
     */
    public static int[] topK(double[] probabilities, int k) {
        k = Math.min(k, probabilities.length);
        int[] indices = new int[k];
        boolean[] used = new boolean[probabilities.length];

        for (int i = 0; i < k; i++) {
            int maxIndex = -1;
            double maxProb = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < probabilities.length; j++) {
                if (!used[j] && probabilities[j] > maxProb) {
                    maxProb = probabilities[j];
                    maxIndex = j;
                }
            }

            indices[i] = maxIndex;
            used[maxIndex] = true;
        }

        return indices;
    }
}