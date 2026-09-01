package util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

/**
 * Central place for the app's look and feel so every screen shares the same
 * colors/fonts instead of each view hand-rolling its own styling.
 */
public final class UiTheme {

    public static final Color PRIMARY = new Color(0, 121, 107);
    public static final Color PRIMARY_DARK = new Color(0, 77, 64);
    public static final Color BACKGROUND = new Color(240, 244, 243);
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private UiTheme() {
    }

    public static void apply() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // Fall back silently to the platform default look and feel.
        }
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", new Color(224, 231, 230));
        UIManager.put("nimbusSelectionBackground", PRIMARY);
        UIManager.put("nimbusFocus", PRIMARY_DARK);
        UIManager.put("control", BACKGROUND);
        UIManager.put("text", new Color(33, 33, 33));
        UIManager.put("defaultFont", BODY_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("Label.font", BODY_FONT);
    }

    public static JLabel headerBanner(String title) {
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(PRIMARY);
        label.setForeground(TEXT_ON_PRIMARY);
        label.setFont(TITLE_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return label;
    }

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(TEXT_ON_PRIMARY);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
    }

    public static Border paddedBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}
