package calc;

import math.Matrix;
import alg.Tanh;
import alg.MSE;

/**
 * 逆伝播（Backward Propagation）の計算
 * 改良版：複数の隠れ層をサポート
 */
public class Backward {

    // 勾配
    private double[][] gradW1;
    private double[] gradB1;
    private double[][] gradW2;
    private double[] gradB2;
    private double[][] gradW3;
    private double[] gradB3;

    /**
     * 逆伝播の実行
     * @param fwd 順伝播オブジェクト
     * @param y 正解値
     * @return 損失値
     */
    public double backward(Forward fwd, double y) {
        // 損失の計算
        double loss = MSE.calculate(fwd.getA3(), y);

        // 出力層のデルタ（恒等活性化なので導関数は1）
        double delta3 = MSE.gradient(fwd.getA3(), y);

        // 出力層の勾配
        gradW3 = Matrix.outer(new double[]{delta3}, fwd.getA2());
        gradB3 = new double[]{delta3};

        // 隠れ層2のデルタ
        double[] delta2Temp = Matrix.dotMV(Matrix.t(fwd.getW3()), new double[]{delta3});
        double[] tanhDeriv2 = Tanh.derivative(fwd.getA2());
        double[] delta2 = Matrix.hadamard(delta2Temp, tanhDeriv2);

        // 隠れ層2の勾配
        gradW2 = Matrix.outer(delta2, fwd.getA1());
        gradB2 = delta2;

        // 隠れ層1のデルタ
        double[] delta1Temp = Matrix.dotMV(Matrix.t(fwd.getW2()), delta2);
        double[] tanhDeriv1 = Tanh.derivative(fwd.getA1());
        double[] delta1 = Matrix.hadamard(delta1Temp, tanhDeriv1);

        // 隠れ層1の勾配
        gradW1 = Matrix.outer(delta1, new double[]{fwd.getLastInput()});
        gradB1 = delta1;

        return loss;
    }

    // ゲッターメソッド
    public double[][] getGradW1() { return gradW1; }
    public double[] getGradB1() { return gradB1; }
    public double[][] getGradW2() { return gradW2; }
    public double[] getGradB2() { return gradB2; }
    public double[][] getGradW3() { return gradW3; }
    public double[] getGradB3() { return gradB3; }
}
