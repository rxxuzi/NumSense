package data.num;

import java.util.Random;

/**
 * 数字1の描画クラス
 */
public class One extends DigitDrawer {

    public One(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int x = center + offsetX;
        int startY = center - 8 + offsetY;
        int endY = center + 8 + offsetY;

        // バリエーションを選択
        int variant = rand.nextInt(4);

        switch (variant) {
            case 0:
                // シンプルな縦線
                drawSimpleOne(img, x, startY, endY, style, thickness);
                break;
            case 1:
                // 上部に装飾がある1
                drawDecorativeOne(img, x, startY, endY, style, thickness);
                break;
            case 2:
                // 手書き風の1
                drawHandwrittenOne(img, x, startY, endY, style, thickness);
                break;
            case 3:
                // 筆記体風の1
                drawCursiveOne(img, x, startY, endY, style, thickness);
                break;
        }
    }

    /**
     * シンプルな1
     */
    private void drawSimpleOne(double[][] img, int x, int startY, int endY, DrawStyle style, double thickness) {
        // メインの縦線
        if (style == DrawStyle.ROUNDED || style == DrawStyle.FLOWING) {
            // わずかに曲がった1
            int controlX = x + rand.nextInt(3) - 1;
            drawCurve(img, x, startY, x, endY, controlX, center, thickness);
        } else {
            drawLine(img, x, startY, x, endY, thickness);
        }
    }

    /**
     * 装飾付きの1
     */
    private void drawDecorativeOne(double[][] img, int x, int startY, int endY, DrawStyle style, double thickness) {
        // メインの縦線
        drawLine(img, x, startY, x, endY, thickness);

        // 上部の斜め線
        drawLine(img, x - 3, startY + 2, x, startY, thickness * 0.8);

        // ベース（オプション）
        if (rand.nextDouble() > 0.5) {
            drawLine(img, x - 3, endY, x + 3, endY, thickness * 0.7);
        }
    }

    /**
     * 手書き風の1
     */
    private void drawHandwrittenOne(double[][] img, int x, int startY, int endY, DrawStyle style, double thickness) {
        // 筆圧を変化させながら描画
        int numPoints = 50;
        StrokePoint[] points = new StrokePoint[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double t = (double)i / (numPoints - 1);
            double y = startY + (endY - startY) * t;

            // 手ブレを追加
            double xOffset = (rand.nextDouble() - 0.5) * 0.8;
            if (style == DrawStyle.SHAKY) {
                xOffset *= 2;
            }

            // 筆圧変化（上部で強く、下部で弱く）
            double pressure = 1.2 - 0.4 * t;
            if (i < 5) {
                pressure *= 0.7; // 書き始めは弱く
            }

            points[i] = new StrokePoint(x + xOffset, y, pressure);
        }

        drawStrokeWithPressure(img, points, thickness);

        // 上部の飾り
        if (rand.nextDouble() > 0.3) {
            drawLine(img, x - 2, startY + 1, x, startY, thickness * 0.9);
        }
    }

    /**
     * 筆記体風の1
     */
    private void drawCursiveOne(double[][] img, int x, int startY, int endY, DrawStyle style, double thickness) {
        // S字カーブを描く
        int numPoints = 60;
        StrokePoint[] points = new StrokePoint[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double t = (double)i / (numPoints - 1);
            double y = startY + (endY - startY) * t;

            // S字カーブ
            double xOffset = 2 * Math.sin(t * Math.PI * 2);
            if (t < 0.2) {
                xOffset -= 2; // 上部は左に
            }

            // 筆圧変化
            double pressure = 0.8 + 0.4 * Math.sin(t * Math.PI);

            points[i] = new StrokePoint(x + xOffset, y, pressure);
        }

        drawStrokeWithPressure(img, points, thickness);
    }
}