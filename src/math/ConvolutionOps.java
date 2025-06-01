package math;

/**
 * 畳み込みニューラルネットワーク用の演算
 */
public class ConvolutionOps {

    /**
     * 2D畳み込み演算（単一チャンネル）
     * @param input 入力画像 [height][width]
     * @param kernel カーネル [kernelHeight][kernelWidth]
     * @param stride ストライド
     * @param padding パディング
     * @return 畳み込み結果
     */
    public static double[][] convolve2D(double[][] input, double[][] kernel,
                                        int stride, int padding) {
        // パディングを適用
        double[][] paddedInput = applyPadding(input, padding);

        int inputHeight = paddedInput.length;
        int inputWidth = paddedInput[0].length;
        int kernelHeight = kernel.length;
        int kernelWidth = kernel[0].length;

        // 出力サイズを計算
        int outputHeight = (inputHeight - kernelHeight) / stride + 1;
        int outputWidth = (inputWidth - kernelWidth) / stride + 1;

        double[][] output = new double[outputHeight][outputWidth];

        // 畳み込み演算
        for (int oh = 0; oh < outputHeight; oh++) {
            for (int ow = 0; ow < outputWidth; ow++) {
                double sum = 0.0;

                // カーネルの各要素について
                for (int kh = 0; kh < kernelHeight; kh++) {
                    for (int kw = 0; kw < kernelWidth; kw++) {
                        int ih = oh * stride + kh;
                        int iw = ow * stride + kw;
                        sum += paddedInput[ih][iw] * kernel[kh][kw];
                    }
                }

                output[oh][ow] = sum;
            }
        }

        return output;
    }

    /**
     * 3D畳み込み演算（複数チャンネル）
     * @param input 入力テンソル [channels][height][width]
     * @param kernels カーネル [outputChannels][inputChannels][kernelHeight][kernelWidth]
     * @param bias バイアス [outputChannels]
     * @param stride ストライド
     * @param padding パディング
     * @return 畳み込み結果 [outputChannels][outputHeight][outputWidth]
     */
    public static double[][][] convolve3D(double[][][] input, double[][][][] kernels,
                                          double[] bias, int stride, int padding) {
        int inputChannels = input.length;
        int outputChannels = kernels.length;

        // 最初のチャンネルで出力サイズを計算
        double[][] firstPadded = applyPadding(input[0], padding);
        int outputHeight = (firstPadded.length - kernels[0][0].length) / stride + 1;
        int outputWidth = (firstPadded[0].length - kernels[0][0][0].length) / stride + 1;

        double[][][] output = new double[outputChannels][outputHeight][outputWidth];

        // 各出力チャンネルについて
        for (int oc = 0; oc < outputChannels; oc++) {
            // 各入力チャンネルの畳み込みを累積
            for (int ic = 0; ic < inputChannels; ic++) {
                double[][] conv = convolve2D(input[ic], kernels[oc][ic], stride, padding);

                // 結果を累積
                for (int h = 0; h < outputHeight; h++) {
                    for (int w = 0; w < outputWidth; w++) {
                        output[oc][h][w] += conv[h][w];
                    }
                }
            }

            // バイアスを追加
            if (bias != null) {
                for (int h = 0; h < outputHeight; h++) {
                    for (int w = 0; w < outputWidth; w++) {
                        output[oc][h][w] += bias[oc];
                    }
                }
            }
        }

        return output;
    }

    /**
     * 最大プーリング
     * @param input 入力画像 [height][width]
     * @param poolSize プーリングサイズ
     * @param stride ストライド
     * @return プーリング結果
     */
    public static double[][] maxPool2D(double[][] input, int poolSize, int stride) {
        int inputHeight = input.length;
        int inputWidth = input[0].length;

        int outputHeight = (inputHeight - poolSize) / stride + 1;
        int outputWidth = (inputWidth - poolSize) / stride + 1;

        double[][] output = new double[outputHeight][outputWidth];

        for (int oh = 0; oh < outputHeight; oh++) {
            for (int ow = 0; ow < outputWidth; ow++) {
                double maxVal = Double.NEGATIVE_INFINITY;

                // プーリング領域の最大値を見つける
                for (int ph = 0; ph < poolSize; ph++) {
                    for (int pw = 0; pw < poolSize; pw++) {
                        int ih = oh * stride + ph;
                        int iw = ow * stride + pw;
                        maxVal = Math.max(maxVal, input[ih][iw]);
                    }
                }

                output[oh][ow] = maxVal;
            }
        }

        return output;
    }

