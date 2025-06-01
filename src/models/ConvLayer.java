package models;

import math.ConvolutionOps;
import java.util.Random;

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

    // 逆伝播用のキャッシュ
    private double[][][] lastInput;
    private double[][][] lastOutput;

    public ConvLayer(int inChannels, int outChannels, int kernelSize,
                     int stride, int padding, double learningRate) {
        this.inChannels = inChannels;
        this.outChannels = outChannels;
        this.kernelSize = kernelSize;
        this.stride = stride;
        this.padding = padding;
        this.learningRate = learningRate;

        // He初期化
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
        // 入力をキャッシュ（逆伝播で使用）
        this.lastInput = input;

        // 畳み込み演算
        double[][][] output = ConvolutionOps.convolve3D(input, weights, bias, stride, padding);

        // 出力をキャッシュ
        this.lastOutput = output;

        return output;
    }

    /**
     * 完全な逆伝播実装
     * @param gradOutput 出力に対する勾配
     * @param input 順伝播時の入力（キャッシュから取得）
     * @return 入力に対する勾配
     */
    public double[][][] backward(double[][][] gradOutput, double[][][] input) {
        // 使用する入力（キャッシュがあればそれを使用）
        if (lastInput != null) {
            input = lastInput;
        }

        // 勾配の初期化
        gradWeights = new double[outChannels][inChannels][kernelSize][kernelSize];
        gradBias = new double[outChannels];

        // パディングされた入力を準備
        double[][][] paddedInput = new double[inChannels][][];
        for (int ic = 0; ic < inChannels; ic++) {
            paddedInput[ic] = ConvolutionOps.applyPadding(input[ic], padding);
        }

        // 出力サイズ
        int outputHeight = gradOutput[0].length;
        int outputWidth = gradOutput[0][0].length;

        // 1. バイアスの勾配を計算（出力勾配の総和）
        for (int oc = 0; oc < outChannels; oc++) {
            double sum = 0;
            for (int oh = 0; oh < outputHeight; oh++) {
                for (int ow = 0; ow < outputWidth; ow++) {
                    sum += gradOutput[oc][oh][ow];
                }
            }
            gradBias[oc] = sum;
        }

        // 2. 重みの勾配を計算
        // dL/dW = input * gradOutput （相関演算）
        for (int oc = 0; oc < outChannels; oc++) {
            for (int ic = 0; ic < inChannels; ic++) {
                for (int kh = 0; kh < kernelSize; kh++) {
                    for (int kw = 0; kw < kernelSize; kw++) {
                        double sum = 0;

                        // 出力の各位置について
                        for (int oh = 0; oh < outputHeight; oh++) {
                            for (int ow = 0; ow < outputWidth; ow++) {
                                // 対応する入力位置
                                int ih = oh * stride + kh;
                                int iw = ow * stride + kw;

                                sum += gradOutput[oc][oh][ow] * paddedInput[ic][ih][iw];
                            }
                        }

                        gradWeights[oc][ic][kh][kw] = sum;
                    }
                }
            }
        }

        // 3. 入力に対する勾配を計算（転置畳み込み）
        // パディングを考慮した入力サイズ
        int paddedHeight = input[0].length + 2 * padding;
        int paddedWidth = input[0][0].length + 2 * padding;

        // パディングされた勾配入力を初期化
        double[][][] paddedGradInput = new double[inChannels][paddedHeight][paddedWidth];

        // 各入力チャンネルについて
        for (int ic = 0; ic < inChannels; ic++) {
            // 各出力チャンネルからの寄与を累積
            for (int oc = 0; oc < outChannels; oc++) {
                // 出力の各位置について
                for (int oh = 0; oh < outputHeight; oh++) {
                    for (int ow = 0; ow < outputWidth; ow++) {
                        double grad = gradOutput[oc][oh][ow];

                        // カーネルの各位置について
                        for (int kh = 0; kh < kernelSize; kh++) {
                            for (int kw = 0; kw < kernelSize; kw++) {
                                // 対応する入力位置
                                int ih = oh * stride + kh;
                                int iw = ow * stride + kw;

                                // 勾配を伝播（重みを掛けて累積）
                                paddedGradInput[ic][ih][iw] += grad * weights[oc][ic][kh][kw];
                            }
                        }
                    }
                }
            }
        }

        // パディングを除去して最終的な入力勾配を作成
        double[][][] gradInput = new double[inChannels][input[0].length][input[0][0].length];
        for (int ic = 0; ic < inChannels; ic++) {
            for (int h = 0; h < input[0].length; h++) {
                for (int w = 0; w < input[0][0].length; w++) {
                    gradInput[ic][h][w] = paddedGradInput[ic][h + padding][w + padding];
                }
            }
        }

        return gradInput;
    }

    /**
     * Adamによる重み更新
     */
    public void updateWeights() {
        t++;
        double beta1 = 0.9, beta2 = 0.999, epsilon = 1e-8;

        // 重みの更新
        for (int oc = 0; oc < outChannels; oc++) {
            // バイアスの更新
            mBias[oc] = beta1 * mBias[oc] + (1 - beta1) * gradBias[oc];
            vBias[oc] = beta2 * vBias[oc] + (1 - beta2) * gradBias[oc] * gradBias[oc];
            double mHat = mBias[oc] / (1 - Math.pow(beta1, t));
            double vHat = vBias[oc] / (1 - Math.pow(beta2, t));
            bias[oc] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);

            // カーネル重みの更新
            for (int ic = 0; ic < inChannels; ic++) {
                for (int kh = 0; kh < kernelSize; kh++) {
                    for (int kw = 0; kw < kernelSize; kw++) {
                        double grad = gradWeights[oc][ic][kh][kw];

                        // モーメントの更新
                        mWeights[oc][ic][kh][kw] = beta1 * mWeights[oc][ic][kh][kw] + (1 - beta1) * grad;
                        vWeights[oc][ic][kh][kw] = beta2 * vWeights[oc][ic][kh][kw] + (1 - beta2) * grad * grad;

                        // バイアス補正
                        mHat = mWeights[oc][ic][kh][kw] / (1 - Math.pow(beta1, t));
                        vHat = vWeights[oc][ic][kh][kw] / (1 - Math.pow(beta2, t));

                        // 重みの更新
                        weights[oc][ic][kh][kw] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
                    }
                }
            }
        }

        // 勾配のクリア
        gradWeights = null;
        gradBias = null;
    }

    public void setLearningRate(double lr) {
        this.learningRate = lr;
    }

    // ゲッター（デバッグ用）
    public double[][][][] getWeights() { return weights; }
    public double[] getBias() { return bias; }
}