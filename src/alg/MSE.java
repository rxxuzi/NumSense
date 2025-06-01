package alg;

/**
 * 平均二乗誤差（Mean Squared Error）
 */
public class MSE {

    /**
     * MSE損失を計算
     * @param predicted 予測値
     * @param actual 実際の値
     * @return MSE損失
     */
    public static double calculate(double predicted, double actual) {
        double diff = predicted - actual;
        return 0.5 * diff * diff;
    }

    /**
     * MSE損失の勾配を計算
     * @param predicted 予測値
     * @param actual 実際の値
     * @return 勾配
     */
    public static double gradient(double predicted, double actual) {
        return predicted - actual;
    }

    /**
     * バッチ全体のMSE損失を計算
     * @param predicted 予測値の配列
     * @param actual 実際の値の配列
     * @return 平均MSE損失
     */
    public static double calculateBatch(double[] predicted, double[] actual) {
        double sum = 0.0;
        for (int i = 0; i < predicted.length; i++) {
            sum += calculate(predicted[i], actual[i]);
        }
        return sum / predicted.length;
    }
}
