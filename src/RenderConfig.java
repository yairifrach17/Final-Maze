

public record RenderConfig(
        String wallCellColor,
        String pathColor,
        boolean drawGrid,
        String gridColor,
        int animationDelayMs
) {

    public static RenderConfig createDefault() {
        return new RenderConfig("#222222", "#00AA00", true, "#CCCCCC", 80);
    }
}