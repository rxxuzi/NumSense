package data;

import data.num.*;
import main.CNN;
import java.util.Random;

/**
 * 改良版 手書き数字画像生成器
 * 各数字の描画を専用クラスに分離
 */
public class MINIST {

    private final Random rand;
    private final int imageSize = CNN.IMAGE_SIZE;
    private final int center = CNN.IMAGE_SIZE / 2;

    // 各数字の描画クラス
    private final DigitDrawer[] digitDrawers;

    public MINIST() {
        this.rand = new Random();
        this.digitDrawers = initializeDrawers();
    }

    public MINIST(long seed) {
        this.rand = new Random(seed);
        this.digitDrawers = initializeDrawers();
    }

    /**
     * 各数字の描画クラスを初期化
     */
    private DigitDrawer[] initializeDrawers() {
        DigitDrawer[] drawers = new DigitDrawer[10];

        drawers[0] = new Zero(imageSize, rand);
        drawers[1] = new One(imageSize, rand);
        drawers[2] = new Two(imageSize, rand);
        drawers[3] = new Three(imageSize, rand);
        drawers[4] = new Four(imageSize, rand);
        drawers[5] = new Five(imageSize, rand);
        drawers[6] = new Six(imageSize, rand);
        drawers[7] = new Seven(imageSize, rand);
        drawers[8] = new Eight(imageSize, rand);
        drawers[9] = new Nine(imageSize, rand);

        return drawers;
    }

