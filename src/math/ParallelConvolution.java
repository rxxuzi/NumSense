package math;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * 並列処理による高速畳み込み演算
 * ForkJoinPoolを使用してマルチコアCPUを活用
 */
public class ParallelConvolution {

    private static final ForkJoinPool pool = ForkJoinPool.commonPool();
    private static final int THRESHOLD = 16; // 並列化の閾値

    /**
     * 並列3D畳み込み演算
     */
    public static double[][][] convolve3DParallel(double[][][] input, double[][][][] kernels,
                                                  double[] bias, int stride, int padding) {
        int outputChannels = kernels.length;

        // 最初のチャンネルで出力サイズを計算
        double[][] firstPadded = ConvolutionOps.applyPadding(input[0], padding);
        int outputHeight = (firstPadded.length - kernels[0][0].length) / stride + 1;
        int outputWidth = (firstPadded[0].length - kernels[0][0][0].length) / stride + 1;

        double[][][] output = new double[outputChannels][outputHeight][outputWidth];

        // 各出力チャンネルを並列処理
        ConvolutionTask task = new ConvolutionTask(input, kernels, bias, stride, padding,
                output, 0, outputChannels);
        pool.invoke(task);

        return output;
    }

    /**
     * 畳み込みタスク（分割統治法）
     */
    private static class ConvolutionTask extends RecursiveTask<Void> {
        private final double[][][] input;
        private final double[][][][] kernels;
        private final double[] bias;
        private final int stride, padding;
        private final double[][][] output;
        private final int startChannel, endChannel;

        ConvolutionTask(double[][][] input, double[][][][] kernels, double[] bias,
                        int stride, int padding, double[][][] output,
                        int startChannel, int endChannel) {
            this.input = input;
            this.kernels = kernels;
            this.bias = bias;
            this.stride = stride;
            this.padding = padding;
            this.output = output;
            this.startChannel = startChannel;
            this.endChannel = endChannel;
        }

        @Override
        protected Void compute() {
            int numChannels = endChannel - startChannel;

            if (numChannels <= THRESHOLD) {
                // 直接計算
                computeDirectly();
            } else {
                // 分割して並列処理
                int mid = startChannel + numChannels / 2;

                ConvolutionTask leftTask = new ConvolutionTask(
                        input, kernels, bias, stride, padding, output, startChannel, mid);
                ConvolutionTask rightTask = new ConvolutionTask(
                        input, kernels, bias, stride, padding, output, mid, endChannel);

                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }

            return null;
        }

        private void computeDirectly() {
            int inputChannels = input.length;
            int outputHeight = output[0].length;
            int outputWidth = output[0][0].length;

            // パディングされた入力を事前に計算
            double[][][] paddedInput = new double[inputChannels][][];
            for (int ic = 0; ic < inputChannels; ic++) {
                paddedInput[ic] = ConvolutionOps.applyPadding(input[ic], padding);
            }

            // 各出力チャンネルについて
            for (int oc = startChannel; oc < endChannel; oc++) {
                // 出力位置の計算
                for (int oh = 0; oh < outputHeight; oh++) {
                    for (int ow = 0; ow < outputWidth; ow++) {
                        double sum = 0.0;

                        // 各入力チャンネルの畳み込み
                        for (int ic = 0; ic < inputChannels; ic++) {
                            sum += convolvePoint(paddedInput[ic], kernels[oc][ic],
                                    oh, ow, stride);
                        }

                        // バイアスを追加
                        if (bias != null) {
                            sum += bias[oc];
                        }

                        output[oc][oh][ow] = sum;
                    }
                }
            }
        }

        private double convolvePoint(double[][] input, double[][] kernel,
                                     int oh, int ow, int stride) {
            double sum = 0.0;
            int kernelHeight = kernel.length;
            int kernelWidth = kernel[0].length;

            for (int kh = 0; kh < kernelHeight; kh++) {
                for (int kw = 0; kw < kernelWidth; kw++) {
                    int ih = oh * stride + kh;
                    int iw = ow * stride + kw;
                    sum += input[ih][iw] * kernel[kh][kw];
                }
            }

            return sum;
        }
    }

    /**
     * 並列最大プーリング
     */
    public static double[][][] maxPool3DParallel(double[][][] input, int poolSize, int stride) {
        int channels = input.length;
        int outputHeight = (input[0].length - poolSize) / stride + 1;
        int outputWidth = (input[0][0].length - poolSize) / stride + 1;

        double[][][] output = new double[channels][outputHeight][outputWidth];

        // 各チャンネルを並列処理
        PoolingTask task = new PoolingTask(input, output, poolSize, stride, 0, channels);
        pool.invoke(task);

        return output;
    }

    /**
     * プーリングタスク
     */
    private static class PoolingTask extends RecursiveTask<Void> {
        private final double[][][] input;
        private final double[][][] output;
        private final int poolSize, stride;
        private final int startChannel, endChannel;

