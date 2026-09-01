package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    private int id;
    private String appointmentNo;
    private Patient patient;
    private Dentist dentist;
    private Treatment treatment;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;

    private Appointment(Builder builder) {
        this.id = builder.id;
        this.appointmentNo = builder.appointmentNo;
        this.patient = builder.patient;
        this.dentist = builder.dentist;
        this.treatment = builder.treatment;
        this.appointmentDate = builder.appointmentDate;
        this.appointmentTime = builder.appointmentTime;
        this.status = builder.status;
    }

    public Appointment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppointmentNo() {
        return appointmentNo;
    }

    public void setAppointmentNo(String appointmentNo) {
        this.appointmentNo = appointmentNo;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Dentist getDentist() {
        return dentist;
    }

    public void setDentist(Dentist dentist) {
        this.dentist = dentist;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    /**
     * Builder pattern: Appointment has several optional/late-bound fields
     * (id and appointmentNo are only known after persistence, status defaults
     * to SCHEDULED) so a fluent builder keeps construction readable at the
     * call site instead of a long positional constructor.
     */
    public static class Builder {

        private int id;
        private String appointmentNo;
        private Patient patient;
        private Dentist dentist;
        private Treatment treatment;
        private LocalDate appointmentDate;
        private LocalTime appointmentTime;
        private AppointmentStatus status = AppointmentStatus.SCHEDULED;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder appointmentNo(String appointmentNo) {
            this.appointmentNo = appointmentNo;
            return this;
        }

        public Builder patient(Patient patient) {
            this.patient = patient;
            return this;
        }

        public Builder dentist(Dentist dentist) {
            this.dentist = dentist;
            return this;
        }

        public Builder treatment(Treatment treatment) {
            this.treatment = treatment;
            return this;
        }

        public Builder appointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public Builder appointmentTime(LocalTime appointmentTime) {
            this.appointmentTime = appointmentTime;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}
