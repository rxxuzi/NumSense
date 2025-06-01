package data.num;

import java.util.Random;

public class Three extends DigitDrawer {
    public Three(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の曲線
        drawCurve(img, cx - 5, cy - 8, cx + 5, cy - 8, cx, cy - 10, thickness);
        drawCurve(img, cx + 5, cy - 8, cx + 5, cy - 2, cx + 7, cy - 5, thickness);

        // 中央の曲線
        drawCurve(img, cx + 5, cy - 2, cx - 2, cy, cx + 2, cy - 1, thickness);
        drawCurve(img, cx - 2, cy, cx + 5, cy + 2, cx + 2, cy + 1, thickness);

        // 下部の曲線
        drawCurve(img, cx + 5, cy + 2, cx + 5, cy + 8, cx + 7, cy + 5, thickness);
        drawCurve(img, cx + 5, cy + 8, cx - 5, cy + 8, cx, cy + 10, thickness);
    }
}