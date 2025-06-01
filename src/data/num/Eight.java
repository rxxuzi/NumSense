package data.num;

import java.util.Random;

public class Eight extends DigitDrawer {

    public Eight(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // シンプルに2つの円を描画する方式のみ使用
        drawBasicEight(img, cx, cy, style, thickness);
    }

    /**
     * 基本的な8の描画（2つの円を上下に配置）
     */
    private void drawBasicEight(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        if (style == DrawStyle.ANGULAR) {
            // 角張った8
            drawAngularEight(img, cx, cy, thickness);
        } else {
            // 通常の8（2つの円）
            drawTwoCirclesEight(img, cx, cy, thickness);
        }
    }

    /**
     * 2つの円で構成される8
     */
    private void drawTwoCirclesEight(double[][] img, int cx, int cy, double thickness) {
        // バリエーションを選択
        int variant = rand.nextInt(3);

        switch (variant) {
            case 0:
                // 標準的な2つの円
                drawStandardTwoCircles(img, cx, cy, thickness);
                break;
            case 1:
                // 少し傾いた8
                drawTiltedEight(img, cx, cy, thickness);
                break;
            case 2:
                // 手書き風の8
                drawHandwrittenEight(img, cx, cy, thickness);
                break;
        }
    }

    /**
     * 標準的な2つの円による8
     */
    private void drawStandardTwoCircles(double[][] img, int cx, int cy, double thickness) {
        // 上下の円のサイズ
        double topRadiusX = 4.5 + rand.nextDouble() * 0.5;
        double topRadiusY = 4.5 + rand.nextDouble() * 0.5;
        double bottomRadiusX = 5.0 + rand.nextDouble() * 0.5;
        double bottomRadiusY = 5.0 + rand.nextDouble() * 0.5;

        // 上下の円の中心位置
        int topCy = cy - 3;
        int bottomCy = cy + 3;

        // 上の円を描画
        drawEllipse(img, cx, topCy, topRadiusX, topRadiusY, thickness * 1.2, false);

        // 下の円を描画
        drawEllipse(img, cx, bottomCy, bottomRadiusX, bottomRadiusY, thickness * 1.2, false);

        // 中央のくびれ部分を接続
        // 左側のくびれ
        drawLine(img,
                (int)(cx - topRadiusX + 1), cy - 1,
                (int)(cx - bottomRadiusX + 1), cy + 1,
                thickness * 0.8);
        // 右側のくびれ
        drawLine(img,
                (int)(cx + topRadiusX - 1), cy - 1,
                (int)(cx + bottomRadiusX - 1), cy + 1,
                thickness * 0.8);
    }

    /**
     * 少し傾いた8
     */
    private void drawTiltedEight(double[][] img, int cx, int cy, double thickness) {
        // 傾き角度
        double tilt = (rand.nextDouble() - 0.5) * 0.2; // ±10度程度

        // 上の円
        int topCy = cy - 3;
        double topOffsetX = tilt * 3;
        drawEllipse(img, cx + (int)topOffsetX, topCy, 4.5, 4.5, thickness * 1.2, false);

        // 下の円
        int bottomCy = cy + 3;
        double bottomOffsetX = -tilt * 3;
        drawEllipse(img, cx + (int)bottomOffsetX, bottomCy, 5.0, 5.0, thickness * 1.2, false);

        // 接続線
        drawLine(img, cx + (int)topOffsetX - 4, cy - 1,
                cx + (int)bottomOffsetX - 4, cy + 1, thickness * 0.8);
        drawLine(img, cx + (int)topOffsetX + 4, cy - 1,
                cx + (int)bottomOffsetX + 4, cy + 1, thickness * 0.8);
    }

    /**
     * 手書き風の8（筆圧変化あり）
     */
    private void drawHandwrittenEight(double[][] img, int cx, int cy, double thickness) {
        // 上の円を筆圧変化で描画
        int numPoints = 60;
        StrokePoint[] topCircle = new StrokePoint[numPoints];

        double topRadius = 4.5;
        int topCy = cy - 3;

        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / numPoints;
            double x = cx + topRadius * Math.cos(t);
            double y = topCy + topRadius * Math.sin(t);

            // 手ブレ
            x += (rand.nextDouble() - 0.5) * 0.5;
            y += (rand.nextDouble() - 0.5) * 0.5;

            // 筆圧変化
            double pressure = 0.8 + 0.4 * Math.sin(t * 2);

            topCircle[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, topCircle, thickness);

        // 下の円を筆圧変化で描画
        StrokePoint[] bottomCircle = new StrokePoint[numPoints];

        double bottomRadius = 5.0;
        int bottomCy = cy + 3;

        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / numPoints;
            double x = cx + bottomRadius * Math.cos(t);
            double y = bottomCy + bottomRadius * Math.sin(t);

            // 手ブレ
            x += (rand.nextDouble() - 0.5) * 0.5;
            y += (rand.nextDouble() - 0.5) * 0.5;

            // 筆圧変化
            double pressure = 0.8 + 0.4 * Math.sin(t * 2 + Math.PI);

            bottomCircle[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, bottomCircle, thickness);
    }

    /**
     * 角張った8の描画
     */
    private void drawAngularEight(double[][] img, int cx, int cy, double thickness) {
        // 上部の長方形
        int topY = cy - 7;
        int topBottom = cy - 1;
        int left = cx - 4;
        int right = cx + 4;

        // 上部
        drawLine(img, left, topY, right, topY, thickness);
        drawLine(img, left, topY, left, topBottom, thickness);
        drawLine(img, right, topY, right, topBottom, thickness);
        drawLine(img, left, topBottom, right, topBottom, thickness);

        // 下部の長方形（少し大きめ）
        int bottomTop = cy + 1;
        int bottomY = cy + 7;
        int leftBottom = cx - 5;
        int rightBottom = cx + 5;

        // 下部
        drawLine(img, leftBottom, bottomTop, rightBottom, bottomTop, thickness);
        drawLine(img, leftBottom, bottomTop, leftBottom, bottomY, thickness);
        drawLine(img, rightBottom, bottomTop, rightBottom, bottomY, thickness);
        drawLine(img, leftBottom, bottomY, rightBottom, bottomY, thickness);
    }
}