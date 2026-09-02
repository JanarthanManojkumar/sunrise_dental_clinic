-- Adds an active flag to users so staff accounts can be deactivated
-- (src/dao/UserDAO.java, src/api/UserHandler.java) instead of deleted.
-- Not safe to re-run: MySQL 8.0 does not support ADD COLUMN IF NOT EXISTS
-- (errors with 1064), so running this twice will fail with "duplicate column".

USE sunrise_dental_clinic;

ALTER TABLE users
    ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1 AFTER role;
