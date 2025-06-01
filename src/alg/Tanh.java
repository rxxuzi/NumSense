package alg;

/**
 * Tanh活性化関数とその導関数
 */
public class Tanh {

    /**
     * tanh関数を適用
     * @param x 入力値
     * @return tanh(x)
     */
    public static double apply(double x) {
        // オーバーフロー/アンダーフロー対策
        if (x > 20) return 1.0;
        if (x < -20) return -1.0;

        double expPos = Math.exp(x);
        double expNeg = Math.exp(-x);
        return (expPos - expNeg) / (expPos + expNeg);
    }

    /**
     * ベクトルの各要素にtanh関数を適用
     * @param x 入力ベクトル
     * @return tanh適用後のベクトル
     */
    public static double[] apply(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = apply(x[i]);
        }
        return result;
    }

    /**
     * tanhの導関数を計算（1 - tanh^2(x)）
     * @param tanhX tanh(x)の値
     * @return tanh'(x)
     */
    public static double derivative(double tanhX) {
        return 1.0 - tanhX * tanhX;
    }

    /**
     * ベクトルの各要素にtanhの導関数を適用
     * @param tanhX tanh適用後のベクトル
     * @return 導関数適用後のベクトル
     */
    public static double[] derivative(double[] tanhX) {
        double[] result = new double[tanhX.length];
        for (int i = 0; i < tanhX.length; i++) {
            result[i] = derivative(tanhX[i]);
        }
        return result;
    }
}
