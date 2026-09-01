package view;

import controller.ControllerResult;
import controller.ReportController;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Appointment;
import model.DailyAppointmentCount;
import model.DentistRevenue;
import util.UiTheme;

public class ReportsView extends JDialog {

    private static final String REPORT_APPOINTMENTS_PER_DAY = "Appointments per day";
    private static final String REPORT_REVENUE_PER_DENTIST = "Revenue per dentist";
    private static final String REPORT_UPCOMING = "Upcoming appointments";

    private final ReportController reportController = new ReportController();

    private JComboBox<String> cmbReportType;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblMessage;

    public ReportsView(Frame owner) {
        super(owner, "Reports", true);
        initComponents();
        setSize(520, 420);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Reports");

        cmbReportType = new JComboBox<>(new String[]{
            REPORT_APPOINTMENTS_PER_DAY, REPORT_REVENUE_PER_DENTIST, REPORT_UPCOMING
        });
        JButton btnGenerate = new JButton("Generate");
        UiTheme.stylePrimaryButton(btnGenerate);
        btnGenerate.addActionListener(e -> onGenerate());

        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> dispose());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(UiTheme.BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Report:"));
        topPanel.add(cmbReportType);
        topPanel.add(btnGenerate);
        topPanel.add(btnBack);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(UiTheme.BACKGROUND);
        northPanel.add(header, BorderLayout.NORTH);
        northPanel.add(topPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(java.awt.Color.RED);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(lblMessage, BorderLayout.SOUTH);
    }

    private void onGenerate() {
        String selected = (String) cmbReportType.getSelectedItem();
        if (REPORT_APPOINTMENTS_PER_DAY.equals(selected)) {
            showAppointmentsPerDay();
        } else if (REPORT_REVENUE_PER_DENTIST.equals(selected)) {
            showRevenuePerDentist();
        } else {
            showUpcomingAppointments();
        }
    }

    private void showAppointmentsPerDay() {
        ControllerResult<List<DailyAppointmentCount>> result = reportController.appointmentsPerDay();
        if (!result.isSuccess()) {
            lblMessage.setText(result.getMessage());
            return;
        }
        tableModel = new DefaultTableModel(new Object[]{"Date", "Appointments"}, 0);
        for (DailyAppointmentCount row : result.getData()) {
            tableModel.addRow(new Object[]{row.date(), row.count()});
        }
        table.setModel(tableModel);
        lblMessage.setText(" ");
    }

    private void showRevenuePerDentist() {
        ControllerResult<List<DentistRevenue>> result = reportController.revenuePerDentist();
        if (!result.isSuccess()) {
            lblMessage.setText(result.getMessage());
            return;
        }
        tableModel = new DefaultTableModel(new Object[]{"Dentist", "Revenue (Rs.)"}, 0);
        for (DentistRevenue row : result.getData()) {
            tableModel.addRow(new Object[]{row.dentistName(), row.revenue()});
        }
        table.setModel(tableModel);
        lblMessage.setText(" ");
    }

    private void showUpcomingAppointments() {
        ControllerResult<List<Appointment>> result = reportController.upcomingAppointments();
        if (!result.isSuccess()) {
            lblMessage.setText(result.getMessage());
            return;
        }
        tableModel = new DefaultTableModel(
                new Object[]{"Appointment No", "Patient", "Dentist", "Date", "Time"}, 0);
        for (Appointment appointment : result.getData()) {
            tableModel.addRow(new Object[]{
                appointment.getAppointmentNo(),
                appointment.getPatient().getName(),
                appointment.getDentist().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
            });
        }
        table.setModel(tableModel);
        lblMessage.setText(" ");
    }
}
