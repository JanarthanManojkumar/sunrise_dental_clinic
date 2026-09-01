package view;

import controller.BillingController;
import controller.ControllerResult;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.print.PrinterException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import util.UiTheme;

public class ReceiptView extends JDialog {

    private final JTextArea txtReceipt;
    private final BillingController billingController;
    private final String appointmentNo;

    public ReceiptView(Frame owner, String receiptText, BillingController billingController, String appointmentNo) {
        super(owner, "Bill Receipt", true);
        this.billingController = billingController;
        this.appointmentNo = appointmentNo;
        getContentPane().setBackground(UiTheme.BACKGROUND);
        JLabel header = UiTheme.headerBanner("Bill Receipt");

        txtReceipt = new JTextArea(receiptText, 18, 45);
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtReceipt.setBorder(UiTheme.paddedBorder(10, 10, 10, 10));

        JButton btnPrint = new JButton("Print");
        UiTheme.stylePrimaryButton(btnPrint);
        btnPrint.addActionListener(e -> onPrint());
        JButton btnEmail = new JButton("Email Bill");
        btnEmail.addActionListener(e -> onEmail());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UiTheme.BACKGROUND);
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnEmail);
        buttonPanel.add(btnClose);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(txtReceipt), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onPrint() {
        try {
            txtReceipt.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing failed or was cancelled: " + e.getMessage());
        }
    }

    private void onEmail() {
        ControllerResult<String> result = billingController.emailBill(appointmentNo);
        JOptionPane.showMessageDialog(this,
                result.isSuccess() ? result.getData() : result.getMessage());
    }
}
