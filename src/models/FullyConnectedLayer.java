package models;

import math.Matrix;

import java.util.Random;

/**
 * 全結合層
 */
class FullyConnectedLayer {
    private double[][] weights;
    private double[] bias;
    private double[][] gradWeights;
    private double[] gradBias;

    private double learningRate;
    private int inputSize, outputSize;

    // Adamオプティマイザー用
    private double[][] mWeights, vWeights;
    private double[] mBias, vBias;
    private int t = 0;

    public FullyConnectedLayer(int inputSize, int outputSize, double learningRate) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.learningRate = learningRate;

        // Xavierの初期化
        double scale = Math.sqrt(2.0 / inputSize);
        Random rand = new Random();

        weights = new double[outputSize][inputSize];
        bias = new double[outputSize];

        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[i][j] = rand.nextGaussian() * scale;
            }
        }

        // Adamの初期化
        mWeights = new double[outputSize][inputSize];
        vWeights = new double[outputSize][inputSize];
        mBias = new double[outputSize];
        vBias = new double[outputSize];
    }

    public double[] forward(double[] input) {
        return Matrix.addVec(Matrix.dotMV(weights, input), bias);
    }

    public double[] backward(double[] gradOutput, double[] input) {
        // 重みとバイアスの勾配
        gradWeights = Matrix.outer(gradOutput, input);
        gradBias = gradOutput.clone();

        // 入力に対する勾配
        return Matrix.dotMV(Matrix.t(weights), gradOutput);
    }

    public void updateWeights() {
        t++;
        double beta1 = 0.9, beta2 = 0.999, epsilon = 1e-8;

        // Adam更新
        for (int i = 0; i < outputSize; i++) {
            // バイアスの更新
            updateBias(beta1, beta2, epsilon, i, mBias, gradBias, vBias, t, bias, learningRate);
            double mHat;
            double vHat;

            // 重みの更新
            for (int j = 0; j < inputSize; j++) {
                mWeights[i][j] = beta1 * mWeights[i][j] + (1 - beta1) * gradWeights[i][j];
                vWeights[i][j] = beta2 * vWeights[i][j] + (1 - beta2) * gradWeights[i][j] * gradWeights[i][j];
                mHat = mWeights[i][j] / (1 - Math.pow(beta1, t));
                vHat = vWeights[i][j] / (1 - Math.pow(beta2, t));
                weights[i][j] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
            }
        }
    }

    static void updateBias(double beta1, double beta2, double epsilon, int i, double[] mBias, double[] gradBias, double[] vBias, int t, double[] bias, double learningRate) {
        mBias[i] = beta1 * mBias[i] + (1 - beta1) * gradBias[i];
        vBias[i] = beta2 * vBias[i] + (1 - beta2) * gradBias[i] * gradBias[i];
        double mHat = mBias[i] / (1 - Math.pow(beta1, t));
        double vHat = vBias[i] / (1 - Math.pow(beta2, t));
        bias[i] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
    }

    public void setLearningRate(double lr) {
        this.learningRate = lr;
    }
}
