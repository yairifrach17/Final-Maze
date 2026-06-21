import java.awt.Point;
import java.util.*;

public class MazeSolver {

    public static List<Point> solve(MazeModel maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        List<Point> path = new ArrayList<>();


        Point start = new Point(0, 0);
        Point end = new Point(width - 1, height - 1);


        if (maze.isWall(start.x, start.y) || maze.isWall(end.x, end.y)) {
            return path;
        }


        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[width][height];
        Point[][] parents = new Point[width][height];

        queue.add(start);
        visited[start.x][start.y] = true;

        boolean found = false;

        int[] dX = {1, 0, -1, 0};
        int[] dY = {0, 1, 0, -1};

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(end)) {
                found = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nextX = current.x + dX[i];
                int nextY = current.y + dY[i];
                Point nextPoint = new Point(nextX, nextY);

                if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
                    if (!maze.isWall(nextX, nextY) && !visited[nextX][nextY]) {
                        visited[nextX][nextY] = true;
                        parents[nextX][nextY] = current;
                        queue.add(nextPoint);
                    }
                }
            }
        }


        if (found) {
            Point curr = end;
            while (curr != null) {
                path.add(0, curr);
                curr = parents[curr.x][curr.y];
            }
        }

        return path;
    }
}