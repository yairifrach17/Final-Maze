import java.awt.Point;
import java.util.*;

public class MazeSolver {

    /**
     * שלב 10: פונקציה שמחפשת מסלול מהפינה השמאלית העליונה לימנית התחתונה
     * מחזירה רשימה של נקודות (המסלול), או רשימה ריקה אם אין פתרון
     */
    public static List<Point> solve(MazeModel maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        List<Point> path = new ArrayList<>();

        // הגדרת נקודת התחלה ונקודת סיום
        Point start = new Point(0, 0);
        Point end = new Point(width - 1, height - 1);

        // תנאי הגנה מההנחיות: אם ההתחלה או הסיום הם קיר - אין פתרון
        if (maze.isWall(start.x, start.y) || maze.isWall(end.x, end.y)) {
            return path; // מחזיר רשימה ריקה
        }

        // תור לצורך סריקת ה-BFS ומערך מעקב אחרי משבצות שכבר ביקרנו בהן
        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[width][height];

        // מפה שתעזור לנו לשחזר את המסלול בסוף (מפתח: משבצת נוכחית, ערך: המשבצת שממנה הגענו אליה)
        Map<Point, Point> parentMap = new HashMap<>();

        // התחלת הסריקה מנקודת המוצא
        queue.add(start);
        visited[start.x][start.y] = true;

        boolean found = false;

        // 4 כיווני תנועה מותרים: ימינה, למטה, שמאלה, למעלה (בלי אלכסונים!)
        int[] dX = {1, 0, -1, 0};
        int[] dY = {0, 1, 0, -1};

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // אם הגענו ליעד - עוצרים את הסריקה
            if (current.equals(end)) {
                found = true;
                break;
            }

            // בדיקת 4 הכיוונים מסביב למשבצת הנוכחית
            for (int i = 0; i < 4; i++) {
                int nextX = current.x + dX[i];
                int nextY = current.y + dY[i];
                Point nextPoint = new Point(nextX, nextY);

                // בדיקה שהמשבצת הבאה חוקית: בתוך הגבולות, היא לא קיר, ועדיין לא ביקרנו בה
                if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
                    if (!maze.isWall(nextX, nextY) && !visited[nextX][nextY]) {
                        visited[nextX][nextY] = true;
                        parentMap.put(nextPoint, current); // שומרים מאיפה הגענו אליה
                        queue.add(nextPoint);
                    }
                }
            }
        }

        // אם מצאנו פתרון, נשחזר את המסלול מהסוף להתחלה באמצעות ה-parentMap
        if (found) {
            Point curr = end;
            while (curr != null) {
                path.add(0, curr); // מוסיף תמיד לתחילת הרשימה כדי שהמסלול יהיה ישר (מההתחלה לסוף)
                curr = parentMap.get(curr);
            }
        }

        return path;
    }
}