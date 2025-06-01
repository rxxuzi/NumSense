package data.num;

import java.util.Random;

public class Seven extends DigitDrawer {
    public Seven(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の横線
        drawLine(img, cx - 6, cy - 8, cx + 6, cy - 8, thickness);

        // 斜めの線
        if (style == DrawStyle.ANGULAR) {
            drawLine(img, cx + 6, cy - 8, cx - 2, cy + 8, thickness);
        } else {
            // わずかに曲がった7
            drawCurve(img, cx + 6, cy - 8, cx - 2, cy + 8, cx + 2, cy, thickness);
        }

        // 短い横線（オプション）
        if (rand.nextDouble() > 0.6) {
            drawLine(img, cx - 2, cy, cx + 2, cy, thickness * 0.7);
        }
    }
}