package math;

/**
 * 3D/4Dテンソル演算クラス
 * 画像処理のための多次元配列操作
 */
public class Tensor {

    /**
     * 4Dテンソルを作成（画像バッチ用）
     * @param batch バッチサイズ
     * @param channels チャンネル数
     * @param height 高さ
     * @param width 幅
     * @return 初期化された4Dテンソル
     */
    public static double[][][][] zeros4D(int batch, int channels, int height, int width) {
        return new double[batch][channels][height][width];
    }

    /**
     * 3Dテンソルを作成（単一画像用）
     * @param channels チャンネル数
     * @param height 高さ
     * @param width 幅
     * @return 初期化された3Dテンソル
     */
    public static double[][][] zeros3D(int channels, int height, int width) {
        return new double[channels][height][width];
    }

    /**
     * 2D画像を3Dテンソルに変換（グレースケール）
     * @param image 2D画像配列
     * @return 3Dテンソル [1][H][W]
     */
    public static double[][][] fromGrayscale(double[][] image) {
        int height = image.length;
        int width = image[0].length;
        double[][][] tensor = new double[1][height][width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                tensor[0][h][w] = image[h][w];
            }
        }
        return tensor;
    }

    /**
     * 3Dテンソルを2D画像に変換（グレースケール）
     * @param tensor 3Dテンソル
     * @return 2D画像配列
     */
    public static double[][] toGrayscale(double[][][] tensor) {
        int height = tensor[0].length;
        int width = tensor[0][0].length;
        double[][] image = new double[height][width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                image[h][w] = tensor[0][h][w];
            }
        }
        return image;
    }

    /**
     * テンソルを1次元配列にフラット化
     * @param tensor 3Dテンソル
     * @return フラット化された配列
     */
    public static double[] flatten(double[][][] tensor) {
        int channels = tensor.length;
        int height = tensor[0].length;
        int width = tensor[0][0].length;
        double[] flat = new double[channels * height * width];

        int idx = 0;
        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    flat[idx++] = tensor[c][h][w];
                }
            }
        }
        return flat;
    }

    /**
     * 1次元配列を3Dテンソルに再形成
     * @param flat フラット配列
     * @param channels チャンネル数
     * @param height 高さ
     * @param width 幅
     * @return 3Dテンソル
     */
    public static double[][][] reshape(double[] flat, int channels, int height, int width) {
        double[][][] tensor = new double[channels][height][width];

        int idx = 0;
        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    tensor[c][h][w] = flat[idx++];
                }
            }
        }
        return tensor;
    }

    /**
     * テンソルのコピー
     * @param src ソーステンソル
     * @return コピーされたテンソル
     */
    public static double[][][] copy3D(double[][][] src) {
        int channels = src.length;
        int height = src[0].length;
        int width = src[0][0].length;
        double[][][] dst = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                System.arraycopy(src[c][h], 0, dst[c][h], 0, width);
            }
        }
        return dst;
    }

    /**
     * テンソルの正規化（0-1の範囲に）
     * @param tensor 入力テンソル
     * @return 正規化されたテンソル
     */
    public static double[][][] normalize(double[][][] tensor) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        // 最小値と最大値を見つける
        for (double[][] channel : tensor) {
            for (double[] row : channel) {
                for (double val : row) {
                    min = Math.min(min, val);
                    max = Math.max(max, val);
                }
            }
        }

        // 正規化
        double[][][] normalized = copy3D(tensor);
        double range = max - min;
        if (range > 0) {
            for (int c = 0; c < tensor.length; c++) {
                for (int h = 0; h < tensor[0].length; h++) {
                    for (int w = 0; w < tensor[0][0].length; w++) {
                        normalized[c][h][w] = (tensor[c][h][w] - min) / range;
                    }
                }
            }
        }

        return normalized;
    }

    /**
     * パディングを追加
     * @param tensor 入力テンソル
     * @param padding パディングサイズ
     * @return パディングされたテンソル
     */
    public static double[][][] pad(double[][][] tensor, int padding) {
        int channels = tensor.length;
        int height = tensor[0].length;
        int width = tensor[0][0].length;

        int newHeight = height + 2 * padding;
        int newWidth = width + 2 * padding;

        double[][][] padded = new double[channels][newHeight][newWidth];

        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    padded[c][h + padding][w + padding] = tensor[c][h][w];
                }
            }
        }

        return padded;
    }

    /**
     * 畳み込み演算（シンプル版）
     * @param input 入力テンソル [C_in][H][W]
     * @param kernel カーネル [C_out][C_in][K_h][K_w]
     * @param stride ストライド
     * @return 出力テンソル [C_out][H_out][W_out]
     */
    public static double[][][] convolve(double[][][] input, double[][][][] kernel, int stride) {
        int inChannels = input.length;
        int inHeight = input[0].length;
        int inWidth = input[0][0].length;

        int outChannels = kernel.length;
        int kernelHeight = kernel[0][0].length;
        int kernelWidth = kernel[0][0][0].length;

        int outHeight = (inHeight - kernelHeight) / stride + 1;
        int outWidth = (inWidth - kernelWidth) / stride + 1;

        double[][][] output = new double[outChannels][outHeight][outWidth];

        // 各出力チャンネルについて
        for (int oc = 0; oc < outChannels; oc++) {
            // 出力位置
            for (int oh = 0; oh < outHeight; oh++) {
                for (int ow = 0; ow < outWidth; ow++) {
                    double sum = 0.0;

                    // 畳み込み計算
                    for (int ic = 0; ic < inChannels; ic++) {
                        for (int kh = 0; kh < kernelHeight; kh++) {
                            for (int kw = 0; kw < kernelWidth; kw++) {
                                int ih = oh * stride + kh;
                                int iw = ow * stride + kw;
                                sum += input[ic][ih][iw] * kernel[oc][ic][kh][kw];
                            }
                        }
                    }

                    output[oc][oh][ow] = sum;
                }
            }
        }

        return output;
    }

    /**
     * 最大プーリング
     * @param input 入力テンソル
     * @param poolSize プーリングサイズ
     * @param stride ストライド
     * @return プーリング後のテンソル
     */
    public static double[][][] maxPool(double[][][] input, int poolSize, int stride) {
        int channels = input.length;
        int inHeight = input[0].length;
        int inWidth = input[0][0].length;

        int outHeight = (inHeight - poolSize) / stride + 1;
        int outWidth = (inWidth - poolSize) / stride + 1;

        double[][][] output = new double[channels][outHeight][outWidth];

        for (int c = 0; c < channels; c++) {
            for (int oh = 0; oh < outHeight; oh++) {
                for (int ow = 0; ow < outWidth; ow++) {
                    double maxVal = Double.NEGATIVE_INFINITY;

                    // プーリング領域の最大値を見つける
                    for (int ph = 0; ph < poolSize; ph++) {
                        for (int pw = 0; pw < poolSize; pw++) {
                            int ih = oh * stride + ph;
                            int iw = ow * stride + pw;
                            maxVal = Math.max(maxVal, input[c][ih][iw]);
                        }
                    }

                    output[c][oh][ow] = maxVal;
                }
            }
        }

        return output;
    }

    /**
     * アップサンプリング（最近傍補間）
     * @param input 入力テンソル
     * @param scale スケール倍率
     * @return アップサンプリング後のテンソル
     */
    public static double[][][] upsample(double[][][] input, int scale) {
        int channels = input.length;
        int inHeight = input[0].length;
        int inWidth = input[0][0].length;

        int outHeight = inHeight * scale;
        int outWidth = inWidth * scale;

        double[][][] output = new double[channels][outHeight][outWidth];

        for (int c = 0; c < channels; c++) {
            for (int oh = 0; oh < outHeight; oh++) {
                for (int ow = 0; ow < outWidth; ow++) {
                    int ih = oh / scale;
                    int iw = ow / scale;
                    output[c][oh][ow] = input[c][ih][iw];
                }
            }
        }

        return output;
    }

    /**
     * テンソルの要素ごとの演算（加算）
     * @param a テンソルA
     * @param b テンソルB
     * @return A + B
     */
    public static double[][][] add(double[][][] a, double[][][] b) {
        int channels = a.length;
        int height = a[0].length;
        int width = a[0][0].length;

        double[][][] result = new double[channels][height][width];

        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    result[c][h][w] = a[c][h][w] + b[c][h][w];
                }
            }
        }

        return result;
    }

    /**
     * テンソルの要素ごとの演算（乗算）
     * @param tensor テンソル
     * @param scalar スカラー値
     * @return tensor * scalar
     */
    public static double[][][] multiply(double[][][] tensor, double scalar) {
        double[][][] result = copy3D(tensor);

        for (int c = 0; c < tensor.length; c++) {
            for (int h = 0; h < tensor[0].length; h++) {
                for (int w = 0; w < tensor[0][0].length; w++) {
                    result[c][h][w] *= scalar;
                }
            }
        }

        return result;
    }
}