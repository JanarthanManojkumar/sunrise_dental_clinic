const user = requireAuth();

const params = new URLSearchParams(window.location.search);
const mode = params.get("mode") === "update" ? "update" : "register";
const appointmentNo = params.get("appointmentNo");

const messageEl = document.getElementById("message");
const dentistSelect = document.getElementById("dentist");
const treatmentSelect = document.getElementById("treatment");
const existingPatientSelect = document.getElementById("existingPatient");

if (mode === "update") {
    document.getElementById("pageTitle").textContent = "Sunrise Dental Clinic - Update Appointment";
    document.getElementById("banner").textContent = "Update Appointment";
    document.getElementById("btnSubmit").textContent = "Save Changes";
    ["patientName", "address", "contactNumber", "email"].forEach((id) => {
        document.getElementById(id).disabled = true;
    });
    document.getElementById("existingPatientLabel").hidden = true;
    existingPatientSelect.hidden = true;
}

let existingPatients = [];

async function loadExistingPatients() {
    if (mode !== "register") {
        return;
    }
    const result = await apiFetch("/patients?q=");
    if (!result.success) {
        return;
    }
    existingPatients = result.data;
    for (const patient of existingPatients) {
        const option = document.createElement("option");
        option.value = patient.id;
        option.textContent = patient.name + " - " + patient.contactNumber;
        existingPatientSelect.appendChild(option);
    }
}

existingPatientSelect.addEventListener("change", () => {
    const patient = existingPatients.find((p) => String(p.id) === existingPatientSelect.value);
    document.getElementById("patientName").value = patient ? patient.name : "";
    document.getElementById("address").value = patient ? (patient.address || "") : "";
    document.getElementById("contactNumber").value = patient ? patient.contactNumber : "";
    document.getElementById("email").value = patient ? (patient.email || "") : "";
    ["patientName", "address", "contactNumber", "email"].forEach((id) => {
        document.getElementById(id).disabled = !!patient;
    });
});

async function loadDentists() {
    const result = await apiFetch("/dentists");
    if (result.success) {
        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "-- Select Dentist --";
        dentistSelect.appendChild(placeholder);
        for (const dentist of result.data) {
            const option = document.createElement("option");
            option.value = dentist.id;
            option.textContent = dentist.name;
            dentistSelect.appendChild(option);
        }
    }
}

async function loadTreatments() {
    const result = await apiFetch("/treatments");
    if (result.success) {
        for (const treatment of result.data) {
            const option = document.createElement("option");
            option.value = treatment.id;
            option.textContent = treatment.name;
            treatmentSelect.appendChild(option);
        }
    }
}

async function loadExistingAppointment() {
    if (mode !== "update" || !appointmentNo) {
        return;
    }
    const result = await apiFetch("/appointments/" + encodeURIComponent(appointmentNo));
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    const appointment = result.data;
    document.getElementById("patientName").value = appointment.patient.name;
    document.getElementById("address").value = appointment.patient.address || "";
    document.getElementById("contactNumber").value = appointment.patient.contactNumber;
    document.getElementById("email").value = appointment.patient.email || "";
    dentistSelect.value = appointment.dentist.id;
    treatmentSelect.value = appointment.treatment.id;
    document.getElementById("date").value = appointment.appointmentDate;
    document.getElementById("time").value = appointment.appointmentTime;
}

(async () => {
    await Promise.all([loadDentists(), loadTreatments(), loadExistingPatients()]);
    await loadExistingAppointment();
})();

document.getElementById("appointmentForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    messageEl.textContent = "";

    const dateValue = document.getElementById("date").value.trim();
    const today = new Date().toISOString().slice(0, 10);
    if (dateValue < today) {
        messageEl.textContent = "Appointment date cannot be in the past";
        return;
    }

    const body = {
        patientName: document.getElementById("patientName").value,
        address: document.getElementById("address").value,
        contactNumber: document.getElementById("contactNumber").value,
        email: document.getElementById("email").value,
        dentistId: Number(dentistSelect.value),
        treatmentId: Number(treatmentSelect.value),
        date: dateValue,
        time: document.getElementById("time").value.trim(),
        existingPatientId: existingPatientSelect.value ? Number(existingPatientSelect.value) : null,
    };

    const result = mode === "register"
        ? await apiFetch("/appointments", { method: "POST", body: JSON.stringify(body) })
        : await apiFetch("/appointments/" + encodeURIComponent(appointmentNo), {
            method: "PUT", body: JSON.stringify(body),
        });

    if (result.success) {
        const verb = mode === "register" ? "Appointment registered: " : "Appointment updated: ";
        alert(verb + result.data.appointmentNo);
        window.location.href = "dashboard.html";
    } else {
        messageEl.textContent = result.message;
    }
});
