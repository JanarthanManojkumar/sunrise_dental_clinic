package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import util.UiTheme;

public class HelpView extends JDialog {

    private static final String HELP_TEXT =
            "SUNRISE DENTAL CLINIC - STAFF HELP GUIDE\n"
            + "=========================================\n\n"
            + "1. LOGIN\n"
            + "   Enter your username and password, then click Login.\n"
            + "   Receptionist accounts can register/search/bill appointments.\n"
            + "   Admin accounts can additionally manage dentists, treatments and view reports.\n\n"
            + "2. REGISTER APPOINTMENT\n"
            + "   Click 'Register Appointment', fill in the patient's name, address and contact\n"
            + "   number, choose a dentist and treatment, then enter the date (YYYY-MM-DD) and\n"
            + "   time (HH:mm). The appointment number is generated automatically.\n\n"
            + "3. SEARCH / MANAGE APPOINTMENT\n"
            + "   Click 'Search / Manage Appointment' and enter the appointment number to view\n"
            + "   its details. From there you can Update the schedule, Cancel the appointment,\n"
            + "   or Calculate & Print its Bill.\n\n"
            + "4. CALCULATE & PRINT BILL\n"
            + "   From the search screen, click 'Calculate & Print Bill'. The total is the fixed\n"
            + "   consultation fee plus the chosen treatment's fee. Use Print to send it to a\n"
            + "   printer, or Close to dismiss the receipt.\n\n"
            + "5. ADMIN: MANAGE DENTISTS / TREATMENTS\n"
            + "   Add a new dentist/treatment using the form fields and the Add button, or select\n"
            + "   an existing row in the table and click Update Selected to edit it.\n\n"
            + "6. ADMIN: REPORTS\n"
            + "   Choose a report (appointments per day, revenue per dentist, or upcoming\n"
            + "   appointments) and click Generate.\n\n"
            + "7. EXIT\n"
            + "   Click Exit on the main menu and confirm to close the application safely.\n";

    public HelpView(Frame owner) {
        super(owner, "Help", true);
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Help");

        JTextArea txtHelp = new JTextArea(HELP_TEXT, 22, 60);
        txtHelp.setEditable(false);
        txtHelp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtHelp.setBorder(UiTheme.paddedBorder(10, 10, 10, 10));

        JButton btnClose = new JButton("Back to Dashboard");
        UiTheme.stylePrimaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UiTheme.BACKGROUND);
        buttonPanel.add(btnClose);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(txtHelp), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }
}
