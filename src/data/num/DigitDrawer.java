package data.num;

import java.util.Random;

/**
 * 数字描画の基底クラス
 * 各数字の描画クラスはこのクラスを継承する
 */
public abstract class DigitDrawer {

    protected final Random rand;
    protected final int imageSize;
    protected final int center;

    // 描画スタイル
    public enum DrawStyle {
        NORMAL,      // 通常
        THICK,       // 太い
        THIN,        // 細い
        ROUNDED,     // 丸みを帯びた
        ANGULAR,     // 角張った
        FLOWING,     // 流れるような
        SHAKY        // 震えた線
    }

    // 筆圧シミュレーション用
    public static class StrokePoint {
        public double x, y;
        public double pressure;

        public StrokePoint(double x, double y, double pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }
    }

    public DigitDrawer(int imageSize, Random rand) {
        this.imageSize = imageSize;
        this.center = imageSize / 2;
        this.rand = rand;
    }

    /**
     * 数字を描画する抽象メソッド
     */
    public abstract void draw(double[][] img, int offsetX, int offsetY, DrawStyle style, double thickness);

    /**
     * スタイルに応じた線の太さを取得
     */
    protected double getThickness(DrawStyle style) {
        switch (style) {
            case THICK:
                return 1.5 + rand.nextDouble() * 0.5;
            case THIN:
                return 0.6 + rand.nextDouble() * 0.3;
            case SHAKY:
                return 0.8 + rand.nextDouble() * 0.4;
            case FLOWING:
                return 1.0 + rand.nextDouble() * 0.4;
            case NORMAL:
            default:
                return 1.0 + rand.nextDouble() * 0.3;
        }
    }

    /**
     * 線を描画（改良版）
     */
    protected void drawLine(double[][] img, int x1, int y1, int x2, int y2, double thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            // 太さを考慮して周囲のピクセルも描画
            for (int ty = -1; ty <= 1; ty++) {
                for (int tx = -1; tx <= 1; tx++) {
                    int px = x1 + tx;
                    int py = y1 + ty;
                    if (px >= 0 && px < imageSize && py >= 0 && py < imageSize) {
                        double distance = Math.sqrt(tx * tx + ty * ty);
                        if (distance <= thickness / 2) {
                            double intensity = 1.0 - distance / (thickness / 2);
                            img[py][px] = Math.max(img[py][px], intensity);
                        }
                    }
                }
            }

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * 曲線を描画
     */
    protected void drawCurve(double[][] img, int startX, int startY, int endX, int endY,
                             int controlX, int controlY, double thickness) {
        // 2次ベジェ曲線
        for (double t = 0; t <= 1.0; t += 0.01) {
            double x = (1-t)*(1-t)*startX + 2*(1-t)*t*controlX + t*t*endX;
            double y = (1-t)*(1-t)*startY + 2*(1-t)*t*controlY + t*t*endY;

            int px = (int)Math.round(x);
            int py = (int)Math.round(y);

            if (px >= 0 && px < imageSize && py >= 0 && py < imageSize) {
                // 太さを適用
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = px + dx;
                        int ny = py + dy;
                        if (nx >= 0 && nx < imageSize && ny >= 0 && ny < imageSize) {
                            double distance = Math.sqrt(dx * dx + dy * dy);
                            if (distance <= thickness / 2) {
                                img[ny][nx] = Math.max(img[ny][nx], 1.0 - distance / thickness);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 楕円を描画
     */
    protected void drawEllipse(double[][] img, int cx, int cy, double radiusX, double radiusY,
                               double thickness, boolean filled) {
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double dx = (x - cx) / radiusX;
                double dy = (y - cy) / radiusY;
                double distance = dx * dx + dy * dy;

                if (filled) {
                    if (distance <= 1.0) {
                        img[y][x] = 1.0;
                    }
                } else {
                    // 輪郭のみ
                    double innerRadius = Math.max(0, 1.0 - thickness / Math.min(radiusX, radiusY));
                    if (distance <= 1.0 && distance >= innerRadius * innerRadius) {
                        double intensity = 1.0 - Math.abs(distance - ((1.0 + innerRadius * innerRadius) / 2)) * 2;
                        img[y][x] = Math.max(img[y][x], intensity);
                    }
                }
            }
        }
    }

    /**
     * 筆圧を考慮した線を描画
     */
    protected void drawStrokeWithPressure(double[][] img, StrokePoint[] points, double baseThickness) {
        for (int i = 0; i < points.length - 1; i++) {
            StrokePoint p1 = points[i];
            StrokePoint p2 = points[i + 1];

            // 線分の長さに応じて補間点数を決定
            double dist = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
            int steps = Math.max(2, (int)(dist * 2));

            for (int step = 0; step <= steps; step++) {
                double t = (double)step / steps;
                double x = p1.x + (p2.x - p1.x) * t;
                double y = p1.y + (p2.y - p1.y) * t;
                double pressure = p1.pressure + (p2.pressure - p1.pressure) * t;
                double thickness = baseThickness * pressure;

                // 円形のブラシで描画
                drawBrush(img, x, y, thickness);
            }
        }
    }

    /**
     * ブラシで点を描画
     */
    protected void drawBrush(double[][] img, double cx, double cy, double radius) {
        int minX = Math.max(0, (int)(cx - radius - 1));
        int maxX = Math.min(imageSize - 1, (int)(cx + radius + 1));
        int minY = Math.max(0, (int)(cy - radius - 1));
        int maxY = Math.min(imageSize - 1, (int)(cy + radius + 1));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                if (dist <= radius) {
                    // ソフトエッジ
                    double intensity = 1.0 - Math.pow(dist / radius, 2);
                    img[y][x] = Math.max(img[y][x], intensity);
                }
            }
        }
    }

    /**
     * ベジェ曲線のパスを生成
     */
    protected StrokePoint[] createBezierPath(double[] start, double[] control1,
                                             double[] control2, double[] end, int numPoints) {
        StrokePoint[] path = new StrokePoint[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double t = (double)i / (numPoints - 1);
            double t2 = t * t;
            double t3 = t2 * t;
            double mt = 1 - t;
            double mt2 = mt * mt;
            double mt3 = mt2 * mt;

            // 3次ベジェ曲線
            double x = mt3 * start[0] + 3 * mt2 * t * control1[0] +
                    3 * mt * t2 * control2[0] + t3 * end[0];
            double y = mt3 * start[1] + 3 * mt2 * t * control1[1] +
                    3 * mt * t2 * control2[1] + t3 * end[1];

            // 筆圧は中央で最大
            double pressure = 0.7 + 0.3 * Math.sin(t * Math.PI);

            path[i] = new StrokePoint(x, y, pressure);
        }

        return path;
    }

    /**
     * 滑らかな線の描画
     */
    protected void drawLineSmooth(double[][] img, double x1, double y1, double x2, double y2, double thickness) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        int steps = Math.max(2, (int)(distance * 2));

        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;

            drawBrush(img, x, y, thickness / 2);
        }
    }
}