package util;

import model.Appointment;
import model.Bill;

/**
 * Factory pattern: builds a formatted receipt string for a bill without the
 * caller (BillingController / BillingView) needing to know the concrete
 * layout logic, so the receipt format can change independently.
 */
public final class ReceiptFactory {

    private ReceiptFactory() {
    }

    public static String createReceipt(Appointment appointment, Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      SUNRISE DENTAL CLINIC - RECEIPT\n");
        sb.append("========================================\n");
        sb.append("Appointment No : ").append(appointment.getAppointmentNo()).append('\n');
        sb.append("Patient        : ").append(appointment.getPatient().getName()).append('\n');
        sb.append("Dentist        : ").append(appointment.getDentist().getName()).append('\n');
        sb.append("Treatment      : ").append(appointment.getTreatment().getName()).append('\n');
        sb.append("Date/Time      : ").append(appointment.getAppointmentDate())
                .append(' ').append(appointment.getAppointmentTime()).append('\n');
        sb.append("----------------------------------------\n");
        sb.append(String.format("Consultation Fee : Rs. %,.2f%n", bill.getConsultationFee()));
        sb.append(String.format("Treatment Fee    : Rs. %,.2f%n", bill.getTreatmentFee()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL            : Rs. %,.2f%n", bill.getTotal()));
        sb.append("========================================\n");
        sb.append("Issued at: ").append(bill.getIssuedAt()).append('\n');
        return sb.toString();
    }
}
