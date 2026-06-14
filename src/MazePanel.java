import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private final MazeModel mazeModel;
    private final RenderConfig config;

    private final List<Point> animatedPath = new ArrayList<>();
    private Timer animationTimer;
    private boolean isAnimating = false;
    private Runnable onAnimationComplete;

    public MazePanel(MazeModel mazeModel, RenderConfig config) {
        this.mazeModel = mazeModel;
        this.config = config;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        // ביטול פילטרים שגורמים לטשטוש בקירות
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int width = mazeModel.getWidth();
        int height = mazeModel.getHeight();

        // השארת מרווח ביטחון נקי של 40 פיקסלים מכל צד
        int padding = 40;
        int availableWidth = getWidth() - (padding * 2);
        int availableHeight = getHeight() - (padding * 2);

        if (availableWidth < 10) availableWidth = 10;
        if (availableHeight < 10) availableHeight = 10;

        // חישוב מקדם הכיווץ המדויק כשבר עשרוני
        double scaleX = (double) availableWidth / width;
        double scaleY = (double) availableHeight / height;
        double scale = Math.min(scaleX, scaleY);

        if (scale > 1.0) {
            scale = 1.0;
        }

        // חישוב המרכוז האוטומטי בתוך ה-Panel
        int offsetX = (getWidth() - (int) (width * scale)) / 2;
        int offsetY = (getHeight() - (int) (height * scale)) / 2;

        Color wallColor = Color.decode(config.wallCellColor());
        Color gridColor = Color.decode(config.gridColor());
        Color pathColor = Color.decode(config.pathColor());

        // 1. ציור קירות ומעברים בשיטת Pixel-Perfect
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int x1 = offsetX + (int) (x * scale);
                int y1 = offsetY + (int)(y * scale);
                int x2 = offsetX + (int) ((x + 1) * scale);
                int y2 = offsetY + (int) ((y + 1) * scale);

                int cellW = x2 - x1;
                int cellH = y2 - y1;

                if (mazeModel.isWall(x, y)) {
                    g2d.setColor(wallColor);
                    g2d.fillRect(x1, y1, cellW, cellH);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x1, y1, cellW, cellH);
                }
            }
        }

        // 2. התיקון המנצח: ציור נתיב הפתרון כקו צינור עבה ורציף בין מרכזי התאים
        if (!animatedPath.isEmpty()) {
            // הפעלת החלקה ספציפית לקו הפתרון כדי שייראה מעוגל ויפה
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(pathColor);

            // עובי דינמי: לפחות 3.5 פיקסלים כדי שייראה עבה וברור תמיד, או 70% מגודל התא במבוכים קטנים
            float strokeWidth = Math.max(3.5f, (float) (scale * 0.7));
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < animatedPath.size() - 1; i++) {
                Point p1 = animatedPath.get(i);
                Point p2 = animatedPath.get(i + 1);

                // חישוב מרכז תא 1
                int x1 = offsetX + (int) (p1.x * scale) + (int) (scale / 2);
                int y1 = offsetY + (int) (p1.y * scale) + (int) (scale / 2);

                // חישוב מרכז תא 2
                int x2 = offsetX + (int) (p2.x * scale) + (int) (scale / 2);
                int y2 = offsetY + (int)(p2.y * scale) + (int) (scale / 2);

                g2d.drawLine(x1, y1, x2, y2);
            }

            // מקרה קצה: אם יש רק נקודה אחת בנתיב
            if (animatedPath.size() == 1) {
                Point p = animatedPath.get(0);
                int x = offsetX + (int) (p.x * scale) + (int) (scale / 2);
                int y = offsetY + (int)(p.y * scale) + (int) (scale / 2);
                g2d.drawLine(x, y, x, y);
            }

            // החזרת מצב ההחלקה למצב כבוי בשביל שאר הרכיבים
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // 3. ציור קווי רשת (רק אם התאים גדולים מ-4 פיקסלים)
        if (config.drawGrid() && scale > 4.0) {
            g2d.setColor(gridColor);
            g2d.setStroke(new BasicStroke(1.0f));
            for (int x = 0; x <= width; x++) {
                int lineX = offsetX + (int) (x * scale);
                g2d.drawLine(lineX, offsetY, lineX, offsetY + (int) (height * scale));
            }
            for (int y = 0; y <= height; y++) {
                int lineY = offsetY + (int) (y * scale);
                g2d.drawLine(offsetX, lineY, offsetX + (int) (width * scale), lineY);
            }
        }
    }

    public void startSolutionAnimation(List<Point> fullPath, Runnable onComplete) {
        if (isAnimating) return;

        this.onAnimationComplete = onComplete;
        isAnimating = true;
        animatedPath.clear();
        repaint();

        int hyperSpeedDelay = 1;

        animationTimer = new Timer(hyperSpeedDelay, e -> {
            if (animatedPath.size() < fullPath.size()) {
                for (int i = 0; i < 60; i++) {
                    if (animatedPath.size() < fullPath.size()) {
                        animatedPath.add(fullPath.get(animatedPath.size()));
                    }
                }
                repaint();
            } else {
                animationTimer.stop();
                isAnimating = false;
                if (onAnimationComplete != null) {
                    onAnimationComplete.run();
                }
            }
        });

        animationTimer.start();
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}