import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MainFrame extends JFrame {
    private final JTextField widthField;
    private final JTextField heightField;
    private final JLabel configInfoLabel;
    private final JButton refreshButton;
    private final JButton getMazeButton;
    private final JButton checkSolutionButton;
    private final JPanel centerPanel;

    private RenderConfig currentConfig;
    private MazeModel currentMaze;
    private MazePanel mazePanel;

    public MainFrame() {
        super("משחק המבוך הויזואלי");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        widthField = new JTextField("30", 5);
        heightField = new JTextField("30", 5);
        configInfoLabel = new JLabel("טוען הגדרות מהשרת...", SwingConstants.CENTER);
        refreshButton = new JButton("Refresh Config");
        getMazeButton = new JButton("GET MAZE");
        checkSolutionButton = new JButton("Check Solution");
        checkSolutionButton.setEnabled(false);

        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Width (5-100):"));
        inputPanel.add(widthField);
        inputPanel.add(new JLabel("Height (5-100):"));
        inputPanel.add(heightField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(getMazeButton);
        buttonPanel.add(checkSolutionButton);

        topPanel.add(configInfoLabel);
        topPanel.add(inputPanel);
        topPanel.add(buttonPanel);
        add(topPanel, BorderLayout.NORTH);

        centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> loadConfigFromServer());
        getMazeButton.addActionListener(e -> loadMazeFromServer());
        checkSolutionButton.addActionListener(e -> handleCheckSolution());

        loadConfigFromServer();
    }

    private void loadConfigFromServer() {
        configInfoLabel.setText("שולף הגדרות מהשרת, אנא המתן...");
        new Thread(() -> {
            currentConfig = NetworkService.getRenderConfig();
            SwingUtilities.invokeLater(() -> {
                configInfoLabel.setText(String.format(
                        "הגדרות: צבע קיר: %s | נתיב: %s | רשת: %b | עיכוב: %dms",
                        currentConfig.wallCellColor(), currentConfig.pathColor(),
                        currentConfig.drawGrid(), currentConfig.animationDelayMs()
                ));
            });
        }).start();
    }

    private void loadMazeFromServer() {
        int requestedWidth = parseDimension(widthField.getText());
        int requestedHeight = parseDimension(heightField.getText());

        widthField.setText(String.valueOf(requestedWidth));
        heightField.setText(String.valueOf(requestedHeight));

        configInfoLabel.setText("מוריד תמונת מבוך מהשרת...");
        checkSolutionButton.setEnabled(false);

        new Thread(() -> {
            BufferedImage mazeImg = NetworkService.getMazeImage(requestedWidth, requestedHeight);

            if (mazeImg == null) {
                SwingUtilities.invokeLater(() -> configInfoLabel.setText("שגיאה: נכשל בהורדת המבוך."));
                return;
            }

            // לוקחים את הממדים האמיתיים של מה שהתקבל לעיבוד הפיקסלים
            int actualWidth = mazeImg.getWidth();
            int actualHeight = mazeImg.getHeight();

            currentMaze = new MazeModel(mazeImg, actualWidth, actualHeight);

            SwingUtilities.invokeLater(() -> {
                configInfoLabel.setText("המבוך נטען בהצלחה! לחץ Check Solution לפתרון.");

                centerPanel.removeAll();
                mazePanel = new MazePanel(currentMaze, currentConfig);
                centerPanel.add(mazePanel, BorderLayout.CENTER); // ממלא את כל שטח הפאנל
                centerPanel.revalidate();
                centerPanel.repaint();

                checkSolutionButton.setEnabled(true);
            });
        }).start();
    }

    private void handleCheckSolution() {
        if (currentMaze == null || mazePanel == null || mazePanel.isAnimating()) return;

        List<Point> solution = MazeSolver.solve(currentMaze);

        if (solution.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No solution found", "תוצאה", JOptionPane.WARNING_MESSAGE);
        } else {
            setControlsEnabled(false);
            mazePanel.startSolutionAnimation(solution, () -> {
                setControlsEnabled(true);
            });
        }
    }

    private void setControlsEnabled(boolean enabled) {
        getMazeButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        checkSolutionButton.setEnabled(enabled);
        widthField.setEnabled(enabled);
        heightField.setEnabled(enabled);
    }

    private int parseDimension(String text) {
        try {
            int val = Integer.parseInt(text.trim());
            if (val >= 5 && val <= 100) {
                return val;
            }
        } catch (NumberFormatException e) { }
        return 30; // ברירת מחדל
    }
}