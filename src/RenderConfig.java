

/**
 * מייצג את הגדרות הציור המתקבלות מהשרת.
 * שימוש ב-record חוסך כתיבת Getters, Setters ואינטחול ידני.
 */
public record RenderConfig(
        String wallCellColor,
        String pathColor,
        boolean drawGrid,
        String gridColor,
        int animationDelayMs
) {
    // הגדרות ברירת מחדל למקרה שהשרת לא זמין או מחזיר שגיאה
    public static RenderConfig createDefault() {
        return new RenderConfig("#222222", "#00AA00", true, "#CCCCCC", 80);
    }
}