import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

public class NetworkService {

    private static final String CONFIG_URL = "https://backend-qcf9.onrender.com/fm1/get-render-config";
    private static final String MAZE_IMAGE_URL = "https://backend-qcf9.onrender.com/fm1/get-maze-image";

    public static RenderConfig getRenderConfig() {
        try {
            URL url = new URL(CONFIG_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return RenderConfig.createDefault();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String json = response.toString();
            String wallCellColor = parseJsonString(json, "wallCellColor");
            String pathColor = parseJsonString(json, "pathColor");
            boolean drawGrid = json.contains("\"drawGrid\":true");
            String gridColor = parseJsonString(json, "gridColor");
            int animationDelayMs = parseJsonInt(json, "animationDelayMs");

            return new RenderConfig(wallCellColor, pathColor, drawGrid, gridColor, animationDelayMs);

        } catch (Exception e) {
            return RenderConfig.createDefault();
        }
    }

    /**
     * פונקציה השולפת את תמונת המבוך עם הפרמטרים של הגודל בצורה מפורשת
     */
    public static BufferedImage getMazeImage(int width, int height) {
        try {
            // שולחים את הפרמטרים בצורה נקייה ומפורשת לשרת
            String urlWithParams = MAZE_IMAGE_URL + "?width=" + width + "&height=" + height;
            URL url = new URL(urlWithParams);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                return ImageIO.read(conn.getInputStream());
            } else {
                System.out.println("השרת החזיר קוד שגיאה: " + conn.getResponseCode() + " - מנסה לטעון ללא פרמטרים");
                // ניסיון גיבוי במקרה והשרת דוחה את הפרמטרים
                URL fallbackUrl = new URL(MAZE_IMAGE_URL);
                HttpURLConnection fallbackConn = (HttpURLConnection) fallbackUrl.openConnection();
                return ImageIO.read(fallbackConn.getInputStream());
            }
        } catch (Exception e) {
            System.out.println("שגיאה בהורדת תמונת המבוך: " + e.getMessage());
        }
        return null;
    }

    private static String parseJsonString(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return "";
        int startIndex = json.indexOf("\"", keyIndex + key.length() + 2) + 1;
        int endIndex = json.indexOf("\"", startIndex);
        return json.substring(startIndex, endIndex);
    }

    private static int parseJsonInt(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return 0;
        int startIndex = json.indexOf(":", keyIndex) + 1;
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        return Integer.parseInt(json.substring(startIndex, endIndex).trim());
    }
}