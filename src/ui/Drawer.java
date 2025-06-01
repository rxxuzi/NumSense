package ui;

import main.CNN;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Drawer extends JPanel {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;
    private boolean drawing = false;
    private DrawingListener listener;

    // デザイン設定
    private static final int PANEL_SIZE = 280;
    private static final int BRUSH_SIZE = 18;
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 252);
    private static final Color STROKE_COLOR = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    public interface DrawingListener {
        void onDrawingChanged();
    }

    public Drawer() {
        setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        initializeCanvas();
        setupMouseListeners();
    }

    private void initializeCanvas() {
        canvas = new BufferedImage(PANEL_SIZE, PANEL_SIZE, BufferedImage.TYPE_INT_RGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        clear();
    }

    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    drawing = true;
                    lastX = e.getX();
                    lastY = e.getY();
                    drawPoint(lastX, lastY);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawing) {
                    drawing = false;
                    if (listener != null) {
                        listener.onDrawingChanged();
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing && SwingUtilities.isLeftMouseButton(e)) {
                    int currentX = e.getX();
                    int currentY = e.getY();
                    drawSmoothLine(lastX, lastY, currentX, currentY);
                    lastX = currentX;
                    lastY = currentY;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void drawPoint(int x, int y) {
        g2d.setColor(STROKE_COLOR);
        // ソフトなエッジのブラシ
        Graphics2D g2 = (Graphics2D) g2d.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2.fillOval(x - BRUSH_SIZE/2, y - BRUSH_SIZE/2, BRUSH_SIZE, BRUSH_SIZE);
        g2.dispose();
        repaint();
    }

    private void drawSmoothLine(int x1, int y1, int x2, int y2) {
        g2d.setColor(STROKE_COLOR);
        g2d.setStroke(new BasicStroke(BRUSH_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // より滑らかな線のために中間点を補間
        int steps = Math.max(1, (int)Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) / 2);

        for (int i = 0; i <= steps; i++) {
            float t = (float)i / steps;
            int x = (int)(x1 + t * (x2 - x1));
            int y = (int)(y1 + t * (y2 - y1));

            Graphics2D g2 = (Graphics2D) g2d.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            g2.fillOval(x - BRUSH_SIZE/2, y - BRUSH_SIZE/2, BRUSH_SIZE, BRUSH_SIZE);
            g2.dispose();
        }

        repaint();
    }

    public void clear() {
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        repaint();
    }

    public void addDrawingListener(DrawingListener listener) {
        this.listener = listener;
    }

    public void setImage(double[][] image) {
        clear();

        int scale = PANEL_SIZE / image.length;

        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                int intensity = (int)(255 * image[y][x]);
                g2d.setColor(new Color(intensity, intensity, intensity));
                g2d.fillRect(x * scale, y * scale, scale, scale);
            }
        }

        repaint();
        if (listener != null) {
            listener.onDrawingChanged();
        }
    }

    public double[][] getCurrentImage() {
        return getResizedImage(CNN.IMAGE_SIZE, CNN.IMAGE_SIZE);
    }

    private double[][] getResizedImage(int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(canvas, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        double[][] result = new double[targetHeight][targetWidth];
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int rgb = resized.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                // 背景が白なので反転
                result[y][x] = (255 - red) / 255.0;
            }
        }

        return result;
    }

    public boolean hasDrawing() {
        // 中央部分をチェック
        int centerX = PANEL_SIZE / 2;
        int centerY = PANEL_SIZE / 2;
        int checkRadius = PANEL_SIZE / 4;

        for (int y = centerY - checkRadius; y < centerY + checkRadius; y++) {
            for (int x = centerX - checkRadius; x < centerX + checkRadius; x++) {
                int rgb = canvas.getRGB(x, y);
                if (rgb != BACKGROUND_COLOR.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 影を描画
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRect(3, 3, getWidth() - 3, getHeight() - 3);

        // キャンバスを描画
        g2.drawImage(canvas, 0, 0, null);
    }
}