package view;

import controller.AppointmentController;
import controller.ControllerResult;
import controller.DentistController;
import controller.TreatmentController;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import model.Appointment;
import model.Dentist;
import model.Treatment;
import util.UiTheme;

/**
 * One dialog handles both Register (new appointment, all fields editable)
 * and Update (existing appointment, patient details locked, schedule fields
 * editable) so the two flows don't duplicate the same form layout twice.
 */
public class AppointmentFormView extends JDialog {

    public enum Mode {
        REGISTER, UPDATE
    }

    private final Mode mode;
    private final Appointment existingAppointment;
    private final AppointmentController appointmentController = new AppointmentController();

    private JTextField txtPatientName;
    private JTextField txtAddress;
    private JTextField txtContact;
    private JTextField txtEmail;
    private JComboBox<Dentist> cmbDentist;
    private JComboBox<Treatment> cmbTreatment;
    private JTextField txtDate;
    private JTextField txtTime;
    private JLabel lblMessage;

    public AppointmentFormView(Frame owner, Mode mode, Appointment existingAppointment) {
        super(owner, mode == Mode.REGISTER ? "Register Appointment" : "Update Appointment", true);
        this.mode = mode;
        this.existingAppointment = existingAppointment;
        initComponents();
        if (mode == Mode.UPDATE && existingAppointment != null) {
            populateForUpdate();
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner(mode == Mode.REGISTER ? "Register Appointment" : "Update Appointment");

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 12));
        formPanel.setBackground(UiTheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        formPanel.add(new JLabel("Patient Name:"));
        txtPatientName = new JTextField();
        formPanel.add(txtPatientName);

        formPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        formPanel.add(txtAddress);

        formPanel.add(new JLabel("Contact Number:"));
        txtContact = new JTextField();
        formPanel.add(txtContact);

        formPanel.add(new JLabel("Email (for e-bills):"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        formPanel.add(new JLabel("Dentist:"));
        cmbDentist = new JComboBox<>();
        formPanel.add(cmbDentist);

        formPanel.add(new JLabel("Treatment:"));
        cmbTreatment = new JComboBox<>();
        formPanel.add(cmbTreatment);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField();
        formPanel.add(txtDate);

        formPanel.add(new JLabel("Time (HH:mm):"));
        txtTime = new JTextField();
        formPanel.add(txtTime);

        loadDentists();
        loadTreatments();

        if (mode == Mode.UPDATE) {
            txtPatientName.setEditable(false);
            txtAddress.setEditable(false);
            txtContact.setEditable(false);
            txtEmail.setEditable(false);
        }

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(java.awt.Color.RED);

        JButton btnSubmit = new JButton(mode == Mode.REGISTER ? "Register" : "Save Changes");
        UiTheme.stylePrimaryButton(btnSubmit);
        btnSubmit.addActionListener(e -> onSubmit());

        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> dispose());

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setBackground(UiTheme.BACKGROUND);
        actionButtonPanel.add(btnSubmit);
        actionButtonPanel.add(btnBack);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UiTheme.BACKGROUND);
        southPanel.add(actionButtonPanel, BorderLayout.NORTH);
        southPanel.add(lblMessage, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadDentists() {
        DentistController dentistController = new DentistController();
        ControllerResult<List<Dentist>> result = dentistController.listDentists();
        if (result.isSuccess()) {
            for (Dentist dentist : result.getData()) {
                cmbDentist.addItem(dentist);
            }
        }
    }

    private void loadTreatments() {
        TreatmentController treatmentController = new TreatmentController();
        ControllerResult<List<Treatment>> result = treatmentController.listTreatments();
        if (result.isSuccess()) {
            for (Treatment treatment : result.getData()) {
                cmbTreatment.addItem(treatment);
            }
        }
    }

    private void populateForUpdate() {
        txtPatientName.setText(existingAppointment.getPatient().getName());
        txtAddress.setText(existingAppointment.getPatient().getAddress());
        txtContact.setText(existingAppointment.getPatient().getContactNumber());
        txtEmail.setText(existingAppointment.getPatient().getEmail());
        selectComboItem(cmbDentist, existingAppointment.getDentist());
        selectComboItem(cmbTreatment, existingAppointment.getTreatment());
        txtDate.setText(existingAppointment.getAppointmentDate().toString());
        txtTime.setText(existingAppointment.getAppointmentTime().toString());
    }

    private <T> void selectComboItem(JComboBox<T> comboBox, T match) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(match)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void onSubmit() {
        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(txtDate.getText().trim());
        } catch (DateTimeParseException ex) {
            lblMessage.setText("Invalid date, use YYYY-MM-DD");
            return;
        }
        try {
            time = LocalTime.parse(txtTime.getText().trim());
        } catch (DateTimeParseException ex) {
            lblMessage.setText("Invalid time, use HH:mm");
            return;
        }

        Dentist dentist = (Dentist) cmbDentist.getSelectedItem();
        Treatment treatment = (Treatment) cmbTreatment.getSelectedItem();

        ControllerResult<Appointment> result;
        if (mode == Mode.REGISTER) {
            result = appointmentController.registerAppointment(txtPatientName.getText(), txtAddress.getText(),
                    txtContact.getText(), txtEmail.getText(), dentist, treatment, date, time);
        } else {
            result = appointmentController.updateAppointment(existingAppointment, dentist, treatment, date, time);
        }

        if (result.isSuccess()) {
            lblMessage.setForeground(java.awt.Color.BLUE);
            Appointment saved = result.getData();
            JOptionPane.showMessageDialog(this,
                    (mode == Mode.REGISTER ? "Appointment registered: " : "Appointment updated: ")
                            + saved.getAppointmentNo());
            dispose();
        } else {
            lblMessage.setForeground(java.awt.Color.RED);
            lblMessage.setText(result.getMessage());
        }
    }
}
