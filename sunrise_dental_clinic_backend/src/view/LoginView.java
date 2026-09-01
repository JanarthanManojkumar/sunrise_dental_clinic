package view;

import controller.ControllerResult;
import controller.LoginController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.User;
import util.UiTheme;

public class LoginView extends JFrame {

    private final LoginController loginController = new LoginController();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblMessage;

    public LoginView() {
        super("Sunrise Dental Clinic - Login");
        getContentPane().setBackground(UiTheme.BACKGROUND);
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        JLabel header = UiTheme.headerBanner("Sunrise Dental Clinic");

        JLabel subtitle = new JLabel("Staff Login", SwingConstants.CENTER);
        subtitle.setFont(UiTheme.HEADING_FONT);
        subtitle.setForeground(UiTheme.PRIMARY_DARK);
        subtitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 12));
        formPanel.setBackground(UiTheme.BACKGROUND);
        formPanel.setBorder(UiTheme.paddedBorder(0, 40, 0, 40));
        formPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword);

        JButton btnLogin = new JButton("Login");
        UiTheme.stylePrimaryButton(btnLogin);
        btnLogin.setPreferredSize(new java.awt.Dimension(120, 34));
        btnLogin.addActionListener(e -> onLogin());

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(new Color(198, 40, 40));
        lblMessage.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(UiTheme.BACKGROUND);
        buttonPanel.setBorder(UiTheme.paddedBorder(15, 0, 5, 0));
        buttonPanel.add(btnLogin);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UiTheme.BACKGROUND);
        titlePanel.add(header, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(UiTheme.BACKGROUND);
        centerPanel.add(titlePanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UiTheme.BACKGROUND);
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(lblMessage, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLogin);
    }

    private void onLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        ControllerResult<User> result = loginController.login(username, password);
        if (result.isSuccess()) {
            lblMessage.setForeground(new Color(0, 105, 92));
            lblMessage.setText("Login successful!");
            User user = result.getData();
            new MainMenuView(user).setVisible(true);
            dispose();
        } else {
            lblMessage.setForeground(new Color(198, 40, 40));
            lblMessage.setText(result.getMessage());
        }
    }
}
