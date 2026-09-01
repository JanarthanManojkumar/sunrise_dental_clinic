package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill {

    private int id;
    private int appointmentId;
    private BigDecimal consultationFee;
    private BigDecimal treatmentFee;
    private BigDecimal total;
    private LocalDateTime issuedAt;

    public Bill() {
    }

    public Bill(int id, int appointmentId, BigDecimal consultationFee, BigDecimal treatmentFee,
            BigDecimal total, LocalDateTime issuedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.treatmentFee = treatmentFee;
        this.total = total;
        this.issuedAt = issuedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public BigDecimal getTreatmentFee() {
        return treatmentFee;
    }

    public void setTreatmentFee(BigDecimal treatmentFee) {
        this.treatmentFee = treatmentFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
