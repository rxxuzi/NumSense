package models;

import alg.CrossEntropy;
import alg.ReLU;
import alg.Softmax;
import math.ConvolutionOps;
import math.Matrix;
import math.Tensor;

import java.util.Random;

/**
 * 改良版CNNモデル
 * - 完全な逆伝播実装
 * - データ拡張
 * - 学習率減衰
 * - ドロップアウト
 */
public class ImprovedCNN {

    // 層のパラメータ
    private final ConvLayer conv1;
    private final ConvLayer conv2;
    private final FullyConnectedLayer fc1;
    private final FullyConnectedLayer fc2;

    // ハイパーパラメータ
    private double initialLearningRate;
    private double currentLearningRate;
    private double dropoutRate = 0.5;
    private int epoch = 0;
    private boolean isTraining = true;

    private final Random random;

    public ImprovedCNN(double learningRate) {
        this.initialLearningRate = learningRate;
        this.currentLearningRate = learningRate;
        this.random = new Random(42);

        // 層の初期化
        conv1 = new ConvLayer(1, 16, 3, 1, 1, learningRate);    // 入力1ch、出力16ch、3x3カーネル
        conv2 = new ConvLayer(16, 32, 3, 1, 1, learningRate);   // 入力16ch、出力32ch、3x3カーネル
        fc1 = new FullyConnectedLayer(32 * 8 * 8, 128, learningRate);
        fc2 = new FullyConnectedLayer(128, 10, learningRate);
    }

    /**
     * 順伝播
     */
    public double[] forward(double[][][] input) {
        // Conv1 -> ReLU -> Pool
        double[][][] conv1Out = conv1.forward(input);
        double[][][] relu1Out = ReLU.apply(conv1Out);
        double[][][] pool1Out = ConvolutionOps.maxPool3D(relu1Out, 2, 2);

        // Conv2 -> ReLU -> Pool
        double[][][] conv2Out = conv2.forward(pool1Out);
        double[][][] relu2Out = ReLU.apply(conv2Out);
        double[][][] pool2Out = ConvolutionOps.maxPool3D(relu2Out, 2, 2);

        // Flatten
        double[] flattened = Tensor.flatten(pool2Out);

        // FC1 -> ReLU -> Dropout
        double[] fc1Out = fc1.forward(flattened);
        double[] relu3Out = ReLU.apply(fc1Out);
        double[] dropped = applyDropout(relu3Out);

        // FC2 -> Softmax
        double[] fc2Out = fc2.forward(dropped);
        return Softmax.apply(fc2Out);
    }

