-- Adds the missing email column to patients, needed by PatientDAO/AppointmentDAO
-- (src/dao/PatientDAO.java, src/dao/AppointmentDAO.java) which already read/write it.
-- Safe to re-run: only adds the column if it isn't already there.

USE sunrise_dental_clinic;

ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL AFTER contact_number;
