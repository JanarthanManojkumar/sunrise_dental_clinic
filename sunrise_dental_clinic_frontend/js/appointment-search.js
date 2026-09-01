const user = requireAuth();

const messageEl = document.getElementById("message");
const btnUpdate = document.getElementById("btnUpdate");
const btnCancel = document.getElementById("btnCancel");
const btnBill = document.getElementById("btnBill");

let currentAppointment = null;

function displayAppointment(appointment) {
    currentAppointment = appointment;
    document.getElementById("valPatient").textContent = appointment.patient.name;
    document.getElementById("valDentist").textContent = appointment.dentist.name;
    document.getElementById("valTreatment").textContent = appointment.treatment.name;
    document.getElementById("valDateTime").textContent =
        appointment.appointmentDate + " " + appointment.appointmentTime;
    document.getElementById("valStatus").textContent = appointment.status;

    const active = appointment.status === "SCHEDULED";
    btnUpdate.disabled = !active;
    btnCancel.disabled = !active;
    btnBill.disabled = false;
}

function clearAppointment() {
    currentAppointment = null;
    ["valPatient", "valDentist", "valTreatment", "valDateTime", "valStatus"].forEach((id) => {
        document.getElementById(id).textContent = "-";
    });
    btnUpdate.disabled = true;
    btnCancel.disabled = true;
    btnBill.disabled = true;
}

document.getElementById("searchForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const appointmentNo = document.getElementById("appointmentNo").value.trim();
    const result = await apiFetch("/appointments/" + encodeURIComponent(appointmentNo));
    if (result.success) {
        displayAppointment(result.data);
        messageEl.textContent = "";
    } else {
        clearAppointment();
        messageEl.textContent = result.message;
    }
});

btnUpdate.addEventListener("click", () => {
    if (!currentAppointment) {
        return;
    }
    window.location.href = "appointment-form.html?mode=update&appointmentNo="
        + encodeURIComponent(currentAppointment.appointmentNo);
});

btnCancel.addEventListener("click", async () => {
    if (!currentAppointment) {
        return;
    }
    if (!confirm("Cancel appointment " + currentAppointment.appointmentNo + "?")) {
        return;
    }
    const result = await apiFetch(
        "/appointments/" + encodeURIComponent(currentAppointment.appointmentNo) + "/cancel",
        { method: "POST" },
    );
    if (result.success) {
        displayAppointment(result.data);
        messageEl.className = "message success";
        messageEl.textContent = "Appointment cancelled.";
    } else {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
    }
});

btnBill.addEventListener("click", async () => {
    if (!currentAppointment) {
        return;
    }
    const result = await apiFetch(
        "/bills/" + encodeURIComponent(currentAppointment.appointmentNo) + "/generate",
        { method: "POST" },
    );
    if (result.success) {
        sessionStorage.setItem("receiptText", result.data);
        sessionStorage.setItem("receiptAppointmentNo", currentAppointment.appointmentNo);
        window.location.href = "receipt.html";
    } else {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
    }
});
