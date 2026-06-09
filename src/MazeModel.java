import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MazeModel {
    private final int width;
    private final int height;
    private final boolean[][] isWall; // true = קיר, false = מעבר
    private List<Point> solutionPath;

    public MazeModel(BufferedImage mazeImage, int width, int height) {
        this.width = width;
        this.height = height;
        this.isWall = new boolean[width][height];
        this.solutionPath = new ArrayList<>();

        if (mazeImage != null) {
            decodeImage(mazeImage);
        }
    }

    /**
     * התיקון: סריקה נכונה של פיקסלי התמונה לפי קורדינטות X ו-Y
     */
    private void decodeImage(BufferedImage img) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // הגנה מפני חריגה מגבולות התמונה האמיתית שהגיעה מהשרת
                if (x < imgWidth && y < imgHeight) {
                    int rgb = img.getRGB(x, y);

                    // חילוץ רכיבי הצבע
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    // פיקסל לבן הוא מעבר, כל השאר קיר
                    if (red == 255 && green == 255 && blue == 255) {
                        isWall[x][y] = false;
                    } else {
                        isWall[x][y] = true;
                    }
                } else {
                    isWall[x][y] = true; // ברירת מחדל מחוץ לתמונה - קיר
                }
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        return isWall[x][y];
    }

    public List<Point> getSolutionPath() { return solutionPath; }
    public void setSolutionPath(List<Point> solutionPath) { this.solutionPath = solutionPath; }
}