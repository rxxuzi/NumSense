package math;

public class Matrix {
    /**
     * 行列の積を計算 (A × B)
     * @param a 左側の行列
     * @param b 右側の行列
     * @return 積の結果
     */
    public static double[][] dot(double[][] a, double[][] b) {
        int m = a.length;
        int n = a[0].length;
        int p = b[0].length;

        double[][] result = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /**
     * 行列とベクトルの積を計算 (M × v)
     * @param m 行列
     * @param v ベクトル
     * @return 積の結果（ベクトル）
     */
    public static double[] dotMV(double[][] m, double[] v) {
        int rows = m.length;
        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < v.length; j++) {
                sum += m[i][j] * v[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /**
     * ベクトルの外積を計算 (v1 × v2^T)
     * @param v1 列ベクトル
     * @param v2 行ベクトル
     * @return 外積の結果（行列）
     */
    public static double[][] outer(double[] v1, double[] v2) {
        int m = v1.length;
        int n = v2.length;
        double[][] result = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = v1[i] * v2[j];
            }
        }
        return result;
    }

    /**
     * 行列の転置
     * @param m 元の行列
     * @return 転置行列
     */
    public static double[][] t(double[][] m) {
        int rows = m.length;
        int cols = m[0].length;
        double[][] result = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = m[i][j];
            }
        }
        return result;
    }

    /**
     * 行列の要素ごとの和
     * @param a 行列1
     * @param b 行列2
     * @return 和の結果
     */
    public static double[][] add(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    /**
     * ベクトルの要素ごとの和
     * @param a ベクトル1
     * @param b ベクトル2
     * @return 和の結果
     */
    public static double[] addVec(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    /**
     * ベクトルの要素ごとの積（アダマール積）
     * @param a ベクトル1
     * @param b ベクトル2
     * @return 積の結果
     */
    public static double[] hadamard(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    /**
     * 行列をスカラー倍
     * @param m 行列
     * @param scalar スカラー値
     * @return スカラー倍した行列
     */
    public static double[][] scale(double[][] m, double scalar) {
        int rows = m.length;
        int cols = m[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = m[i][j] * scalar;
            }
        }
        return result;
    }

    /**
     * ベクトルをスカラー倍
     * @param v ベクトル
     * @param scalar スカラー値
     * @return スカラー倍したベクトル
     */
    public static double[] scaleVec(double[] v, double scalar) {
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = v[i] * scalar;
        }
        return result;
    }

    /**
     * 行列のクローンを作成
     * @param m 元の行列
     * @return クローン行列
     */
    public static double[][] clone(double[][] m) {
        int rows = m.length;
        int cols = m[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(m[i], 0, result[i], 0, cols);
        }
        return result;
    }

    /**
     * ベクトルのクローンを作成
     * @param v 元のベクトル
     * @return クローンベクトル
     */
    public static double[] clone(double[] v) {
        return v.clone();
    }
}
