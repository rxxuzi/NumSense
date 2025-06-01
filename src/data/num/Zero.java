package data.num;

import java.util.Random;

/**
 * 数字0の描画クラス
 */
public class Zero extends DigitDrawer {

    public Zero(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;
        double radiusX = 6.0 + rand.nextDouble() * 2;
        double radiusY = 8.0 + rand.nextDouble() * 2;

        if (style == DrawStyle.ANGULAR) {
            // 角張った0（菱形風）
            drawAngularZero(img, cx, cy, thickness);
        } else {
            // 通常の楕円（様々なバリエーション）
            int variant = rand.nextInt(3);
            switch (variant) {
                case 0:
                    // 標準的な楕円
                    drawEllipse(img, cx, cy, radiusX, radiusY, thickness * 1.5, false);
                    break;
                case 1:
                    // 少し傾いた楕円
                    drawTiltedEllipse(img, cx, cy, radiusX, radiusY, thickness);
                    break;
                case 2:
                    // 手書き風の歪んだ楕円
                    drawHandwrittenEllipse(img, cx, cy, radiusX, radiusY, thickness);
                    break;
            }
        }
    }

    /**
     * 角張った0の描画
     */
    private void drawAngularZero(double[][] img, int cx, int cy, double thickness) {
        int top = cy - 8;
        int bottom = cy + 8;
        int left = cx - 6;
        int right = cx + 6;

        drawLine(img, left + 2, top, right - 2, top, thickness);
        drawLine(img, right - 2, top, right, top + 2, thickness);
        drawLine(img, right, top + 2, right, bottom - 2, thickness);
        drawLine(img, right, bottom - 2, right - 2, bottom, thickness);
        drawLine(img, right - 2, bottom, left + 2, bottom, thickness);
        drawLine(img, left + 2, bottom, left, bottom - 2, thickness);
        drawLine(img, left, bottom - 2, left, top + 2, thickness);
        drawLine(img, left, top + 2, left + 2, top, thickness);
    }

    /**
     * 傾いた楕円の描画
     */
    private void drawTiltedEllipse(double[][] img, int cx, int cy, double radiusX, double radiusY, double thickness) {
        double angle = (rand.nextDouble() - 0.5) * 0.3; // ±15度程度の傾き
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // パラメトリック方程式で楕円を描画
        int numPoints = 100;
        StrokePoint[] points = new StrokePoint[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / numPoints;

            // 楕円の基本座標
            double ellipseX = radiusX * Math.cos(t);
            double ellipseY = radiusY * Math.sin(t);

            // 回転を適用
            double x = cx + ellipseX * cos - ellipseY * sin;
            double y = cy + ellipseX * sin + ellipseY * cos;

            // 筆圧変化
            double pressure = 0.9 + 0.2 * Math.sin(4 * t);

            points[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, points, thickness);
    }

    /**
     * 手書き風の歪んだ楕円
     */
    private void drawHandwrittenEllipse(double[][] img, int cx, int cy, double radiusX, double radiusY, double thickness) {
        int numPoints = 100;
        StrokePoint[] points = new StrokePoint[numPoints];

        // ランダムな歪みパラメータ
        double wobbleX = rand.nextDouble() * 0.3;
        double wobbleY = rand.nextDouble() * 0.3;
        double wobblePhase = rand.nextDouble() * Math.PI;

        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / numPoints;

            // 基本の楕円に歪みを加える
            double distortionX = Math.sin(3 * t + wobblePhase) * wobbleX;
            double distortionY = Math.cos(4 * t + wobblePhase) * wobbleY;

            double x = cx + (radiusX + distortionX) * Math.cos(t);
            double y = cy + (radiusY + distortionY) * Math.sin(t);

            // 手ブレを追加
            x += (rand.nextDouble() - 0.5) * 0.3;
            y += (rand.nextDouble() - 0.5) * 0.3;

            // 筆圧変化（開始・終了点で弱く）
            double pressure = 0.8 + 0.4 * Math.sin(t + Math.PI/4);
            if (i < 5 || i > numPoints - 5) {
                pressure *= 0.7;
            }

            points[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, points, thickness);
    }
}