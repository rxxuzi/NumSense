package data.num;

import java.util.Random;

public class Two extends DigitDrawer {
    public Two(int imageSize, Random rand) {
        super(imageSize, rand);
    }

    @Override
    public void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness) {
        int cx = center + offsetX;
        int cy = center + offsetY;

        // 上部の曲線
        drawCurve(img, cx - 6, cy - 6, cx + 6, cy - 6, cx, cy - 10, thickness);
        drawCurve(img, cx + 6, cy - 6, cx + 6, cy - 2, cx + 8, cy - 4, thickness);

        // 中央から下への斜線
        if (style == DrawStyle.ANGULAR) {
            drawLine(img, cx + 6, cy - 2, cx - 6, cy + 8, thickness);
        } else {
            drawCurve(img, cx + 6, cy - 2, cx - 6, cy + 8, cx, cy + 2, thickness);
        }

        // 下部の横線
        drawLine(img, cx - 6, cy + 8, cx + 6, cy + 8, thickness);
    }
}
