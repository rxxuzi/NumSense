package data.num;

import java.util.Random;

public class Four extends DigitDrawer {
    public Four(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 左の斜め線
        if (style == DrawStyle.ANGULAR) {
            drawLine(img, cx - 4, cy - 8, cx - 4, cy + 2, thickness);
        } else {
            drawLine(img, cx - 4, cy - 8, cx - 6, cy + 2, thickness);
        }

        // 横線
        drawLine(img, cx - 6, cy + 2, cx + 6, cy + 2, thickness);

        // 右の縦線
        drawLine(img, cx + 3, cy - 8, cx + 3, cy + 8, thickness);
    }
}
