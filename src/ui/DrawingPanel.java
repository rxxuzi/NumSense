package ui;

import main.CNN;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * 手書き入力用の描画パネル
 */
public class DrawingPanel extends JPanel {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;
    private boolean drawing = false;
    private DrawingListener listener;

    // 設定
    private static final int CANVAS_SIZE = CNN.CANVAS_DISPLAY_SIZE;  // 28x28の10倍
    private static final int BRUSH_SIZE = 16;
    private static final Color DRAWING_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    /**
     * 描画変更リスナーインターフェース
     */
    public interface DrawingListener {
        void onDrawingChanged();
    }

    public DrawingPanel() {
        setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        initializeCanvas();
        setupMouseListeners();
    }

    /**
     * キャンバスの初期化
     */
    private void initializeCanvas() {
        canvas = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        clear();
    }

    /**
     * マウスリスナーの設定
     */
    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    drawing = true;
                    lastX = e.getX();
                    lastY = e.getY();

                    // 点を描画（クリックだけの場合）
                    drawPoint(lastX, lastY);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawing) {
                    drawing = false;
                    notifyDrawingChanged();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing && SwingUtilities.isLeftMouseButton(e)) {
                    int currentX = e.getX();
                    int currentY = e.getY();

                    // 線を描画
                    drawLine(lastX, lastY, currentX, currentY);

                    lastX = currentX;
                    lastY = currentY;
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    /**
     * 点を描画
     */
    private void drawPoint(int x, int y) {
        g2d.setColor(DRAWING_COLOR);
        g2d.fillOval(x - BRUSH_SIZE/2, y - BRUSH_SIZE/2, BRUSH_SIZE, BRUSH_SIZE);
        repaint();
    }

    /**
     * 線を描画
     */
    private void drawLine(int x1, int y1, int x2, int y2) {
        g2d.setColor(DRAWING_COLOR);
        g2d.setStroke(new BasicStroke(BRUSH_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }

    /**
     * キャンバスをクリア
     */
    public void clear() {
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        repaint();
    }

    /**
     * 描画リスナーを設定
     */
    public void addDrawingListener(DrawingListener listener) {
        this.listener = listener;
    }

    /**
     * 描画変更を通知
     */
    private void notifyDrawingChanged() {
        if (listener != null) {
            listener.onDrawingChanged();
        }
    }

    /**
     * 画像を設定（サンプル表示用）
     */
    public void setImage(double[][] image) {
        clear();

        // 28x28の画像を280x280に拡大して描画
        int scale = CANVAS_SIZE / image.length;

        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                int gray = (int)(255 * (1 - image[y][x]));  // 反転（黒地に白文字）
                g2d.setColor(new Color(gray, gray, gray));
                g2d.fillRect(x * scale, y * scale, scale, scale);
            }
        }

        repaint();
        notifyDrawingChanged();
    }

    /**
     * 描画内容を指定サイズにリサイズして取得
     * @param targetWidth 目標幅
     * @param targetHeight 目標高さ
     * @return リサイズされた画像データ（0.0-1.0の範囲）
     */
    public double[][] getResizedImage(int targetWidth, int targetHeight) {
        // アンチエイリアシング付きでリサイズ
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // キャンバスをリサイズして描画
        g.drawImage(canvas, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        // ピクセルデータを取得して正規化
        double[][] result = new double[targetHeight][targetWidth];
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int rgb = resized.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;

                // 白黒反転して正規化（白背景の黒文字→黒背景の白文字）
                result[y][x] = (255 - red) / 255.0;
            }
        }

        return result;
    }

    /**
     * 現在の描画内容を取得（28x28にリサイズ）
     */
    public double[][] getCurrentImage() {
        return getResizedImage(CNN.IMAGE_SIZE, CNN.IMAGE_SIZE);
    }

    /**
     * 描画内容があるかチェック
     */
    public boolean hasDrawing() {
        // キャンバスの中央部分をチェック
        int centerX = CANVAS_SIZE / 2;
        int centerY = CANVAS_SIZE / 2;
        int checkRadius = CANVAS_SIZE / 4;

        for (int y = centerY - checkRadius; y < centerY + checkRadius; y++) {
            for (int x = centerX - checkRadius; x < centerX + checkRadius; x++) {
                int rgb = canvas.getRGB(x, y);
                if (rgb != Color.WHITE.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }

    /**
     * ブラシサイズを設定
     */
    public void setBrushSize(int size) {
        // 将来の拡張用
    }

    /**
     * 描画色を設定
     */
    public void setDrawingColor(Color color) {
        // 将来の拡張用
    }
}