    /**
     * 学習（順伝播と逆伝播）
     */
    public double train(double[][][] input, int targetClass) {
        isTraining = true;

        // 順伝播（中間結果を保存）
        // Conv1
        double[][][] conv1Out = conv1.forward(input);
        double[][][] relu1Out = ReLU.apply(conv1Out);
        double[][][] pool1Out = maxPoolForward(relu1Out, 2, 2);
        int[][][] pool1Indices = lastPoolIndices;

        // Conv2
        double[][][] conv2Out = conv2.forward(pool1Out);
        double[][][] relu2Out = ReLU.apply(conv2Out);
        double[][][] pool2Out = maxPoolForward(relu2Out, 2, 2);
        int[][][] pool2Indices = lastPoolIndices;

        // Flatten
        double[] flattened = Tensor.flatten(pool2Out);

        // FC1
        double[] fc1Out = fc1.forward(flattened);
        double[] relu3Out = ReLU.apply(fc1Out);
        double[] dropped = applyDropout(relu3Out);
        boolean[] dropoutMask = lastDropoutMask;

        // FC2
        double[] fc2Out = fc2.forward(dropped);
        double[] probabilities = Softmax.apply(fc2Out);

        // 損失計算
        double loss = CrossEntropy.calculate(probabilities, targetClass);

        // 逆伝播
        // Softmax + CrossEntropyの勾配
        double[] gradOutput = Softmax.gradientWithCrossEntropy(probabilities, targetClass);

        // FC2の逆伝播
        double[] gradFC1 = fc2.backward(gradOutput, dropped);

        // Dropoutの逆伝播
        double[] gradDropout = applyDropoutBackward(gradFC1, dropoutMask);

        // ReLU (FC1後)の逆伝播
        double[] gradReLU3 = new double[fc1Out.length];
        for (int i = 0; i < fc1Out.length; i++) {
            gradReLU3[i] = fc1Out[i] > 0 ? gradDropout[i] : 0;
        }

        // FC1の逆伝播
        double[] gradFlatten = fc1.backward(gradReLU3, flattened);

        // Unflatten
        double[][][] gradPool2 = Tensor.reshape(gradFlatten, 32, 7, 7);

        // MaxPool2の逆伝播
        double[][][] gradReLU2 = maxPoolBackward(gradPool2, pool2Indices, 2, 2, relu2Out);

        // ReLU (Conv2後)の逆伝播
        double[][][] gradConv2 = new double[32][][];
        for (int c = 0; c < 32; c++) {
            gradConv2[c] = new double[14][14];
            for (int h = 0; h < 14; h++) {
                for (int w = 0; w < 14; w++) {
                    gradConv2[c][h][w] = conv2Out[c][h][w] > 0 ? gradReLU2[c][h][w] : 0;
                }
            }
        }

        // Conv2の逆伝播
        double[][][] gradPool1 = conv2.backward(gradConv2, pool1Out);

        // MaxPool1の逆伝播
        double[][][] gradReLU1 = maxPoolBackward(gradPool1, pool1Indices, 2, 2, relu1Out);

        // ReLU (Conv1後)の逆伝播
        double[][][] gradConv1 = new double[16][][];
        for (int c = 0; c < 16; c++) {
            gradConv1[c] = new double[28][28];
            for (int h = 0; h < 28; h++) {
                for (int w = 0; w < 28; w++) {
                    gradConv1[c][h][w] = conv1Out[c][h][w] > 0 ? gradReLU1[c][h][w] : 0;
                }
            }
        }

        // Conv1の逆伝播
        conv1.backward(gradConv1, input);

        // 重みの更新
        conv1.updateWeights();
        conv2.updateWeights();
        fc1.updateWeights();
        fc2.updateWeights();

        return loss;
    }

    /**
     * 予測
     */
    public int predict(double[][][] input) {
        isTraining = false;
        double[] probabilities = forward(input);
        return Softmax.argmax(probabilities);
    }

    /**
     * エポック終了時の処理
     */
    public void endEpoch() {
        epoch++;
        // 学習率の減衰（10エポックごとに0.9倍）
        if (epoch % 10 == 0) {
            currentLearningRate *= 0.9;
            conv1.setLearningRate(currentLearningRate);
            conv2.setLearningRate(currentLearningRate);
            fc1.setLearningRate(currentLearningRate);
            fc2.setLearningRate(currentLearningRate);
        }
    }

    // 補助メソッド
    private int[][][] lastPoolIndices;

    private double[][][] maxPoolForward(double[][][] input, int poolSize, int stride) {
        int channels = input.length;
        int inputHeight = input[0].length;
        int inputWidth = input[0][0].length;
        int outputHeight = (inputHeight - poolSize) / stride + 1;
        int outputWidth = (inputWidth - poolSize) / stride + 1;

        double[][][] output = new double[channels][outputHeight][outputWidth];
        lastPoolIndices = new int[channels][outputHeight][outputWidth];

        for (int c = 0; c < channels; c++) {
            for (int oh = 0; oh < outputHeight; oh++) {
                for (int ow = 0; ow < outputWidth; ow++) {
                    double maxVal = Double.NEGATIVE_INFINITY;
                    int maxIdx = 0;

                    for (int ph = 0; ph < poolSize; ph++) {
                        for (int pw = 0; pw < poolSize; pw++) {
                            int ih = oh * stride + ph;
                            int iw = ow * stride + pw;
                            if (input[c][ih][iw] > maxVal) {
                                maxVal = input[c][ih][iw];
                                maxIdx = ph * poolSize + pw;
                            }
                        }
                    }

                    output[c][oh][ow] = maxVal;
                    lastPoolIndices[c][oh][ow] = maxIdx;
                }
            }
        }

        return output;
    }

