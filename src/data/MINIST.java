package data;

import main.CNN;

import java.util.Random;

/**
 * 改良版 手書き数字画像生成器
 * より多様で現実的な手書き数字を生成
 * 特に8の描画を強化
 */
public class MINIST {

    private final Random rand;
    private final int imageSize = CNN.IMAGE_SIZE;
    private final int center = CNN.IMAGE_SIZE / 2;

    // 描画スタイルのバリエーション
    private enum DrawStyle {
        NORMAL,      // 通常
        THICK,       // 太い
        THIN,        // 細い
        ROUNDED,     // 丸みを帯びた
        ANGULAR,     // 角張った
        FLOWING,     // 流れるような
        SHAKY        // 震えた線
    }

    // 筆圧シミュレーション用
    private class StrokePoint {
        double x, y;
        double pressure;

        StrokePoint(double x, double y, double pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }
    }

    public MINIST() {
        this.rand = new Random();
    }

    public MINIST(long seed) {
        this.rand = new Random(seed);
    }

    /**
     * 数字の画像を生成
     * @param digit 数字（0-9）
     * @param noise ノイズレベル（0.0-1.0）
     * @return 画像配列（0.0-1.0の値）
     */
    public double[][] generateDigit(int digit, double noise) {
        double[][] image = new double[imageSize][imageSize];

        // 背景を白（0.0）で初期化
        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                image[i][j] = 0.0;
            }
        }

        // ランダムなスタイルを選択
        DrawStyle style = DrawStyle.values()[rand.nextInt(DrawStyle.values().length)];

        // 数字を描画
        drawDigit(image, digit, style);

        // ランダムな変形を追加
        image = applyRandomTransform(image);

        // ノイズを追加
        if (noise > 0) {
            addNoise(image, noise);
        }

        // スムージング
        image = smoothImage(image);

        return image;
    }

    /**
     * 数字を描画（スタイル付き）
     */
    private void drawDigit(double[][] image, int digit, DrawStyle style) {
        // 数字の中心位置をランダムに少しずらす
        int offsetX = rand.nextInt(5) - 2;
        int offsetY = rand.nextInt(5) - 2;

        // スタイルに応じた線の太さ
        double thickness = getThickness(style);

        switch (digit) {
            case 0:
                drawZero(image, offsetX, offsetY, style, thickness);
                break;
            case 1:
                drawOne(image, offsetX, offsetY, style, thickness);
                break;
            case 2:
                drawTwo(image, offsetX, offsetY, style, thickness);
                break;
            case 3:
                drawThree(image, offsetX, offsetY, style, thickness);
                break;
            case 4:
                drawFour(image, offsetX, offsetY, style, thickness);
                break;
            case 5:
                drawFive(image, offsetX, offsetY, style, thickness);
                break;
            case 6:
                drawSix(image, offsetX, offsetY, style, thickness);
                break;
            case 7:
                drawSeven(image, offsetX, offsetY, style, thickness);
                break;
            case 8:
                drawEightImproved(image, offsetX, offsetY, style, thickness);
                break;
            case 9:
                drawNine(image, offsetX, offsetY, style, thickness);
                break;
        }
    }

    /**
     * スタイルに応じた線の太さを取得
     */
    private double getThickness(DrawStyle style) {
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
     * 筆圧を考慮した線を描画
     */
    private void drawStrokeWithPressure(double[][] img, StrokePoint[] points, double baseThickness) {
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
    private void drawBrush(double[][] img, double cx, double cy, double radius) {
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
     * 改良版の8の描画
     */
    private void drawEightImproved(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

        // 8の描画方法を複数用意
        int variant = rand.nextInt(5);

        switch (variant) {
            case 0:
                // スムーズな連続曲線での8
                drawEightContinuous(img, cx, cy, style, thickness);
                break;
            case 1:
                // 2つの円を自然に接続
                drawEightNaturalCircles(img, cx, cy, style, thickness);
                break;
            case 2:
                // 一筆書き風の8
                drawEightOneStroke(img, cx, cy, style, thickness);
                break;
            case 3:
                // くびれを強調した8
                drawEightEmphasizedWaist(img, cx, cy, style, thickness);
                break;
            case 4:
            default:
                // クラシックスタイル（改良版）
                drawEightClassic(img, cx, cy, style, thickness);
                break;
        }
    }

    /**
     * 連続曲線で描く8
     */
    private void drawEightContinuous(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        // パラメトリック曲線で8を描画
        int numPoints = 100;
        StrokePoint[] points = new StrokePoint[numPoints];

        double scaleX = 5.0 + rand.nextDouble() * 2;
        double scaleY = 7.0 + rand.nextDouble() * 2;

        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / (numPoints - 1);
            double x = cx + scaleX * Math.sin(t);
            double y = cy + scaleY * Math.sin(2 * t) / 2;

            // 筆圧の変化
            double pressure = 0.8 + 0.4 * Math.sin(4 * t);
            if (style == DrawStyle.FLOWING) {
                pressure *= 0.9 + 0.2 * Math.sin(8 * t);
            }

            points[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, points, thickness);
    }

    /**
     * 自然な円の接続で描く8
     */
    private void drawEightNaturalCircles(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        // 上下の円のパラメータ
        double topRadius = 4.5 + rand.nextDouble();
        double bottomRadius = 5.0 + rand.nextDouble();
        double separation = 3.0 + rand.nextDouble() * 0.5;

        // 上の円（右回り）
        int numPoints = 60;
        StrokePoint[] topCircle = new StrokePoint[numPoints];
        for (int i = 0; i < numPoints; i++) {
            double t = i * 2 * Math.PI / numPoints;
            double x = cx + topRadius * Math.cos(t);
            double y = cy - separation + topRadius * Math.sin(t);

            // くびれ部分で筆圧を変化
            double pressure = 1.0;
            if (Math.abs(t - Math.PI) < Math.PI / 4 || Math.abs(t) < Math.PI / 4) {
                pressure = 0.7 + 0.3 * Math.cos(4 * t);
            }

            topCircle[i] = new StrokePoint(x, y, pressure);
        }

        // 下の円（左回り）
        StrokePoint[] bottomCircle = new StrokePoint[numPoints];
        for (int i = 0; i < numPoints; i++) {
            double t = -i * 2 * Math.PI / numPoints;
            double x = cx + bottomRadius * Math.cos(t);
            double y = cy + separation + bottomRadius * Math.sin(t);

            double pressure = 1.0;
            if (Math.abs(t - Math.PI) < Math.PI / 4 || Math.abs(t) < Math.PI / 4) {
                pressure = 0.7 + 0.3 * Math.cos(4 * t);
            }

            bottomCircle[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, topCircle, thickness);
        drawStrokeWithPressure(img, bottomCircle, thickness);

        // 接続部分を滑らかに
        drawSmoothConnection(img, cx, cy, topRadius, bottomRadius, separation, thickness);
    }

    /**
     * 一筆書き風の8
     */
    private void drawEightOneStroke(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        // 8を一筆で描くパス
        int numPoints = 120;
        StrokePoint[] path = new StrokePoint[numPoints];

        double scaleX = 5.0 + rand.nextDouble();
        double scaleY = 8.0 + rand.nextDouble();

        // 開始位置をランダムに
        double startAngle = rand.nextDouble() * 2 * Math.PI;

        for (int i = 0; i < numPoints; i++) {
            double t = (double)i / (numPoints - 1);
            double angle = startAngle + t * 4 * Math.PI; // 2周

            // リサージュ曲線の変形で8の形を作る
            double x = cx + scaleX * Math.sin(angle + Math.PI / 2);
            double y = cy + scaleY * Math.sin(angle * 2) / 2.2;

            // 手ブレを追加
            if (style == DrawStyle.SHAKY) {
                x += (rand.nextDouble() - 0.5) * 0.5;
                y += (rand.nextDouble() - 0.5) * 0.5;
            }

            // 筆圧の変化（始点と終点で弱く）
            double pressure = Math.sin(t * Math.PI) * 0.8 + 0.4;

            path[i] = new StrokePoint(x, y, pressure);
        }

        drawStrokeWithPressure(img, path, thickness);
    }

    /**
     * くびれを強調した8
     */
    private void drawEightEmphasizedWaist(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        // ベジェ曲線で8の各部分を描画

        // 上半分（右側から開始、反時計回り）
        StrokePoint[] upperPath = createBezierPath(
                new double[]{cx + 1, cy},           // 開始点（くびれ右）
                new double[]{cx + 6, cy - 2},       // 制御点1
                new double[]{cx + 6, cy - 6},       // 制御点2
                new double[]{cx, cy - 8},           // 頂点
                30
        );

        StrokePoint[] upperPath2 = createBezierPath(
                new double[]{cx, cy - 8},           // 頂点
                new double[]{cx - 6, cy - 6},       // 制御点1
                new double[]{cx - 6, cy - 2},       // 制御点2
                new double[]{cx - 1, cy},           // くびれ左
                30
        );

        // 下半分（左側から開始、反時計回り）
        StrokePoint[] lowerPath = createBezierPath(
                new double[]{cx - 1, cy},           // くびれ左
                new double[]{cx - 7, cy + 2},       // 制御点1
                new double[]{cx - 7, cy + 6},       // 制御点2
                new double[]{cx, cy + 8},           // 底点
                30
        );

        StrokePoint[] lowerPath2 = createBezierPath(
                new double[]{cx, cy + 8},           // 底点
                new double[]{cx + 7, cy + 6},       // 制御点1
                new double[]{cx + 7, cy + 2},       // 制御点2
                new double[]{cx + 1, cy},           // くびれ右
                30
        );

        // 各パスを描画
        drawStrokeWithPressure(img, upperPath, thickness);
        drawStrokeWithPressure(img, upperPath2, thickness);
        drawStrokeWithPressure(img, lowerPath, thickness);
        drawStrokeWithPressure(img, lowerPath2, thickness);
    }

    /**
     * クラシックスタイルの8（改良版）
     */
    private void drawEightClassic(double[][] img, int cx, int cy, DrawStyle style, double thickness) {
        if (style == DrawStyle.ANGULAR) {
            // 角張った8
            drawEightAngular(img, cx, cy, thickness);
        } else {
            // 滑らかな8
            double topRadiusX = 4.5 + rand.nextDouble();
            double topRadiusY = 4.5 + rand.nextDouble();
            double bottomRadiusX = 5.0 + rand.nextDouble();
            double bottomRadiusY = 5.0 + rand.nextDouble();

            // 上下の円を少しずらして自然な重なりを作る
            int topCy = cy - 3;
            int bottomCy = cy + 3;

            // 変形楕円で描画
            drawDeformedEllipse(img, cx, topCy, topRadiusX, topRadiusY, thickness, false, 0.1);
            drawDeformedEllipse(img, cx, bottomCy, bottomRadiusX, bottomRadiusY, thickness, false, -0.1);

            // くびれ部分を自然に接続
            drawNaturalWaist(img, cx, cy, topRadiusX, bottomRadiusX, thickness);
        }
    }

    /**
     * 角張った8の描画
     */
    private void drawEightAngular(double[][] img, int cx, int cy, double thickness) {
        // 六角形ベースの8
        int[] xPoints = {cx - 4, cx - 4, cx, cx + 4, cx + 4, cx};
        int[] yPoints = {cy - 5, cy - 1, cy - 7, cy - 5, cy - 1, cy - 7};

        // 上部
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            drawLine(img, xPoints[i], yPoints[i], xPoints[next], yPoints[next], thickness);
        }

        // 下部（少し大きめ）
        int[] xPointsBottom = {cx - 5, cx - 5, cx, cx + 5, cx + 5, cx};
        int[] yPointsBottom = {cy + 1, cy + 6, cy + 7, cy + 6, cy + 1, cy + 7};

        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            drawLine(img, xPointsBottom[i], yPointsBottom[i],
                    xPointsBottom[next], yPointsBottom[next], thickness);
        }
    }

    /**
     * 変形楕円の描画（より自然な手書き風）
     */
    private void drawDeformedEllipse(double[][] img, int cx, int cy, double radiusX, double radiusY,
                                     double thickness, boolean filled, double deformation) {
        for (double angle = 0; angle < 2 * Math.PI; angle += 0.01) {
            // 基本の楕円
            double x = cx + radiusX * Math.cos(angle);
            double y = cy + radiusY * Math.sin(angle);

            // 変形を加える
            double deformX = deformation * Math.sin(3 * angle) * radiusX * 0.1;
            double deformY = deformation * Math.cos(3 * angle) * radiusY * 0.1;

            x += deformX;
            y += deformY;

            // 次の点
            double nextAngle = angle + 0.01;
            double nextX = cx + radiusX * Math.cos(nextAngle);
            double nextY = cy + radiusY * Math.sin(nextAngle);
            nextX += deformation * Math.sin(3 * nextAngle) * radiusX * 0.1;
            nextY += deformation * Math.cos(3 * nextAngle) * radiusY * 0.1;

            // 線分を描画
            drawLineSmooth(img, x, y, nextX, nextY, thickness);
        }
    }

    /**
     * 自然なくびれの描画
     */
    private void drawNaturalWaist(double[][] img, int cx, int cy, double topRadius, double bottomRadius,
                                  double thickness) {
        // 左側のくびれ
        double[] leftCurve = {
                cx - topRadius * 0.9, cy - 1,
                cx - topRadius * 0.5, cy,
                cx - bottomRadius * 0.5, cy,
                cx - bottomRadius * 0.9, cy + 1
        };

        // 右側のくびれ
        double[] rightCurve = {
                cx + topRadius * 0.9, cy - 1,
                cx + topRadius * 0.5, cy,
                cx + bottomRadius * 0.5, cy,
                cx + bottomRadius * 0.9, cy + 1
        };

        // ベジェ曲線で描画
        drawBezierCurve(img, leftCurve, thickness * 0.9);
        drawBezierCurve(img, rightCurve, thickness * 0.9);
    }

    /**
     * 滑らかな接続部分の描画
     */
    private void drawSmoothConnection(double[][] img, int cx, int cy, double topRadius,
                                      double bottomRadius, double separation, double thickness) {
        // 左側の接続
        StrokePoint[] leftConnection = createBezierPath(
                new double[]{cx - topRadius * 0.8, cy - separation * 0.3},
                new double[]{cx - topRadius * 0.4, cy},
                new double[]{cx - bottomRadius * 0.4, cy},
                new double[]{cx - bottomRadius * 0.8, cy + separation * 0.3},
                20
        );

        // 右側の接続
        StrokePoint[] rightConnection = createBezierPath(
                new double[]{cx + topRadius * 0.8, cy - separation * 0.3},
                new double[]{cx + topRadius * 0.4, cy},
                new double[]{cx + bottomRadius * 0.4, cy},
                new double[]{cx + bottomRadius * 0.8, cy + separation * 0.3},
                20
        );

        drawStrokeWithPressure(img, leftConnection, thickness * 0.8);
        drawStrokeWithPressure(img, rightConnection, thickness * 0.8);
    }

    /**
     * ベジェ曲線のパスを生成
     */
    private StrokePoint[] createBezierPath(double[] start, double[] control1,
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
     * ベジェ曲線を描画
     */
    private void drawBezierCurve(double[][] img, double[] points, double thickness) {
        if (points.length < 8) return; // 最低4点（8座標）必要

        StrokePoint[] path = createBezierPath(
                new double[]{points[0], points[1]},
                new double[]{points[2], points[3]},
                new double[]{points[4], points[5]},
                new double[]{points[6], points[7]},
                30
        );

        drawStrokeWithPressure(img, path, thickness);
    }

    /**
     * 滑らかな線の描画
     */
    private void drawLineSmooth(double[][] img, double x1, double y1, double x2, double y2, double thickness) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        int steps = Math.max(2, (int)(distance * 2));

        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;

            drawBrush(img, x, y, thickness / 2);
        }
    }

    /**
     * 楕円を描画（改良版）
     */
    private void drawEllipse(double[][] img, int cx, int cy, double radiusX, double radiusY,
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
     * 線を描画（改良版）
     */
    private void drawLine(double[][] img, int x1, int y1, int x2, int y2, double thickness) {
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
    private void drawCurve(double[][] img, int startX, int startY, int endX, int endY,
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

    // 他の数字の描画メソッド（0-7, 9）は元のコードと同じ
    private void drawZero(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;
        double radiusX = 6.0 + rand.nextDouble() * 2;
        double radiusY = 8.0 + rand.nextDouble() * 2;

        if (style == DrawStyle.ANGULAR) {
            // 角張った0（菱形風）
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
        } else {
            // 通常の楕円
            drawEllipse(img, cx, cy, radiusX, radiusY, thickness * 1.5, false);
        }
    }

    private void drawOne(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int x = center + ox;
        int startY = center - 8 + oy;
        int endY = center + 8 + oy;

        // メインの縦線
        if (style == DrawStyle.ROUNDED) {
            // わずかに曲がった1
            int controlX = x + rand.nextInt(3) - 1;
            drawCurve(img, x, startY, x, endY, controlX, center + oy, thickness);
        } else {
            drawLine(img, x, startY, x, endY, thickness);
        }

        // 上部の斜め線
        drawLine(img, x - 3, startY + 2, x, startY, thickness * 0.8);

        // ベース（オプション）
        if (rand.nextDouble() > 0.5) {
            drawLine(img, x - 3, endY, x + 3, endY, thickness * 0.7);
        }
    }

    private void drawTwo(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

        // 上部の曲線
        drawCurve(img, cx - 6, cy - 6, cx + 6, cy - 6, cx, cy - 10, thickness);
        drawCurve(img, cx + 6, cy - 6, cx + 6, cy - 2, cx + 8, cy - 4, thickness);

        // 中央から下への斜線
        if (style == DrawStyle.ANGULAR) {
            // 直線的な2
            drawLine(img, cx + 6, cy - 2, cx - 6, cy + 8, thickness);
        } else {
            // 曲線的な2
            drawCurve(img, cx + 6, cy - 2, cx - 6, cy + 8, cx, cy + 2, thickness);
        }

        // 下部の横線
        drawLine(img, cx - 6, cy + 8, cx + 6, cy + 8, thickness);
    }

    private void drawThree(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

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

    private void drawFour(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

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

    private void drawFive(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

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

    private void drawSix(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

        // 上部の曲線から始まる
        drawCurve(img, cx + 4, cy - 8, cx - 4, cy - 6, cx, cy - 9, thickness);

        // 左の曲線（長い）
        drawCurve(img, cx - 4, cy - 6, cx - 6, cy + 6, cx - 5, cy, thickness);

        // 下部の円
        drawEllipse(img, cx, cy + 3, 5.5, 5.5, thickness * 1.2, false);
    }

    private void drawSeven(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

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

    private void drawNine(double[][] img, int ox, int oy, DrawStyle style, double thickness) {
        int cx = center + ox;
        int cy = center + oy;

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

    /**
     * ランダムな変形を適用（改良版）
     */
    private double[][] applyRandomTransform(double[][] image) {
        // 回転
        double angle = (rand.nextDouble() - 0.5) * 0.4;  // ±20度程度
        image = rotate(image, angle);

        // スケーリング
        double scale = 0.85 + rand.nextDouble() * 0.3;  // 0.85-1.15倍
        if (Math.abs(scale - 1.0) > 0.05) {
            image = scale(image, scale);
        }

        // 歪み（スキュー）
        if (rand.nextDouble() > 0.7) {
            image = skew(image, (rand.nextDouble() - 0.5) * 0.2);
        }

        // 弾性変形
        if (rand.nextDouble() > 0.8) {
            image = elasticDeform(image);
        }

        return image;
    }

    /**
     * スキュー変換
     */
    private double[][] skew(double[][] image, double factor) {
        double[][] skewed = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                int srcX = x - (int)((y - cy) * factor);

                if (srcX >= 0 && srcX < imageSize) {
                    skewed[y][x] = image[y][srcX];
                }
            }
        }

        return skewed;
    }

    /**
     * 弾性変形（簡易版）
     */
    private double[][] elasticDeform(double[][] image) {
        double[][] deformed = new double[imageSize][imageSize];
        double amplitude = 2.0;
        double frequency = 0.1;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double dx = amplitude * Math.sin(frequency * y);
                double dy = amplitude * Math.sin(frequency * x);

                int srcX = (int)(x + dx);
                int srcY = (int)(y + dy);

                if (srcX >= 0 && srcX < imageSize && srcY >= 0 && srcY < imageSize) {
                    deformed[y][x] = image[srcY][srcX];
                }
            }
        }

        return deformed;
    }

    /**
     * 画像を回転
     */
    private double[][] rotate(double[][] image, double angle) {
        double[][] rotated = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                // 回転前の座標を計算
                int dx = x - cx;
                int dy = y - cy;
                double srcX = cos * dx + sin * dy + cx;
                double srcY = -sin * dx + cos * dy + cy;

                // バイリニア補間
                int x0 = (int)Math.floor(srcX);
                int y0 = (int)Math.floor(srcY);
                int x1 = x0 + 1;
                int y1 = y0 + 1;

                if (x0 >= 0 && x1 < imageSize && y0 >= 0 && y1 < imageSize) {
                    double fx = srcX - x0;
                    double fy = srcY - y0;

                    double val = (1 - fx) * (1 - fy) * image[y0][x0] +
                            fx * (1 - fy) * image[y0][x1] +
                            (1 - fx) * fy * image[y1][x0] +
                            fx * fy * image[y1][x1];

                    rotated[y][x] = val;
                }
            }
        }

        return rotated;
    }

    /**
     * 画像をスケーリング（バイリニア補間付き）
     */
    private double[][] scale(double[][] image, double factor) {
        double[][] scaled = new double[imageSize][imageSize];
        int cx = imageSize / 2;
        int cy = imageSize / 2;

        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double srcX = (x - cx) / factor + cx;
                double srcY = (y - cy) / factor + cy;

                // バイリニア補間
                int x0 = (int)Math.floor(srcX);
                int y0 = (int)Math.floor(srcY);
                int x1 = x0 + 1;
                int y1 = y0 + 1;

                if (x0 >= 0 && x1 < imageSize && y0 >= 0 && y1 < imageSize) {
                    double fx = srcX - x0;
                    double fy = srcY - y0;

                    double val = (1 - fx) * (1 - fy) * image[y0][x0] +
                            fx * (1 - fy) * image[y0][x1] +
                            (1 - fx) * fy * image[y1][x0] +
                            fx * fy * image[y1][x1];

                    scaled[y][x] = val;
                }
            }
        }

        return scaled;
    }

    /**
     * ノイズを追加（改良版）
     */
    private void addNoise(double[][] image, double noiseLevel) {
        // ガウシアンノイズ
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                double noise = rand.nextGaussian() * noiseLevel * 0.5;
                image[y][x] = Math.max(0.0, Math.min(1.0, image[y][x] + noise));
            }
        }

        // ソルト＆ペッパーノイズ（低確率）
        if (rand.nextDouble() > 0.8) {
            int numPoints = (int)(imageSize * imageSize * noiseLevel * 0.01);
            for (int i = 0; i < numPoints; i++) {
                int x = rand.nextInt(imageSize);
                int y = rand.nextInt(imageSize);
                image[y][x] = rand.nextDouble() > 0.5 ? 1.0 : 0.0;
            }
        }
    }

    /**
     * 画像をスムージング（改良版ガウシアンフィルタ）
     */
    private double[][] smoothImage(double[][] image) {
        double[][] smoothed = new double[imageSize][imageSize];
        double[][] kernel = {
                {0.0625, 0.125, 0.0625},
                {0.125,  0.25,  0.125},
                {0.0625, 0.125, 0.0625}
        };

        for (int y = 1; y < imageSize - 1; y++) {
            for (int x = 1; x < imageSize - 1; x++) {
                double sum = 0.0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        sum += image[y + ky][x + kx] * kernel[ky + 1][kx + 1];
                    }
                }
                smoothed[y][x] = sum;
            }
        }

        // 境界をコピー
        for (int i = 0; i < imageSize; i++) {
            smoothed[0][i] = image[0][i];
            smoothed[imageSize-1][i] = image[imageSize-1][i];
            smoothed[i][0] = image[i][0];
            smoothed[i][imageSize-1] = image[i][imageSize-1];
        }

        return smoothed;
    }

    /**
     * 画像をコンソールに表示（デバッグ用）
     */
    public static void printImage(double[][] image) {
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                if (image[y][x] > 0.8) {
                    System.out.print("█");
                } else if (image[y][x] > 0.6) {
                    System.out.print("▓");
                } else if (image[y][x] > 0.3) {
                    System.out.print("▒");
                } else if (image[y][x] > 0.1) {
                    System.out.print("░");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    /**
     * テスト用メイン
     */
    public static void main(String[] args) {
        var generator = new MINIST();

        // 8を集中的にテスト
        System.out.println("\n=== Digit 8 - Multiple Variations ===");
        for (int i = 0; i < 10; i++) {
            System.out.println("\nVariation " + (i + 1) + ":");
            double[][] image = generator.generateDigit(8, 0.05);
            printImage(image);
        }

        // 他の数字も簡単にテスト
        System.out.println("\n\n=== Other Digits (0-7, 9) ===");
        for (int digit = 0; digit <= 9; digit++) {
            if (digit != 8) {
                System.out.println("\n--- Digit " + digit + " ---");
                double[][] image = generator.generateDigit(digit, 0.1);
                printImage(image);
            }
        }
    }
}