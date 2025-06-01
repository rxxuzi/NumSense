package models;

import math.ConvolutionOps;

import java.util.Random;

/**
 * 畳み込み層
 */
class ConvLayer {
    private final double[][][][] weights;  // [outChannels][inChannels][kernelH][kernelW]
    private final double[] bias;          // [outChannels]
    private double[][][][] gradWeights;
    private double[] gradBias;

    private double learningRate;
    private int inChannels, outChannels, kernelSize, stride, padding;

    // Adamオプティマイザー用
    private double[][][][] mWeights, vWeights;
    private double[] mBias, vBias;
    private int t = 0;

    public ConvLayer(int inChannels, int outChannels, int kernelSize,
                     int stride, int padding, double learningRate) {
        this.inChannels = inChannels;
        this.outChannels = outChannels;
        this.kernelSize = kernelSize;
        this.stride = stride;
        this.padding = padding;
        this.learningRate = learningRate;

        // Xavierの初期化
        double scale = Math.sqrt(2.0 / (inChannels * kernelSize * kernelSize));
        Random rand = new Random();

        weights = new double[outChannels][inChannels][kernelSize][kernelSize];
        bias = new double[outChannels];

        for (int oc = 0; oc < outChannels; oc++) {
            for (int ic = 0; ic < inChannels; ic++) {
                for (int kh = 0; kh < kernelSize; kh++) {
                    for (int kw = 0; kw < kernelSize; kw++) {
                        weights[oc][ic][kh][kw] = rand.nextGaussian() * scale;
                    }
                }
            }
        }

        // Adamの初期化
        mWeights = new double[outChannels][inChannels][kernelSize][kernelSize];
        vWeights = new double[outChannels][inChannels][kernelSize][kernelSize];
        mBias = new double[outChannels];
        vBias = new double[outChannels];
    }

    public double[][][] forward(double[][][] input) {
        return ConvolutionOps.convolve3D(input, weights, bias, stride, padding);
    }

    public double[][][] backward(double[][][] gradOutput, double[][][] input) {
        // 勾配を計算
        gradWeights = new double[outChannels][inChannels][kernelSize][kernelSize];
        gradBias = new double[outChannels];

        // バイアスの勾配
        for (int oc = 0; oc < outChannels; oc++) {
            double sum = 0;
            for (int h = 0; h < gradOutput[oc].length; h++) {
                for (int w = 0; w < gradOutput[oc][h].length; w++) {
                    sum += gradOutput[oc][h][w];
                }
            }
            gradBias[oc] = sum;
        }

        // 重みの勾配（簡略化版）
        // TODO: 完全な畳み込みの逆伝播実装

        // 入力に対する勾配を返す（簡略化版）
        return new double[inChannels][input[0].length][input[0][0].length];
    }

    public void updateWeights() {
        t++;
        double beta1 = 0.9, beta2 = 0.999, epsilon = 1e-8;

        // Adam更新
        for (int oc = 0; oc < outChannels; oc++) {
            // バイアスの更新
            FullyConnectedLayer.updateBias(beta1, beta2, epsilon, oc, mBias, gradBias, vBias, t, bias, learningRate);
            double mHat;
            double vHat;

            // 重みの更新
            for (int ic = 0; ic < inChannels; ic++) {
                for (int kh = 0; kh < kernelSize; kh++) {
                    for (int kw = 0; kw < kernelSize; kw++) {
                        double grad = gradWeights[oc][ic][kh][kw];
                        mWeights[oc][ic][kh][kw] = beta1 * mWeights[oc][ic][kh][kw] + (1 - beta1) * grad;
                        vWeights[oc][ic][kh][kw] = beta2 * vWeights[oc][ic][kh][kw] + (1 - beta2) * grad * grad;
                        mHat = mWeights[oc][ic][kh][kw] / (1 - Math.pow(beta1, t));
                        vHat = vWeights[oc][ic][kh][kw] / (1 - Math.pow(beta2, t));
                        weights[oc][ic][kh][kw] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
                    }
                }
            }
        }
    }

    public void setLearningRate(double lr) {
        this.learningRate = lr;
    }
}
