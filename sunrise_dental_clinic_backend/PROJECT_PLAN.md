# PROJECT_PLAN.md — Sunrise Dental Clinic Appointment & Patient Management System

Module: CIS6003 Advanced Programming | Assessment: WRIT1 (100%) | Word count: 4000

This file is blueprint for whole assignment. Use it to steer NetBeans work + Claude Code sessions until submission.

---

## 1. Brief Summary & Scope

- Assessment brief header table say "Online vehicle reservation System" — mismatch, template leftover. Scenario body clearly Sunrise Dental Clinic. **Follow scenario, not header row.** Document this as noted inconsistency in report intro.
- Client: Sunrise Dental Clinic, Colombo. Problem: manual paper files cause double bookings, lost records, billing errors.
- Deliverable: Java desktop application, menu-driven, error-free, user-friendly, with data structures/DB storage.
- Word count 4000 includes code, tables, figures, citations. Excludes reference list + appendices.
- Report format: A4 | margins 1.5" left, 1" right/top/bottom | line spacing 1.5 | Times New Roman | headings 14pt bold, body 12pt | Harvard referencing throughout.
- Submit as PDF via Turnitin, filename pattern: `st<ID> CIS6003 WRIT1`.

### Task marks breakdown
| Task | Content | Marks | LO |
|---|---|---|---|
| A | UML diagrams (Use Case, Class, Sequence) + design rationale | 20 | LO I |
| B | Interactive system, validation, reports, distributed web service, design patterns, database | 40 | LO II |
| C | Test plan, TDD, test automation | 20 | LO II |
| D | Public Git/GitHub repo, version history, workflow/CI-CD | 20 | LO III |

---

## 2. Functional Requirements

Mandatory (from brief):
1. User Authentication (Login) — username/password, only authorized staff.
2. Register New Appointment — appointment number, patient name, address, contact number, dentist name, treatment type, appointment date/time.
3. Display Appointment Details — search by appointment number, show full record.
4. Calculate and Print Bill — cost from treatment type + consultation fee, print receipt.
5. Help Section — step-by-step instructions for new staff.
6. Exit System — safe close.

Value-add extras (brief explicitly invites "additional functionalities as needed" — boosts Good/Excellent band):
- Update / Cancel appointment.
- Manage Dentists & Treatment price list (admin only).
- List/report: appointments per day, revenue per dentist, upcoming appointments.
- Role-based access: Receptionist (register/search/bill) vs Admin (manage dentists/treatments/users).

---

## 3. Documented Assumptions

State these explicitly in report (brief rewards well-justified assumptions):
- Two roles: `RECEPTIONIST` and `ADMIN`. Admin manages dentist/treatment master data and staff accounts; receptionist runs day-to-day desk operations.
- Appointment number auto-generated (e.g. `APT-YYYYMMDD-###`), not user-entered, to prevent duplicates/double-booking.
- Treatment types held in a price list table (e.g. Consultation, Filling, Extraction, Root Canal, Cleaning) each with a fixed fee; bill = consultation fee + treatment fee.
- One dentist per appointment, dentists stored as reference data (not free text) to avoid inconsistent names.
- Passwords stored hashed (BCrypt/SHA-256+salt) — justified under ETHICAL EDGE requirement (data privacy/secure coding).

---

## 4. Architecture & Technology Stack

Java Swing desktop application, single module, **MVP (Model-View-Presenter)** pattern, direct JDBC access to MySQL. Package layout mirrors existing reference project `D:\ICBT\Test_automation\src`.

```
view  <->  controller (Presenter)  <->  model
                  |
                 dao  ->  db (DBConnection)  ->  MySQL
```

### Package structure
- `model/` — POJOs: `Patient`, `Dentist`, `Treatment`, `Appointment`, `Bill`, `User`.
- `view/` — NetBeans Swing GUI forms (`.java` + `.form` pairs): LoginView, AppointmentView, BillingView, HelpView, etc. Contains no business logic.
- `controller/` — presenter classes mediating View <-> Model/DAO (e.g. `LoginController`, `AppointmentController`, `BillingController`). This is the "Presenter" in MVP terms.
- `dao/` — JDBC DAO classes per entity (`UserDAO`, `PatientDAO`, `AppointmentDAO`, `DentistDAO`, `TreatmentDAO`, `BillDAO`).
- `db/` — `DBConnection` singleton, pooled JDBC connection to MySQL.
- `test_automation/` — JUnit test classes + main test entry point.

### Design patterns (pick 3–4, justify each — rubric rewards "most suitable," not "most")
- **DAO / Repository** — isolates JDBC/SQL from business logic (`AppointmentDAO`, `PatientDAO`, `UserDAO`).
- **Singleton** — single shared `DBConnection` for pooled JDBC connections.
- **Factory** — `ReportFactory` / `BillFactory` builds different report/receipt formats without client code knowing concrete class.
- **MVP** — View (Swing screens) contains no business logic; Controller (Presenter) mediates between View and Model/DAO, keeping UI testable.
- (Optional 4th) **Builder** — constructs `Appointment` objects with many optional fields cleanly.

### Database schema (starting point)
- `users(id, username, password_hash, role)`
- `patients(id, name, address, contact_number)`
- `dentists(id, name, specialization)`
- `treatments(id, name, fee)`
- `appointments(id, appointment_no, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status)`
- `bills(id, appointment_id, consultation_fee, treatment_fee, total, issued_at)`

---

## 5. UML Plan (Task A)

