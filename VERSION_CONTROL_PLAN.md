# Version Control Plan

Source migrated from `D:\ICBT\sample` (`sunrise_dental_clinic_ant`, `sunrise_dental_clinic_frontend`), unversioned, into this repo. History is built one feature at a time instead of a single bulk import.

## Security note

`DBConnection.java` originally hardcoded a MySQL root password in plaintext. Fixed to read `DB_URL` / `DB_USER` / `DB_PASSWORD` from environment variables before it was ever committed. The real password that was exposed in the old unversioned copy should still be rotated on the MySQL server.

## Commit checklist

- [x] `.gitignore` — build output and IDE-local files excluded
- [x] `VERSION_CONTROL_PLAN.md` — this file
- [x] Backend scaffold — project files, shared infra, `DBConnection` (env vars)
- [x] Frontend scaffold — shared styles, API client
- [x] Backend: authentication
- [x] Frontend: login page and auth guards
- [x] Backend: main menu and help screens
- [x] Frontend: dashboard and help pages
- [x] Backend: appointment booking and patient management
- [x] Frontend: appointment booking and search pages
- [x] Backend: dentist management
- [x] Frontend: dentist management page
- [x] Backend: treatment management
- [x] Frontend: treatment management page
- [ ] Backend: billing and receipts
- [ ] Frontend: receipt page
- [ ] Backend: reporting
- [ ] Frontend: reports page
- [ ] Backend: REST API server wiring
- [ ] Docs: schema migration and project planning docs
- [ ] Docs: README overview and setup instructions

## Setup after clone

Set before running the backend:
```
DB_URL=jdbc:mysql://localhost:3306/sunrise_dental_clinic
DB_USER=root
DB_PASSWORD=<your local MySQL password>
```
Optional, for billing emails: `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`, `BREVO_SENDER_NAME`.
