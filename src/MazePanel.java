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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int width = mazeModel.getWidth();
        int height = mazeModel.getHeight();

        // חישוב גודל משבצת דינמי
        int cellSizeX = getWidth() / width;
        int cellSizeY = getHeight() / height;
        int cellSize = Math.min(cellSizeX, cellSizeY);
        if (cellSize < 1) cellSize = 1;

        int offsetX = (getWidth() - (width * cellSize)) / 2;
        int offsetY = (getHeight() - (height * cellSize)) / 2;

        Color wallColor = Color.decode(config.wallCellColor());
        Color gridColor = Color.decode(config.gridColor());
        Color pathColor = Color.decode(config.pathColor());

        // 1. ציור קירות ומעברים
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int drawX = offsetX + x * cellSize;
                int drawY = offsetY + y * cellSize;

                if (mazeModel.isWall(x, y)) {
                    g2d.setColor(wallColor);
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(drawX, drawY, cellSize, cellSize);
                }
            }
        }

        // 2. הציור: נחש עבה ושמן במיוחד (רדיוס 8)
        g2d.setColor(pathColor);
        for (Point p : animatedPath) {
            int thicknessRadius = 8;

            for (int dx = -thicknessRadius; dx <= thicknessRadius; dx++) {
                for (int dy = -thicknessRadius; dy <= thicknessRadius; dy++) {
                    int targetX = p.x + dx;
                    int targetY = p.y + dy;

                    if (targetX >= 0 && targetX < width && targetY >= 0 && targetY < height) {
                        if (!mazeModel.isWall(targetX, targetY)) {
                            int drawX = offsetX + targetX * cellSize;
                            int drawY = offsetY + targetY * cellSize;
                            g2d.fillRect(drawX, drawY, cellSize, cellSize);
                        }
                    }
                }
            }
        }

        // 3. ציור קווי הרשת
        if (config.drawGrid() && cellSize > 4) {
            g2d.setColor(gridColor);
            g2d.setStroke(new BasicStroke(1.0f));
            for (int x = 0; x <= width; x++) {
                g2d.drawLine(offsetX + x * cellSize, offsetY, offsetX + x * cellSize, offsetY + height * cellSize);
            }
            for (int y = 0; y <= height; y++) {
                g2d.drawLine(offsetX, offsetY + y * cellSize, offsetX + width * cellSize, offsetY + y * cellSize);
            }
        }
    }

    /**
     * מהירות שיא: הדיליי ירד ל-1 מילישנייה, והדילוג עלה ל-60 משבצות בבת אחת
     */
    public void startSolutionAnimation(List<Point> fullPath, Runnable onComplete) {
        if (isAnimating) return;

        this.onAnimationComplete = onComplete;
        isAnimating = true;
        animatedPath.clear();
        repaint();

        // מהירות מקסימלית של 1 מילישנייה בין פעימה לפעימה
        int hyperSpeedDelay = 1;

        animationTimer = new Timer(hyperSpeedDelay, e -> {
            if (animatedPath.size() < fullPath.size()) {
                // מעלים את קצב ההתקדמות ל-60 נקודות בכל טיק בשביל טיסה חלקה ומהירה במיוחד
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