    /**
     * 3D最大プーリング（複数チャンネル）
     * @param input 入力テンソル [channels][height][width]
     * @param poolSize プーリングサイズ
     * @param stride ストライド
     * @return プーリング結果
     */
    public static double[][][] maxPool3D(double[][][] input, int poolSize, int stride) {
        int channels = input.length;
        double[][][] output = new double[channels][][];

        // 各チャンネルに対して独立にプーリング
        for (int c = 0; c < channels; c++) {
            output[c] = maxPool2D(input[c], poolSize, stride);
        }

        return output;
    }

    /**
     * パディングを適用
     * @param input 入力画像
     * @param padding パディングサイズ
     * @return パディングされた画像
     */
    public static double[][] applyPadding(double[][] input, int padding) {
        if (padding == 0) {
            return input;
        }

        int height = input.length;
        int width = input[0].length;
        int paddedHeight = height + 2 * padding;
        int paddedWidth = width + 2 * padding;

        double[][] padded = new double[paddedHeight][paddedWidth];

        // 元の画像をコピー
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                padded[h + padding][w + padding] = input[h][w];
            }
        }

        return padded;
    }

    /**
     * im2col変換（畳み込みを行列積として計算するための変換）
     * @param input 入力画像 [height][width]
     * @param kernelHeight カーネルの高さ
     * @param kernelWidth カーネルの幅
     * @param stride ストライド
     * @param padding パディング
     * @return 変換後の行列 [outputSize][kernelSize]
     */
    public static double[][] im2col(double[][] input, int kernelHeight, int kernelWidth,
                                    int stride, int padding) {
        double[][] padded = applyPadding(input, padding);
        int inputHeight = padded.length;
        int inputWidth = padded[0].length;

        int outputHeight = (inputHeight - kernelHeight) / stride + 1;
        int outputWidth = (inputWidth - kernelWidth) / stride + 1;
        int outputSize = outputHeight * outputWidth;
        int kernelSize = kernelHeight * kernelWidth;

        double[][] col = new double[outputSize][kernelSize];

        int outputIdx = 0;
        for (int oh = 0; oh < outputHeight; oh++) {
            for (int ow = 0; ow < outputWidth; ow++) {
                int kernelIdx = 0;

                for (int kh = 0; kh < kernelHeight; kh++) {
                    for (int kw = 0; kw < kernelWidth; kw++) {
                        int ih = oh * stride + kh;
                        int iw = ow * stride + kw;
                        col[outputIdx][kernelIdx] = padded[ih][iw];
                        kernelIdx++;
                    }
                }

                outputIdx++;
            }
        }

        return col;
    }

    /**
     * col2im変換（im2colの逆変換）
     * @param col 列形式のデータ
     * @param inputHeight 元の画像の高さ
     * @param inputWidth 元の画像の幅
     * @param kernelHeight カーネルの高さ
     * @param kernelWidth カーネルの幅
     * @param stride ストライド
     * @param padding パディング
     * @return 画像形式のデータ
     */
    public static double[][] col2im(double[][] col, int inputHeight, int inputWidth,
                                    int kernelHeight, int kernelWidth,
                                    int stride, int padding) {
        int paddedHeight = inputHeight + 2 * padding;
        int paddedWidth = inputWidth + 2 * padding;
        double[][] padded = new double[paddedHeight][paddedWidth];

        int outputHeight = (paddedHeight - kernelHeight) / stride + 1;
        int outputWidth = (paddedWidth - kernelWidth) / stride + 1;

        int outputIdx = 0;
        for (int oh = 0; oh < outputHeight; oh++) {
            for (int ow = 0; ow < outputWidth; ow++) {
                int kernelIdx = 0;

                for (int kh = 0; kh < kernelHeight; kh++) {
                    for (int kw = 0; kw < kernelWidth; kw++) {
                        int ih = oh * stride + kh;
                        int iw = ow * stride + kw;
                        padded[ih][iw] += col[outputIdx][kernelIdx];
                        kernelIdx++;
                    }
                }

                outputIdx++;
            }
        }

        // パディングを除去
        if (padding > 0) {
            double[][] result = new double[inputHeight][inputWidth];
            for (int h = 0; h < inputHeight; h++) {
                System.arraycopy(padded[h + padding], padding, result[h], 0, inputWidth);
            }
            return result;
        }

        return padded;
    }
}