package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.Role;
import model.User;
import util.SessionManager;
import util.SessionTimeoutMonitor;
import util.UiTheme;

public class MainMenuView extends JFrame {

    private final User loggedInUser;

    public MainMenuView(User loggedInUser) {
        super("Sunrise Dental Clinic - Main Menu");
        this.loggedInUser = loggedInUser;
        getContentPane().setBackground(UiTheme.BACKGROUND);
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setLocationRelativeTo(null);
        SessionTimeoutMonitor.start(this::onSessionTimeout);
    }

    private void initComponents() {
        JLabel header = UiTheme.headerBanner("Sunrise Dental Clinic");

        JLabel lblWelcome = new JLabel(
                "Welcome, " + loggedInUser.getUsername() + "  (" + loggedInUser.getRole() + ")",
                SwingConstants.CENTER);
        lblWelcome.setFont(UiTheme.HEADING_FONT);
        lblWelcome.setForeground(UiTheme.PRIMARY_DARK);
        lblWelcome.setBorder(UiTheme.paddedBorder(14, 10, 14, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UiTheme.BACKGROUND);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(lblWelcome, BorderLayout.SOUTH);

        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        int buttonCount = isAdmin ? 8 : 5;
        JPanel buttonPanel = new JPanel(new GridLayout(buttonCount, 1, 10, 10));
        buttonPanel.setBackground(UiTheme.BACKGROUND);
        buttonPanel.setBorder(UiTheme.paddedBorder(15, 60, 20, 60));

        JButton btnRegister = new JButton("Register Appointment");
        btnRegister.addActionListener(e ->
                new AppointmentFormView(this, AppointmentFormView.Mode.REGISTER, null).setVisible(true));
        buttonPanel.add(styled(btnRegister));

        JButton btnSearch = new JButton("Search / Manage Appointment");
        btnSearch.addActionListener(e -> new AppointmentSearchView(this).setVisible(true));
        buttonPanel.add(styled(btnSearch));

        if (isAdmin) {
            JButton btnDentists = new JButton("Manage Dentists");
            btnDentists.addActionListener(e -> new DentistManagementView(this).setVisible(true));
            buttonPanel.add(styled(btnDentists));

            JButton btnTreatments = new JButton("Manage Treatments");
            btnTreatments.addActionListener(e -> new TreatmentManagementView(this).setVisible(true));
            buttonPanel.add(styled(btnTreatments));

            JButton btnReports = new JButton("Reports");
            btnReports.addActionListener(e -> new ReportsView(this).setVisible(true));
            buttonPanel.add(styled(btnReports));
        }

        JButton btnHelp = new JButton("Help");
        btnHelp.addActionListener(e -> new HelpView(this).setVisible(true));
        buttonPanel.add(styled(btnHelp));

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> onLogout());
        buttonPanel.add(styled(btnLogout));

        JButton btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> onExit());
        buttonPanel.add(styled(btnExit));

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton styled(JButton button) {
        button.setFont(UiTheme.BUTTON_FONT);
        button.setPreferredSize(new Dimension(0, 36));
        return button;
    }

    private void onLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Log out and return to the login screen?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionTimeoutMonitor.stop();
            SessionManager.getInstance().endSession();
            new LoginView().setVisible(true);
            dispose();
        }
    }

    private void onExit() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionTimeoutMonitor.stop();
            SessionManager.getInstance().endSession();
            System.exit(0);
        }
    }

    private void onSessionTimeout() {
        SessionManager.getInstance().endSession();
        JOptionPane.showMessageDialog(this, "Your session has expired due to inactivity. Please log in again.",
                "Session Expired", JOptionPane.WARNING_MESSAGE);
        new LoginView().setVisible(true);
        dispose();
    }
}
