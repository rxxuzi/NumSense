package alg;

public class ReLU {

    /**
     * ReLU関数を適用: f(x) = max(0, x)
     * @param x 入力値
     * @return ReLU(x)
     */
    public static double apply(double x) {
        return Math.max(0, x);
    }

    /**
     * ベクトルの各要素にReLU関数を適用
     * @param x 入力ベクトル
     * @return ReLU適用後のベクトル
     */
    public static double[] apply(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = apply(x[i]);
        }
        return result;
    }

    /**
     * 2D配列の各要素にReLU関数を適用
     * @param x 入力2D配列
     * @return ReLU適用後の2D配列
     */
    public static double[][] apply(double[][] x) {
        int height = x.length;
        int width = x[0].length;
        double[][] result = new double[height][width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                result[h][w] = apply(x[h][w]);
            }
        }
        return result;
    }

    /**
     * 3Dテンソルの各要素にReLU関数を適用
     * @param x 入力3Dテンソル [channels][height][width]
     * @return ReLU適用後の3Dテンソル
     */
    public static double[][][] apply(double[][][] x) {
        int channels = x.length;
        int height = x[0].length;
        int width = x[0][0].length;
        double[][][] result = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    result[c][h][w] = apply(x[c][h][w]);
                }
            }
        }
        return result;
    }

    /**
     * ReLUの導関数: f'(x) = 1 if x > 0, 0 otherwise
     * @param x 入力値（ReLU適用前の値）
     * @return 導関数の値
     */
    public static double derivative(double x) {
        return x > 0 ? 1.0 : 0.0;
    }

    /**
     * ベクトルの各要素にReLUの導関数を適用
     * @param x 入力ベクトル（ReLU適用前の値）
     * @return 導関数適用後のベクトル
     */
    public static double[] derivative(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = derivative(x[i]);
        }
        return result;
    }

    /**
     * 2D配列の各要素にReLUの導関数を適用
     * @param x 入力2D配列（ReLU適用前の値）
     * @return 導関数適用後の2D配列
     */
    public static double[][] derivative(double[][] x) {
        int height = x.length;
        int width = x[0].length;
        double[][] result = new double[height][width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                result[h][w] = derivative(x[h][w]);
            }
        }
        return result;
    }

    /**
     * 3Dテンソルの各要素にReLUの導関数を適用
     * @param x 入力3Dテンソル（ReLU適用前の値）
     * @return 導関数適用後の3Dテンソル
     */
    public static double[][][] derivative(double[][][] x) {
        int channels = x.length;
        int height = x[0].length;
        int width = x[0][0].length;
        double[][][] result = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    result[c][h][w] = derivative(x[c][h][w]);
                }
            }
        }
        return result;
    }

    /**
     * Leaky ReLU関数: f(x) = max(alpha * x, x)
     * ReLUの改良版で、負の値に対しても小さな勾配を持つ
     * @param x 入力値
     * @param alpha 負の領域の傾き（通常0.01）
     * @return Leaky ReLU(x)
     */
    public static double leakyApply(double x, double alpha) {
        return x > 0 ? x : alpha * x;
    }

    /**
     * Leaky ReLUの導関数
     * @param x 入力値
     * @param alpha 負の領域の傾き
     * @return 導関数の値
     */
    public static double leakyDerivative(double x, double alpha) {
        return x > 0 ? 1.0 : alpha;
    }
}