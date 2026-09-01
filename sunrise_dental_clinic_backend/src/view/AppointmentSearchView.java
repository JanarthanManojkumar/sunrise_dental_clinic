package view;

import controller.AppointmentController;
import controller.BillingController;
import controller.ControllerResult;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.Appointment;
import util.UiTheme;

public class AppointmentSearchView extends JDialog {

    private final AppointmentController appointmentController = new AppointmentController();
    private final BillingController billingController = new BillingController();

    private JTextField txtAppointmentNo;
    private JLabel lblPatient;
    private JLabel lblDentist;
    private JLabel lblTreatment;
    private JLabel lblDateTime;
    private JLabel lblStatus;
    private JLabel lblMessage;

    private JButton btnUpdate;
    private JButton btnCancel;
    private JButton btnBill;

    private Appointment currentAppointment;

    public AppointmentSearchView(Frame owner) {
        super(owner, "Search Appointment", true);
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Search / Manage Appointment");

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBackground(UiTheme.BACKGROUND);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        searchPanel.add(new JLabel("Appointment No:"), BorderLayout.WEST);
        txtAppointmentNo = new JTextField(18);
        searchPanel.add(txtAppointmentNo, BorderLayout.CENTER);
        JButton btnSearch = new JButton("Search");
        UiTheme.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> onSearch());
        searchPanel.add(btnSearch, BorderLayout.EAST);

        JPanel detailsPanel = new JPanel(new GridLayout(5, 2, 6, 6));
        detailsPanel.setBackground(UiTheme.BACKGROUND);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        detailsPanel.add(new JLabel("Patient:"));
        lblPatient = new JLabel("-");
        detailsPanel.add(lblPatient);
        detailsPanel.add(new JLabel("Dentist:"));
        lblDentist = new JLabel("-");
        detailsPanel.add(lblDentist);
        detailsPanel.add(new JLabel("Treatment:"));
        lblTreatment = new JLabel("-");
        detailsPanel.add(lblTreatment);
        detailsPanel.add(new JLabel("Date / Time:"));
        lblDateTime = new JLabel("-");
        detailsPanel.add(lblDateTime);
        detailsPanel.add(new JLabel("Status:"));
        lblStatus = new JLabel("-");
        detailsPanel.add(lblStatus);

        JPanel actionPanel = new JPanel();
        btnUpdate = new JButton("Update");
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> onUpdate());
        btnCancel = new JButton("Cancel Appointment");
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(e -> onCancel());
        btnBill = new JButton("Calculate & Print Bill");
        btnBill.setEnabled(false);
        btnBill.addActionListener(e -> onBill());
        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> dispose());

        actionPanel.setBackground(UiTheme.BACKGROUND);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnCancel);
        actionPanel.add(btnBill);
        actionPanel.add(btnBack);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(java.awt.Color.RED);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UiTheme.BACKGROUND);
        centerPanel.add(detailsPanel, BorderLayout.CENTER);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(UiTheme.BACKGROUND);
        northPanel.add(header, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(lblMessage, BorderLayout.SOUTH);
    }

    private void onSearch() {
        ControllerResult<Appointment> result = appointmentController.searchAppointment(txtAppointmentNo.getText().trim());
        if (result.isSuccess()) {
            displayAppointment(result.getData());
            lblMessage.setText(" ");
        } else {
            clearAppointment();
            lblMessage.setText(result.getMessage());
        }
    }

    private void displayAppointment(Appointment appointment) {
        this.currentAppointment = appointment;
        lblPatient.setText(appointment.getPatient().getName());
        lblDentist.setText(appointment.getDentist().getName());
        lblTreatment.setText(appointment.getTreatment().getName());
        lblDateTime.setText(appointment.getAppointmentDate() + " " + appointment.getAppointmentTime());
        lblStatus.setText(appointment.getStatus().toString());
        boolean active = appointment.getStatus() == model.AppointmentStatus.SCHEDULED;
        btnUpdate.setEnabled(active);
        btnCancel.setEnabled(active);
        btnBill.setEnabled(true);
    }

    private void clearAppointment() {
        this.currentAppointment = null;
        lblPatient.setText("-");
        lblDentist.setText("-");
        lblTreatment.setText("-");
        lblDateTime.setText("-");
        lblStatus.setText("-");
        btnUpdate.setEnabled(false);
        btnCancel.setEnabled(false);
        btnBill.setEnabled(false);
    }

    private void onUpdate() {
        AppointmentFormView form = new AppointmentFormView((Frame) getOwner(),
                AppointmentFormView.Mode.UPDATE, currentAppointment);
        form.setVisible(true);
        onSearch();
    }

    private void onCancel() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel appointment " + currentAppointment.getAppointmentNo() + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        ControllerResult<Appointment> result = appointmentController.cancelAppointment(
                currentAppointment.getAppointmentNo());
        if (result.isSuccess()) {
            displayAppointment(result.getData());
            lblMessage.setForeground(java.awt.Color.BLUE);
            lblMessage.setText("Appointment cancelled.");
        } else {
            lblMessage.setForeground(java.awt.Color.RED);
            lblMessage.setText(result.getMessage());
        }
    }

    private void onBill() {
        ControllerResult<String> result = billingController.generateBill(currentAppointment.getAppointmentNo());
        if (result.isSuccess()) {
            new ReceiptView((Frame) getOwner(), result.getData(), billingController,
                    currentAppointment.getAppointmentNo()).setVisible(true);
        } else {
            lblMessage.setForeground(java.awt.Color.RED);
            lblMessage.setText(result.getMessage());
        }
    }
}
