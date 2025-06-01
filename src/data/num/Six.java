package data.num;

import java.util.Random;

public class Six extends DigitDrawer {
    public Six(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の曲線から始まる
        drawCurve(img, cx + 4, cy - 8, cx - 4, cy - 6, cx, cy - 9, thickness);

        // 左の曲線（長い）
        drawCurve(img, cx - 4, cy - 6, cx - 6, cy + 6, cx - 5, cy, thickness);

        // 下部の円
        drawEllipse(img, cx, cy + 3, 5.5, 5.5, thickness * 1.2, false);
    }
}