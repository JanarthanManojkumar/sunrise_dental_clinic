package view;

import controller.ControllerResult;
import controller.TreatmentController;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
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
import model.Treatment;
import util.UiTheme;

public class TreatmentManagementView extends JDialog {

    private final TreatmentController treatmentController = new TreatmentController();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtName;
    private JTextField txtFee;
    private JLabel lblMessage;

    private Treatment selectedTreatment;

    public TreatmentManagementView(Frame owner) {
        super(owner, "Manage Treatments", true);
        initComponents();
        refreshTable();
        setSize(500, 450);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Manage Treatments");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Fee"}, 0) {
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
        formPanel.add(new JLabel("Fee:"));
        txtFee = new JTextField();
        formPanel.add(txtFee);

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
        BigDecimal fee = (BigDecimal) tableModel.getValueAt(row, 2);
        selectedTreatment = new Treatment(id, (String) tableModel.getValueAt(row, 1), fee);
        txtName.setText(selectedTreatment.getName());
        txtFee.setText(fee.toPlainString());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ControllerResult<List<Treatment>> result = treatmentController.listTreatments();
        if (result.isSuccess()) {
            for (Treatment treatment : result.getData()) {
                tableModel.addRow(new Object[]{treatment.getId(), treatment.getName(), treatment.getFee()});
            }
        } else {
            lblMessage.setText(result.getMessage());
        }
    }

    private void onAdd() {
        BigDecimal fee = parseFee();
        ControllerResult<Treatment> result = treatmentController.addTreatment(txtName.getText(), fee);
        handleResult(result);
    }

    private void onUpdate() {
        if (selectedTreatment == null) {
            lblMessage.setText("Select a treatment row first.");
            return;
        }
        BigDecimal fee = parseFee();
        ControllerResult<Treatment> result = treatmentController.updateTreatment(selectedTreatment,
                txtName.getText(), fee);
        handleResult(result);
    }

    private BigDecimal parseFee() {
        try {
            return new BigDecimal(txtFee.getText().trim());
        } catch (NumberFormatException | ArithmeticException ex) {
            return null;
        }
    }

    private void handleResult(ControllerResult<Treatment> result) {
        if (result.isSuccess()) {
            lblMessage.setForeground(java.awt.Color.BLUE);
            lblMessage.setText("Saved.");
            txtName.setText("");
            txtFee.setText("");
            selectedTreatment = null;
            refreshTable();
        } else {
            lblMessage.setForeground(java.awt.Color.RED);
            lblMessage.setText(result.getMessage());
        }
    }
}