    /**
     * 数字の画像を生成
     * @param digit 数字（0-9）
     * @param noise ノイズレベル（0.0-1.0）
     * @return 画像配列（0.0-1.0の値）
     */
    public double[][] generateDigit(int digit, double noise) {
        if (digit < 0 || digit > 9) {
            throw new IllegalArgumentException("Digit must be between 0 and 9");
        }

        double[][] image = new double[imageSize][imageSize];

        // 背景を白（0.0）で初期化
        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                image[i][j] = 0.0;
            }
        }

        // ランダムなスタイルを選択
        DigitDrawer.DrawStyle style = DigitDrawer.DrawStyle.values()[rand.nextInt(DigitDrawer.DrawStyle.values().length)];

        // 数字の中心位置をランダムに少しずらす
        int offsetX = rand.nextInt(5) - 2;
        int offsetY = rand.nextInt(5) - 2;

        // スタイルに応じた線の太さ
        double thickness = getThickness(style);

        // 数字を描画（対応するDrawerクラスを使用）
        digitDrawers[digit].draw(image, offsetX, offsetY, style, thickness);

        // ランダムな変形を追加
        image = applyRandomTransform(image);

        // ノイズを追加
        if (noise > 0) {
            addNoise(image, noise);
        }

        // スムージング
        image = smoothImage(image);

        return image;
    }

    /**
     * スタイルに応じた線の太さを取得
     */
    private double getThickness(DigitDrawer.DrawStyle style) {
        switch (style) {
            case THICK:
                return 1.5 + rand.nextDouble() * 0.5;
            case THIN:
                return 0.6 + rand.nextDouble() * 0.3;
            case SHAKY:
                return 0.8 + rand.nextDouble() * 0.4;
            case FLOWING:
                return 1.0 + rand.nextDouble() * 0.4;
            case NORMAL:
            default:
                return 1.0 + rand.nextDouble() * 0.3;
        }
    }

    /**
     * ランダムな変形を適用
     */
    private double[][] applyRandomTransform(double[][] image) {
        // 回転
        double angle = (rand.nextDouble() - 0.5) * 0.4;  // ±20度程度
        image = rotate(image, angle);

        // スケーリング
        double scale = 0.85 + rand.nextDouble() * 0.3;  // 0.85-1.15倍
        if (Math.abs(scale - 1.0) > 0.05) {
            image = scale(image, scale);
        }

        // 歪み（スキュー）
        if (rand.nextDouble() > 0.7) {
            image = skew(image, (rand.nextDouble() - 0.5) * 0.2);
        }

        // 弾性変形
        if (rand.nextDouble() > 0.8) {
            image = elasticDeform(image);
        }

        return image;
    }

    /**
     * スキュー変換
     */
    private double[][] skew(double[][] image, double factor) {
        double[][] skewed = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                int srcX = x - (int)((y - cy) * factor);

                if (srcX >= 0 && srcX < imageSize) {
                    skewed[y][x] = image[y][srcX];
                }
            }
        }

        return skewed;
    }

    /**
     * 弾性変形
     */
    private double[][] elasticDeform(double[][] image) {
        double[][] deformed = new double[imageSize][imageSize];
        double amplitude = 2.0;
        double frequency = 0.1;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double dx = amplitude * Math.sin(frequency * y);
                double dy = amplitude * Math.sin(frequency * x);

                int srcX = (int)(x + dx);
                int srcY = (int)(y + dy);

                if (srcX >= 0 && srcX < imageSize && srcY >= 0 && srcY < imageSize) {
                    deformed[y][x] = image[srcY][srcX];
                }
            }
        }

        return deformed;
    }

    /**
     * 画像を回転
     */
    private double[][] rotate(double[][] image, double angle) {
        double[][] rotated = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                // 回転前の座標を計算
                int dx = x - cx;
                int dy = y - cy;
                double srcX = cos * dx + sin * dy + cx;
                double srcY = -sin * dx + cos * dy + cy;

                // バイリニア補間
                int x0 = (int)Math.floor(srcX);
                int y0 = (int)Math.floor(srcY);
                int x1 = x0 + 1;
                int y1 = y0 + 1;

                if (x0 >= 0 && x1 < imageSize && y0 >= 0 && y1 < imageSize) {
                    double fx = srcX - x0;
                    double fy = srcY - y0;

                    double val = (1 - fx) * (1 - fy) * image[y0][x0] +
                            fx * (1 - fy) * image[y0][x1] +
                            (1 - fx) * fy * image[y1][x0] +
                            fx * fy * image[y1][x1];

                    rotated[y][x] = val;
                }
            }
        }

        return rotated;
    }

    /**
     * 画像をスケーリング
     */
    private double[][] scale(double[][] image, double factor) {
        double[][] scaled = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double srcX = (x - cx) / factor + cx;
                double srcY = (y - cy) / factor + cy;

                // バイリニア補間
                int x0 = (int)Math.floor(srcX);
                int y0 = (int)Math.floor(srcY);
                int x1 = x0 + 1;
                int y1 = y0 + 1;

                if (x0 >= 0 && x1 < imageSize && y0 >= 0 && y1 < imageSize) {
                    double fx = srcX - x0;
                    double fy = srcY - y0;

                    double val = (1 - fx) * (1 - fy) * image[y0][x0] +
                            fx * (1 - fy) * image[y0][x1] +
                            (1 - fx) * fy * image[y1][x0] +
                            fx * fy * image[y1][x1];

                    scaled[y][x] = val;
                }
            }
        }

        return scaled;
    }

    /**
     * ノイズを追加
     */
    private void addNoise(double[][] image, double noiseLevel) {
        // ガウシアンノイズ
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double noise = rand.nextGaussian() * noiseLevel * 0.5;
                image[y][x] = Math.max(0.0, Math.min(1.0, image[y][x] + noise));
            }
        }

        // ソルト＆ペッパーノイズ（低確率）
        if (rand.nextDouble() > 0.8) {
            int numPoints = (int)(imageSize * imageSize * noiseLevel * 0.01);
            for (int i = 0; i < numPoints; i++) {
                int x = rand.nextInt(imageSize);
                int y = rand.nextInt(imageSize);
                image[y][x] = rand.nextDouble() > 0.5 ? 1.0 : 0.0;
            }
        }
    }

    /**
     * 画像をスムージング
     */
    private double[][] smoothImage(double[][] image) {
        double[][] smoothed = new double[imageSize][imageSize];
        double[][] kernel = {
                {0.0625, 0.125, 0.0625},
                {0.125,  0.25,  0.125},
                {0.0625, 0.125, 0.0625}
        };

        for (int y = 1; y < imageSize - 1; y++) {
            for (int x = 1; x < imageSize - 1; x++) {
                double sum = 0.0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        sum += image[y + ky][x + kx] * kernel[ky + 1][kx + 1];
                    }
                }
                smoothed[y][x] = sum;
            }
        }

        // 境界をコピー
        for (int i = 0; i < imageSize; i++) {
            smoothed[0][i] = image[0][i];
            smoothed[imageSize-1][i] = image[imageSize-1][i];
            smoothed[i][0] = image[i][0];
            smoothed[i][imageSize-1] = image[i][imageSize-1];
        }

        return smoothed;
    }

    /**
     * 画像をコンソールに表示（デバッグ用）
     */
    public static void printImage(double[][] image) {
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                if (image[y][x] > 0.8) {
                    System.out.print("█");
                } else if (image[y][x] > 0.6) {
                    System.out.print("▓");
                } else if (image[y][x] > 0.3) {
                    System.out.print("▒");
                } else if (image[y][x] > 0.1) {
                    System.out.print("░");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    /**
     * テスト用メイン
     */
    public static void main(String[] args) {
        var generator = new MINIST();

        // 8を集中的にテスト
        System.out.println("\n=== Digit 8 - Multiple Variations ===");
        for (int i = 0; i < 10; i++) {
            System.out.println("\nVariation " + (i + 1) + ":");
            double[][] image = generator.generateDigit(8, 0.05);
            printImage(image);
        }

        // 他の数字も簡単にテスト
        System.out.println("\n\n=== Other Digits (0-7, 9) ===");
        for (int digit = 0; digit <= 9; digit++) {
            if (digit != 8) {
                System.out.println("\n--- Digit " + digit + " ---");
                double[][] image = generator.generateDigit(digit, 0.1);
                printImage(image);
            }
        }
    }
}