    private double[][][] maxPoolBackward(double[][][] gradOutput, int[][][] indices,
                                         int poolSize, int stride, double[][][] originalInput) {
        int channels = gradOutput.length;
        int outputHeight = gradOutput[0].length;
        int outputWidth = gradOutput[0][0].length;
        int inputHeight = originalInput[0].length;
        int inputWidth = originalInput[0][0].length;

        double[][][] gradInput = new double[channels][inputHeight][inputWidth];

        for (int c = 0; c < channels; c++) {
            for (int oh = 0; oh < outputHeight; oh++) {
                for (int ow = 0; ow < outputWidth; ow++) {
                    int maxIdx = indices[c][oh][ow];
                    int ph = maxIdx / poolSize;
                    int pw = maxIdx % poolSize;
                    int ih = oh * stride + ph;
                    int iw = ow * stride + pw;

                    gradInput[c][ih][iw] += gradOutput[c][oh][ow];
                }
            }
        }

        return gradInput;
    }

    private boolean[] lastDropoutMask;

    private double[] applyDropout(double[] input) {
        if (!isTraining || dropoutRate == 0) {
            return input;
        }

        double[] output = new double[input.length];
        lastDropoutMask = new boolean[input.length];
        double scale = 1.0 / (1.0 - dropoutRate);

        for (int i = 0; i < input.length; i++) {
            if (random.nextDouble() > dropoutRate) {
                output[i] = input[i] * scale;
                lastDropoutMask[i] = true;
            } else {
                output[i] = 0;
                lastDropoutMask[i] = false;
            }
        }

        return output;
    }

    private double[] applyDropoutBackward(double[] gradOutput, boolean[] mask) {
        if (!isTraining || dropoutRate == 0) {
            return gradOutput;
        }

        double[] gradInput = new double[gradOutput.length];
        double scale = 1.0 / (1.0 - dropoutRate);

        for (int i = 0; i < gradOutput.length; i++) {
            if (mask[i]) {
                gradInput[i] = gradOutput[i] * scale;
            }
        }

        return gradInput;
    }

    /**
     * データ拡張
     */
    public static double[][][] augmentImage(double[][][] image, Random rand) {
        double[][][] augmented = image;

        // ランダムな回転（-15度から+15度）
        if (rand.nextBoolean()) {
            double angle = (rand.nextDouble() - 0.5) * 30 * Math.PI / 180;
            augmented = rotateImage(augmented, angle);
        }

        // ランダムなシフト（-2から+2ピクセル）
        if (rand.nextBoolean()) {
            int shiftX = rand.nextInt(5) - 2;
            int shiftY = rand.nextInt(5) - 2;
            augmented = shiftImage(augmented, shiftX, shiftY);
        }

        // ランダムなノイズ
        if (rand.nextBoolean()) {
            augmented = addNoise(augmented, 0.1, rand);
        }

        return augmented;
    }

    private static double[][][] rotateImage(double[][][] image, double angle) {
        int channels = image.length;
        int height = image[0].length;
        int width = image[0][0].length;
        double[][][] rotated = new double[channels][height][width];

        int centerY = height / 2;
        int centerX = width / 2;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int c = 0; c < channels; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int dy = y - centerY;
                    int dx = x - centerX;
                    int srcY = (int)(cos * dy + sin * dx + centerY);
                    int srcX = (int)(-sin * dy + cos * dx + centerX);

                    if (srcY >= 0 && srcY < height && srcX >= 0 && srcX < width) {
                        rotated[c][y][x] = image[c][srcY][srcX];
                    }
                }
            }
        }

        return rotated;
    }

    private static double[][][] shiftImage(double[][][] image, int shiftX, int shiftY) {
        int channels = image.length;
        int height = image[0].length;
        int width = image[0][0].length;
        double[][][] shifted = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int srcY = y - shiftY;
                    int srcX = x - shiftX;

                    if (srcY >= 0 && srcY < height && srcX >= 0 && srcX < width) {
                        shifted[c][y][x] = image[c][srcY][srcX];
                    }
                }
            }
        }

        return shifted;
    }

    private static double[][][] addNoise(double[][][] image, double noiseLevel, Random rand) {
        int channels = image.length;
        int height = image[0].length;
        int width = image[0][0].length;
        double[][][] noisy = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double noise = (rand.nextDouble() - 0.5) * noiseLevel;
                    noisy[c][y][x] = Math.max(0, Math.min(1, image[c][y][x] + noise));
                }
            }
        }

        return noisy;
    }
}

