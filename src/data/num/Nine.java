package data.num;

import java.util.Random;

public class Nine extends DigitDrawer {
    public Nine(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の円
        drawEllipse(img, cx, cy - 3, 5.5, 5.5, thickness * 1.2, false);

        // 右の曲線（長い）
        drawCurve(img, cx + 4, cy + 2, cx + 4, cy + 8, cx + 5, cy + 5, thickness);

        // 下部の曲線
        if (style == DrawStyle.ROUNDED) {
            drawCurve(img, cx + 4, cy + 8, cx - 4, cy + 6, cx, cy + 9, thickness);
        } else {
            drawLine(img, cx + 4, cy + 8, cx - 2, cy + 8, thickness);
        }
    }
}