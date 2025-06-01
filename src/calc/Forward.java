package calc;

import math.Matrix;
import alg.Tanh;

/**
 * 順伝播（Forward Propagation）の計算
 * 改良版：複数の隠れ層をサポート
 */
public class Forward {

    // ネットワークの重みとバイアス
    private double[][] W1;  // 隠れ層1の重み
    private double[] b1;    // 隠れ層1のバイアス
    private double[][] W2;  // 隠れ層2の重み
    private double[] b2;    // 隠れ層2のバイアス
    private double[][] W3;  // 出力層の重み
    private double[] b3;    // 出力層のバイアス

    // 中間計算結果（逆伝播で使用）
    private double[] z1;    // 隠れ層1の入力
    private double[] a1;    // 隠れ層1の出力
    private double[] z2;    // 隠れ層2の入力
    private double[] a2;    // 隠れ層2の出力
    private double z3;      // 出力層の入力
    private double a3;      // 出力層の出力（最終出力）

    private double lastInput; // 最後の入力値（逆伝播で使用）

    /**
     * コンストラクタ（2層隠れ層）
     * @param inputSize 入力層のサイズ
     * @param hidden1Size 隠れ層1のサイズ
     * @param hidden2Size 隠れ層2のサイズ
     * @param outputSize 出力層のサイズ
     */
    public Forward(int inputSize, int hidden1Size, int hidden2Size, int outputSize) {
        // He初期化（ReLU用）をTanh用に調整
        double scale1 = Math.sqrt(2.0 / inputSize) * 0.8;
        double scale2 = Math.sqrt(2.0 / hidden1Size) * 0.8;
        double scale3 = Math.sqrt(2.0 / hidden2Size) * 0.8;

        W1 = new double[hidden1Size][inputSize];
        b1 = new double[hidden1Size];
        W2 = new double[hidden2Size][hidden1Size];
        b2 = new double[hidden2Size];
        W3 = new double[outputSize][hidden2Size];
        b3 = new double[outputSize];

        // ランダム初期化（改良版）
        java.util.Random rand = new java.util.Random(42);

        // W1の初期化
        for (int i = 0; i < hidden1Size; i++) {
            for (int j = 0; j < inputSize; j++) {
                W1[i][j] = (rand.nextGaussian()) * scale1;
            }
            b1[i] = 0.0;
        }

        // W2の初期化
        for (int i = 0; i < hidden2Size; i++) {
            for (int j = 0; j < hidden1Size; j++) {
                W2[i][j] = (rand.nextGaussian()) * scale2;
            }
            b2[i] = 0.0;
        }

        // W3の初期化
        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < hidden2Size; j++) {
                W3[i][j] = (rand.nextGaussian()) * scale3;
            }
            b3[i] = 0.0;
        }
    }

    /**
     * 順伝播の実行
     * @param x 入力値
     * @return 出力値
     */
    public double forward(double x) {
        lastInput = x;

        // 入力を配列形式に変換
        double[] input = {x};

        // 隠れ層1の計算
        z1 = Matrix.dotMV(W1, input);
        z1 = Matrix.addVec(z1, b1);
        a1 = Tanh.apply(z1);

        // 隠れ層2の計算
        z2 = Matrix.dotMV(W2, a1);
        z2 = Matrix.addVec(z2, b2);
        a2 = Tanh.apply(z2);

        // 出力層の計算（恒等活性化）
        double[] z3Array = Matrix.dotMV(W3, a2);
        z3Array = Matrix.addVec(z3Array, b3);
        z3 = z3Array[0];  // スカラーに変換
        a3 = z3;  // 恒等活性化

        return a3;
    }

    // ゲッターメソッド（逆伝播で使用）
    public double getLastInput() { return lastInput; }
    public double[] getZ1() { return z1; }
    public double[] getA1() { return a1; }
    public double[] getZ2() { return z2; }
    public double[] getA2() { return a2; }
    public double getZ3() { return z3; }
    public double getA3() { return a3; }
    public double[][] getW1() { return W1; }
    public double[] getB1() { return b1; }
    public double[][] getW2() { return W2; }
    public double[] getB2() { return b2; }
    public double[][] getW3() { return W3; }
    public double[] getB3() { return b3; }

    // セッターメソッド（重み更新用）
    public void setW1(double[][] w1) { this.W1 = w1; }
    public void setB1(double[] b1) { this.b1 = b1; }
    public void setW2(double[][] w2) { this.W2 = w2; }
    public void setB2(double[] b2) { this.b2 = b2; }
    public void setW3(double[][] w3) { this.W3 = w3; }
    public void setB3(double[] b3) { this.b3 = b3; }
}