        PoolingTask(double[][][] input, double[][][] output, int poolSize, int stride,
                    int startChannel, int endChannel) {
            this.input = input;
            this.output = output;
            this.poolSize = poolSize;
            this.stride = stride;
            this.startChannel = startChannel;
            this.endChannel = endChannel;
        }

        @Override
        protected Void compute() {
            int numChannels = endChannel - startChannel;

            if (numChannels <= THRESHOLD) {
                // 直接計算
                for (int c = startChannel; c < endChannel; c++) {
                    output[c] = ConvolutionOps.maxPool2D(input[c], poolSize, stride);
                }
            } else {
                // 分割して並列処理
                int mid = startChannel + numChannels / 2;

                PoolingTask leftTask = new PoolingTask(
                        input, output, poolSize, stride, startChannel, mid);
                PoolingTask rightTask = new PoolingTask(
                        input, output, poolSize, stride, mid, endChannel);

                leftTask.fork();
                rightTask.compute();
                leftTask.join();
            }

            return null;
        }
    }

    /**
     * im2col変換を使った高速畳み込み
     * 畳み込みを行列積として計算
     */
    public static double[][][] convolve3DWithIm2col(double[][][] input, double[][][][] kernels,
                                                    double[] bias, int stride, int padding) {
        int inChannels = input.length;
        int outChannels = kernels.length;
        int kernelHeight = kernels[0][0].length;
        int kernelWidth = kernels[0][0][0].length;

        // 出力サイズを計算
        double[][] firstPadded = ConvolutionOps.applyPadding(input[0], padding);
        int outputHeight = (firstPadded.length - kernelHeight) / stride + 1;
        int outputWidth = (firstPadded[0].length - kernelWidth) / stride + 1;

        // im2col変換
        double[][] inputCol = im2colMultiChannel(input, kernelHeight, kernelWidth, stride, padding);

        // カーネルを行列形式に変換
        double[][] kernelMatrix = new double[outChannels][inChannels * kernelHeight * kernelWidth];
        for (int oc = 0; oc < outChannels; oc++) {
            int idx = 0;
            for (int ic = 0; ic < inChannels; ic++) {
                for (int kh = 0; kh < kernelHeight; kh++) {
                    for (int kw = 0; kw < kernelWidth; kw++) {
                        kernelMatrix[oc][idx++] = kernels[oc][ic][kh][kw];
                    }
                }
            }
        }

        // 行列積として計算
        double[][] outputMatrix = Matrix.dot(kernelMatrix, Matrix.t(inputCol));

        // バイアスを追加
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                for (int i = 0; i < outputMatrix[oc].length; i++) {
                    outputMatrix[oc][i] += bias[oc];
                }
            }
        }

        // 出力を3D形式に変換
        double[][][] output = new double[outChannels][outputHeight][outputWidth];
        for (int oc = 0; oc < outChannels; oc++) {
            int idx = 0;
            for (int oh = 0; oh < outputHeight; oh++) {
                for (int ow = 0; ow < outputWidth; ow++) {
                    output[oc][oh][ow] = outputMatrix[oc][idx++];
                }
            }
        }

        return output;
    }

    /**
     * マルチチャンネル版im2col
     */
    private static double[][] im2colMultiChannel(double[][][] input, int kernelHeight, int kernelWidth,
                                                 int stride, int padding) {
        int channels = input.length;
        int height = input[0].length;
        int width = input[0][0].length;

        // パディング後のサイズ
        int paddedHeight = height + 2 * padding;
        int paddedWidth = width + 2 * padding;

        // 出力サイズ
        int outputHeight = (paddedHeight - kernelHeight) / stride + 1;
        int outputWidth = (paddedWidth - kernelWidth) / stride + 1;
        int outputSize = outputHeight * outputWidth;
        int colSize = channels * kernelHeight * kernelWidth;

        double[][] col = new double[outputSize][colSize];

        // 各チャンネルについてim2col変換
        int outputIdx = 0;
        for (int oh = 0; oh < outputHeight; oh++) {
            for (int ow = 0; ow < outputWidth; ow++) {
                int colIdx = 0;

                for (int c = 0; c < channels; c++) {
                    for (int kh = 0; kh < kernelHeight; kh++) {
                        for (int kw = 0; kw < kernelWidth; kw++) {
                            int ih = oh * stride + kh - padding;
                            int iw = ow * stride + kw - padding;

                            // パディング領域の処理
                            if (ih >= 0 && ih < height && iw >= 0 && iw < width) {
                                col[outputIdx][colIdx] = input[c][ih][iw];
                            } else {
                                col[outputIdx][colIdx] = 0.0;
                            }
                            colIdx++;
                        }
                    }
                }
                outputIdx++;
            }
        }

        return col;
    }
}