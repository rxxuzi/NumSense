package data.num;

import java.util.Random;

public class Five extends DigitDrawer {
    public Five(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の横線
        drawLine(img, cx - 6, cy - 8, cx + 6, cy - 8, thickness);

        // 左の縦線
        drawLine(img, cx - 6, cy - 8, cx - 6, cy - 1, thickness);

        // 中央の曲線
        if (style == DrawStyle.ROUNDED) {
            drawCurve(img, cx - 6, cy - 1, cx + 5, cy, cx - 2, cy - 2, thickness);
        } else {
            drawLine(img, cx - 6, cy - 1, cx + 5, cy - 1, thickness);
        }

        // 右の曲線
        drawCurve(img, cx + 5, cy, cx + 5, cy + 6, cx + 7, cy + 3, thickness);

        // 下部の曲線
        drawCurve(img, cx + 5, cy + 6, cx - 5, cy + 8, cx, cy + 9, thickness);
        drawCurve(img, cx - 5, cy + 8, cx - 6, cy + 5, cx - 7, cy + 6, thickness * 0.8);
    }
}