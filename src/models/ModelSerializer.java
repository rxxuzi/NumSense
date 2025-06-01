package models;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CNN モデルのシリアライズ/デシリアライズ
 * 独自の軽量フォーマット .jnn (Java Neural Network)
 */
public class ModelSerializer {

    // ファイルフォーマットのマジックナンバー
    private static final int MAGIC_NUMBER = 0x4A4E4E31; // "JNN1"
    private static final int VERSION = 1;

    /**
     * モデルを保存
     */
    public static void saveModel(ImprovedCNN model, String filepath) throws IOException {
        Path outputPath = Paths.get(filepath);
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filepath)))) {

            // ヘッダー
            out.writeInt(MAGIC_NUMBER);
            out.writeInt(VERSION);

            // モデル構造情報
            ModelStructure structure = extractStructure(model);
            writeStructure(out, structure);

            // 重みとバイアス
            ModelWeights weights = extractWeights(model);
            writeWeights(out, weights);

            System.out.println("Model saved to: " + filepath);
        }
    }

    /**
     * モデルを読み込み
     */
    public static ImprovedCNN loadModel(String filepath) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(filepath)))) {

            // ヘッダーチェック
            int magic = in.readInt();
            if (magic != MAGIC_NUMBER) {
                throw new IOException("Invalid file format");
            }

            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("Unsupported version: " + version);
            }

            // モデル構造を読み込み
            ModelStructure structure = readStructure(in);

            // 新しいモデルインスタンスを作成
            ImprovedCNN model = new ImprovedCNN(structure.learningRate);

            // 重みとバイアスを読み込み
            ModelWeights weights = readWeights(in, structure);
            applyWeights(model, weights);

            System.out.println("Model loaded from: " + filepath);
            return model;
        }
    }

    /**
     * モデル構造を抽出（リフレクションを使用）
     */
    private static ModelStructure extractStructure(ImprovedCNN model) {
        // ImprovedCNNの構造は固定なので、ハードコード
        ModelStructure structure = new ModelStructure();
        structure.learningRate = 0.001; // デフォルト値

        // Conv層の構造
        structure.conv1 = new ConvLayerStructure(1, 16, 3, 1, 1);
        structure.conv2 = new ConvLayerStructure(16, 32, 3, 1, 1);

        // FC層の構造
        structure.fc1 = new FCLayerStructure(32 * 8 * 8, 128);
        structure.fc2 = new FCLayerStructure(128, 10);

        return structure;
    }

    /**
     * モデルの重みを抽出（リフレクションを使用）
     */
    private static ModelWeights extractWeights(ImprovedCNN model) {
        try {
            ModelWeights weights = new ModelWeights();

            // プライベートフィールドにアクセス
            java.lang.reflect.Field conv1Field = ImprovedCNN.class.getDeclaredField("conv1");
            conv1Field.setAccessible(true);
            ConvLayer conv1 = (ConvLayer) conv1Field.get(model);

            java.lang.reflect.Field conv2Field = ImprovedCNN.class.getDeclaredField("conv2");
            conv2Field.setAccessible(true);
            ConvLayer conv2 = (ConvLayer) conv2Field.get(model);

            java.lang.reflect.Field fc1Field = ImprovedCNN.class.getDeclaredField("fc1");
            fc1Field.setAccessible(true);
            FullyConnectedLayer fc1 = (FullyConnectedLayer) fc1Field.get(model);

            java.lang.reflect.Field fc2Field = ImprovedCNN.class.getDeclaredField("fc2");
            fc2Field.setAccessible(true);
            FullyConnectedLayer fc2 = (FullyConnectedLayer) fc2Field.get(model);

            // Conv層の重みを抽出
            weights.conv1Weights = extractConvWeights(conv1);
            weights.conv1Bias = extractConvBias(conv1);
            weights.conv2Weights = extractConvWeights(conv2);
            weights.conv2Bias = extractConvBias(conv2);

            // FC層の重みを抽出
            weights.fc1Weights = extractFCWeights(fc1);
            weights.fc1Bias = extractFCBias(fc1);
            weights.fc2Weights = extractFCWeights(fc2);
            weights.fc2Bias = extractFCBias(fc2);

            return weights;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract weights", e);
        }
    }

    /**
     * Conv層の重みを抽出
     */
    private static double[][][][] extractConvWeights(ConvLayer layer) throws Exception {
        java.lang.reflect.Field weightsField = ConvLayer.class.getDeclaredField("weights");
        weightsField.setAccessible(true);
        return (double[][][][]) weightsField.get(layer);
    }

    /**
     * Conv層のバイアスを抽出
     */
    private static double[] extractConvBias(ConvLayer layer) throws Exception {
        java.lang.reflect.Field biasField = ConvLayer.class.getDeclaredField("bias");
        biasField.setAccessible(true);
        return (double[]) biasField.get(layer);
    }

    /**
     * FC層の重みを抽出
     */
    private static double[][] extractFCWeights(FullyConnectedLayer layer) throws Exception {
        java.lang.reflect.Field weightsField = FullyConnectedLayer.class.getDeclaredField("weights");
        weightsField.setAccessible(true);
        return (double[][]) weightsField.get(layer);
    }

    /**
     * FC層のバイアスを抽出
     */
    private static double[] extractFCBias(FullyConnectedLayer layer) throws Exception {
        java.lang.reflect.Field biasField = FullyConnectedLayer.class.getDeclaredField("bias");
        biasField.setAccessible(true);
        return (double[]) biasField.get(layer);
    }

    /**
     * 重みを適用
     */
    private static void applyWeights(ImprovedCNN model, ModelWeights weights) {
        try {
            // プライベートフィールドにアクセス
            java.lang.reflect.Field conv1Field = ImprovedCNN.class.getDeclaredField("conv1");
            conv1Field.setAccessible(true);
            ConvLayer conv1 = (ConvLayer) conv1Field.get(model);

            java.lang.reflect.Field conv2Field = ImprovedCNN.class.getDeclaredField("conv2");
            conv2Field.setAccessible(true);
            ConvLayer conv2 = (ConvLayer) conv2Field.get(model);

            java.lang.reflect.Field fc1Field = ImprovedCNN.class.getDeclaredField("fc1");
            fc1Field.setAccessible(true);
            FullyConnectedLayer fc1 = (FullyConnectedLayer) fc1Field.get(model);

            java.lang.reflect.Field fc2Field = ImprovedCNN.class.getDeclaredField("fc2");
            fc2Field.setAccessible(true);
            FullyConnectedLayer fc2 = (FullyConnectedLayer) fc2Field.get(model);

            // 重みを設定
            setConvWeights(conv1, weights.conv1Weights, weights.conv1Bias);
            setConvWeights(conv2, weights.conv2Weights, weights.conv2Bias);
            setFCWeights(fc1, weights.fc1Weights, weights.fc1Bias);
            setFCWeights(fc2, weights.fc2Weights, weights.fc2Bias);

        } catch (Exception e) {
            throw new RuntimeException("Failed to apply weights", e);
        }
    }

    private static void setConvWeights(ConvLayer layer, double[][][][] weights, double[] bias) throws Exception {
        java.lang.reflect.Field weightsField = ConvLayer.class.getDeclaredField("weights");
        weightsField.setAccessible(true);
        weightsField.set(layer, weights);

        java.lang.reflect.Field biasField = ConvLayer.class.getDeclaredField("bias");
        biasField.setAccessible(true);
        biasField.set(layer, bias);
    }

    private static void setFCWeights(FullyConnectedLayer layer, double[][] weights, double[] bias) throws Exception {
        java.lang.reflect.Field weightsField = FullyConnectedLayer.class.getDeclaredField("weights");
        weightsField.setAccessible(true);
        weightsField.set(layer, weights);

        java.lang.reflect.Field biasField = FullyConnectedLayer.class.getDeclaredField("bias");
        biasField.setAccessible(true);
        biasField.set(layer, bias);
    }

    // 構造情報の書き込み/読み込み
    private static void writeStructure(DataOutputStream out, ModelStructure structure) throws IOException {
        out.writeDouble(structure.learningRate);

        // Conv1
        out.writeInt(structure.conv1.inChannels);
        out.writeInt(structure.conv1.outChannels);
        out.writeInt(structure.conv1.kernelSize);
        out.writeInt(structure.conv1.stride);
        out.writeInt(structure.conv1.padding);

        // Conv2
        out.writeInt(structure.conv2.inChannels);
        out.writeInt(structure.conv2.outChannels);
        out.writeInt(structure.conv2.kernelSize);
        out.writeInt(structure.conv2.stride);
        out.writeInt(structure.conv2.padding);

        // FC1
        out.writeInt(structure.fc1.inputSize);
        out.writeInt(structure.fc1.outputSize);

        // FC2
        out.writeInt(structure.fc2.inputSize);
        out.writeInt(structure.fc2.outputSize);
    }

    private static ModelStructure readStructure(DataInputStream in) throws IOException {
        ModelStructure structure = new ModelStructure();
        structure.learningRate = in.readDouble();

        // Conv1
        structure.conv1 = new ConvLayerStructure(
                in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt()
        );

        // Conv2
        structure.conv2 = new ConvLayerStructure(
                in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt()
        );

        // FC1
        structure.fc1 = new FCLayerStructure(in.readInt(), in.readInt());

        // FC2
        structure.fc2 = new FCLayerStructure(in.readInt(), in.readInt());

        return structure;
    }

    // 重みの書き込み/読み込み
    private static void writeWeights(DataOutputStream out, ModelWeights weights) throws IOException {
        // Conv1
        write4DArray(out, weights.conv1Weights);
        write1DArray(out, weights.conv1Bias);

        // Conv2
        write4DArray(out, weights.conv2Weights);
        write1DArray(out, weights.conv2Bias);

        // FC1
        write2DArray(out, weights.fc1Weights);
        write1DArray(out, weights.fc1Bias);

        // FC2
        write2DArray(out, weights.fc2Weights);
        write1DArray(out, weights.fc2Bias);
    }

    private static ModelWeights readWeights(DataInputStream in, ModelStructure structure) throws IOException {
        ModelWeights weights = new ModelWeights();

        // Conv1
        weights.conv1Weights = read4DArray(in, structure.conv1.outChannels,
                structure.conv1.inChannels, structure.conv1.kernelSize, structure.conv1.kernelSize);
        weights.conv1Bias = read1DArray(in, structure.conv1.outChannels);

        // Conv2
        weights.conv2Weights = read4DArray(in, structure.conv2.outChannels,
                structure.conv2.inChannels, structure.conv2.kernelSize, structure.conv2.kernelSize);
        weights.conv2Bias = read1DArray(in, structure.conv2.outChannels);

        // FC1
        weights.fc1Weights = read2DArray(in, structure.fc1.outputSize, structure.fc1.inputSize);
        weights.fc1Bias = read1DArray(in, structure.fc1.outputSize);

        // FC2
        weights.fc2Weights = read2DArray(in, structure.fc2.outputSize, structure.fc2.inputSize);
        weights.fc2Bias = read1DArray(in, structure.fc2.outputSize);

        return weights;
    }

    // 配列の読み書きヘルパーメソッド
    private static void write1DArray(DataOutputStream out, double[] array) throws IOException {
        out.writeInt(array.length);
        for (double val : array) {
            out.writeDouble(val);
        }
    }

    private static double[] read1DArray(DataInputStream in, int length) throws IOException {
        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = in.readDouble();
        }
        return array;
    }

    private static void write2DArray(DataOutputStream out, double[][] array) throws IOException {
        out.writeInt(array.length);
        out.writeInt(array[0].length);
        for (double[] row : array) {
            for (double val : row) {
                out.writeDouble(val);
            }
        }
    }

    private static double[][] read2DArray(DataInputStream in, int rows, int cols) throws IOException {
        double[][] array = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = in.readDouble();
            }
        }
        return array;
    }

    private static void write4DArray(DataOutputStream out, double[][][][] array) throws IOException {
        out.writeInt(array.length);
        out.writeInt(array[0].length);
        out.writeInt(array[0][0].length);
        out.writeInt(array[0][0][0].length);

        for (double[][][] dim1 : array) {
            for (double[][] dim2 : dim1) {
                for (double[] dim3 : dim2) {
                    for (double val : dim3) {
                        out.writeDouble(val);
                    }
                }
            }
        }
    }

    private static double[][][][] read4DArray(DataInputStream in, int d1, int d2, int d3, int d4) throws IOException {
        double[][][][] array = new double[d1][d2][d3][d4];
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    for (int l = 0; l < d4; l++) {
                        array[i][j][k][l] = in.readDouble();
                    }
                }
            }
        }
        return array;
    }

    // 内部クラス
    private static class ModelStructure {
        double learningRate;
        ConvLayerStructure conv1;
        ConvLayerStructure conv2;
        FCLayerStructure fc1;
        FCLayerStructure fc2;
    }

    private static class ConvLayerStructure {
        int inChannels, outChannels, kernelSize, stride, padding;

        ConvLayerStructure(int in, int out, int kernel, int stride, int padding) {
            this.inChannels = in;
            this.outChannels = out;
            this.kernelSize = kernel;
            this.stride = stride;
            this.padding = padding;
        }
    }

    private static class FCLayerStructure {
        int inputSize, outputSize;

        FCLayerStructure(int in, int out) {
            this.inputSize = in;
            this.outputSize = out;
        }
    }

    private static class ModelWeights {
        double[][][][] conv1Weights, conv2Weights;
        double[] conv1Bias, conv2Bias;
        double[][] fc1Weights, fc2Weights;
        double[] fc1Bias, fc2Bias;
    }
}