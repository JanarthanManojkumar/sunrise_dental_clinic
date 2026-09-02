# Sunrise Dental Clinic

Clinic management system for booking appointments, managing dentists and treatments, billing patients, and reporting on clinic activity.

- `sunrise_dental_clinic_backend/` — Java Swing desktop app and a REST API server, sharing the same controller/DAO/model layers. NetBeans Ant project.
- `sunrise_dental_clinic_frontend/` — HTML/CSS/JS web client talking to the REST API.


## Backend setup

Requires JDK 17 and a MySQL server with the `sunrise_dental_clinic` schema.

Create `sunrise_dental_clinic_backend/db.properties` (gitignored) before running:
```
db.url=jdbc:mysql://localhost:3306/sunrise_dental_clinic
db.user=root
db.password=<your local MySQL password>
```
Optional, for billing emails through Brevo:
```
BREVO_API_KEY=...
BREVO_SENDER_EMAIL=...
BREVO_SENDER_NAME=...
```

Run the desktop app from NetBeans, or the REST API with:
```
cd sunrise_dental_clinic_backend
run-api-server.bat
```
The API listens on `http://localhost:8080/api`.

## Frontend setup

Open `sunrise_dental_clinic_frontend/index.html` in a browser while the REST API is running. `js/api.js` points at `http://localhost:8080/api` by default.
