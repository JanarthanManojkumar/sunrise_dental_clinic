package view;

import controller.ControllerResult;
import controller.DentistController;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Dentist;
import util.UiTheme;

public class DentistManagementView extends JDialog {

    private final DentistController dentistController = new DentistController();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtName;
    private JTextField txtSpecialization;
    private JLabel lblMessage;

    private Dentist selectedDentist;

    public DentistManagementView(Frame owner) {
        super(owner, "Manage Dentists", true);
        initComponents();
        refreshTable();
        setSize(500, 450);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Manage Dentists");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Specialization"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> onRowSelected());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        formPanel.setBackground(UiTheme.BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);
        formPanel.add(new JLabel("Specialization:"));
        txtSpecialization = new JTextField();
        formPanel.add(txtSpecialization);

        JButton btnAdd = new JButton("Add");
        UiTheme.stylePrimaryButton(btnAdd);
        btnAdd.addActionListener(e -> onAdd());
        JButton btnUpdate = new JButton("Update Selected");
        btnUpdate.addActionListener(e -> onUpdate());
        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UiTheme.BACKGROUND);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnBack);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setForeground(java.awt.Color.RED);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UiTheme.BACKGROUND);
        southPanel.add(formPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(lblMessage, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        selectedDentist = new Dentist(id, (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2));
        txtName.setText(selectedDentist.getName());
        txtSpecialization.setText(selectedDentist.getSpecialization());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ControllerResult<List<Dentist>> result = dentistController.listDentists();
        if (result.isSuccess()) {
            for (Dentist dentist : result.getData()) {
                tableModel.addRow(new Object[]{dentist.getId(), dentist.getName(), dentist.getSpecialization()});
            }
        } else {
            lblMessage.setText(result.getMessage());
        }
    }

    private void onAdd() {
        ControllerResult<Dentist> result = dentistController.addDentist(txtName.getText(), txtSpecialization.getText());
        handleResult(result);
    }

    private void onUpdate() {
        if (selectedDentist == null) {
            lblMessage.setText("Select a dentist row first.");
            return;
        }
        ControllerResult<Dentist> result = dentistController.updateDentist(selectedDentist, txtName.getText(),
                txtSpecialization.getText());
        handleResult(result);
    }

    private void handleResult(ControllerResult<Dentist> result) {
        if (result.isSuccess()) {
            lblMessage.setForeground(java.awt.Color.BLUE);
            lblMessage.setText("Saved.");
            txtName.setText("");
            txtSpecialization.setText("");
            selectedDentist = null;
            refreshTable();
        } else {
            lblMessage.setForeground(java.awt.Color.RED);
            lblMessage.setText(result.getMessage());
        }
    }
}