- **Use Case diagram** — Actors: Receptionist, Admin. Use cases: Login, Register Appointment, Search Appointment, Update/Cancel Appointment, Calculate & Print Bill, Manage Dentists (Admin), Manage Treatments (Admin), View Help, Exit. Use `<<include>>` (e.g. Register Appointment includes Validate Input) and `<<extend>>` (e.g. Print Bill extends Search Appointment).
- **Class diagram** — classes: `User`, `Patient`, `Dentist`, `Treatment`, `Appointment`, `Bill`, plus DAO/Service classes. Show visibility (+/-), attributes, methods, multiplicity (1 Patient — * Appointments, 1 Dentist — * Appointments), aggregation/composition where relevant.
- **Sequence diagrams** (pick 3): Login flow, Register Appointment flow (view -> controller -> DAO -> DB), Calculate & Print Bill flow.
- Tooling: draw.io or PlantUML, export as PNG, embed in report with captions + design-decision explanation text under each diagram (not diagrams alone — rubric wants "clear explanations of design decisions").
- Document assumptions inline wherever a diagram makes a design choice not explicit in the brief.

---

## 6. Testing Plan (Task C)

- Framework: **JUnit 5** + **Mockito** (mock DAO layer when testing service logic).
- Approach: **TDD** — for each feature, write failing test first (e.g. `BillCalculatorTest`), implement minimum code to pass, refactor. Narrate this red-green-refactor cycle in the report with 1-2 concrete examples.
- Test data: representative + boundary + invalid cases (e.g. empty patient name, invalid date, duplicate appointment number, negative fee).
- Test plan document structure: Rationale -> Test Plan table (ID, feature, input, expected, actual, pass/fail) -> Test Data -> Traceability matrix (requirement -> test case).
- Automation: Maven Surefire plugin (`mvn test`) runs all JUnit tests; wire into GitHub Actions (see §7) so tests run on every push — this doubles as Task D's CI/CD evidence.
- Capture screenshots of green test runs (NetBeans test runner or terminal) for report appendix.

---

## 7. Git / GitHub Plan (Task D)

- Initialize git repo now inside `D:\ICBT\AdvancedProgramming` (currently no `.git` present).
- `.gitignore`: `target/`, `*.class`, NetBeans `nbproject/private/`, IDE files.
- Branching: `main` (stable) + short-lived feature branches (`feature/login`, `feature/appointments`, `feature/billing`, `feature/tests`) merged via PRs.
- Commit cadence: real incremental commits across multiple days as features are built — not one bulk commit. Rubric's Excellent band explicitly checks "versioning, and version control techniques demonstrated" plus daily-applied modifications.
- GitHub Actions workflow (`.github/workflows/ci.yml`): on push/PR to `main`, run `mvn -B test` — satisfies "Workflow (CI/CD) demonstrated."
- README.md: setup instructions (MySQL schema import, how to run the application), architecture summary, screenshot.
- Make repository **public** before submission deadline; embed repo URL in report documentation section per brief instruction ("share the report link within the documentation").

---

## 8. Report Structure & Mark-Mapping

| Report Section | Maps to | Must include |
|---|---|---|
| Title, Scenario Recap, Assumptions | context | mismatched-title note, role/pricing/appt-number assumptions |
| System Design (UML) | Task A (20) | use case/class/sequence diagrams + design-decision prose + evaluation/critical reflection |
| Architecture & Implementation | Task B (40) | MVP architecture diagram, design pattern justification, DB schema, screenshots of validated UI, sample reports |
| Testing | Task C (20) | rationale, TDD narrative, test plan table, test data, screenshots of passing automated tests, traceability |
| Git/GitHub Workflow | Task D (20) | repo link, screenshots of commit history/branches/PRs, CI workflow screenshot, versioning explanation |
| References | — | Harvard style, excluded from word count |
| Appendices | — | full code listings / extra screenshots, excluded from word count/grading emphasis |

---

## 9. NetBeans + Claude Code Workflow

- **NetBeans**: Swing GUI Builder (drag-drop forms for `view`), Maven project/run/debug integration, quick manual testing of screens.
- **Claude Code**: writes model/DAO/controller classes, business logic, JUnit tests, git commits/branches, GitHub Actions YAML, and drafts report section text — keeps NetBeans free for the visual GUI parts it's actually good at.
- Suggested build order (also see checklist below): DB schema -> `model` classes -> `db`/`dao` layer + unit tests -> `controller` (presenter) layer + tests -> `view` Swing screens wired to controllers -> validation pass -> reports feature -> Git history cleanup/CI -> report writeup -> UML diagrams -> final PDF export.

---

## 10. Milestone Checklist

- [ ] `git init` in `AdvancedProgramming`, add `.gitignore`, first commit.
- [ ] Design & create MySQL schema (`users`, `patients`, `dentists`, `treatments`, `appointments`, `bills`).
- [ ] Set up `model`/`view`/`controller`/`dao`/`db`/`test_automation` package structure.
- [ ] Implement model classes in `model`.
- [ ] Implement `db` (`DBConnection` singleton) + `dao` layer + unit tests (TDD).
- [ ] Implement `controller` (presenter) layer: auth, appointments, bill, dentists, treatments + tests.
- [ ] Implement Swing login screen + auth flow in `view`, wired to `controller`.
- [ ] Implement Register/Search/Update/Cancel Appointment screens with input validation.
- [ ] Implement Calculate & Print Bill screen/report.
- [ ] Implement Help screen and Exit flow.
- [ ] Implement admin screens (manage dentists/treatments) if time allows.
- [ ] Full JUnit test suite green via `mvn test`; capture screenshots.
- [ ] Add GitHub Actions CI workflow running tests on push.
- [ ] Push feature branches with real incremental commits across multiple days; merge via PRs.
- [ ] Make GitHub repo public; finalize README.
- [ ] Draw Use Case, Class, 3x Sequence diagrams with design-decision commentary.
- [ ] Write report body per §8 structure, Harvard references, correct formatting.
- [ ] Proofread against marking rubric per criterion; export report as PDF; verify filename convention.
