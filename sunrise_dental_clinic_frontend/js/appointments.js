const user = requireAuth();

const tableBody = document.getElementById("tableBody");
const messageEl = document.getElementById("message");
const dentistSelect = document.getElementById("filterDentist");

async function loadDentists() {
    const result = await apiFetch("/dentists");
    if (result.success) {
        for (const dentist of result.data) {
            const option = document.createElement("option");
            option.value = dentist.id;
            option.textContent = dentist.name;
            dentistSelect.appendChild(option);
        }
    }
}

function buildQuery() {
    const params = new URLSearchParams();
    const date = document.getElementById("filterDate").value.trim();
    const dateFrom = document.getElementById("filterDateFrom").value.trim();
    const dateTo = document.getElementById("filterDateTo").value.trim();
    const dentistId = dentistSelect.value;
    const patient = document.getElementById("filterPatient").value.trim();
    if (date) params.set("date", date);
    if (dateFrom) params.set("dateFrom", dateFrom);
    if (dateTo) params.set("dateTo", dateTo);
    if (dentistId) params.set("dentistId", dentistId);
    if (patient) params.set("patient", patient);
    return params.toString();
}

async function refreshTable() {
    const query = buildQuery();
    const result = await apiFetch("/appointments" + (query ? "?" + query : ""));
    tableBody.innerHTML = "";
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    messageEl.textContent = "";
    for (const appointment of result.data) {
        const row = document.createElement("tr");
        row.innerHTML = "<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>";
        row.children[0].textContent = appointment.appointmentNo;
        row.children[1].textContent = appointment.patient.name;
        row.children[2].textContent = appointment.dentist.name;
        row.children[3].textContent = appointment.treatment.name;
        row.children[4].textContent = appointment.appointmentDate;
        row.children[5].textContent = appointment.appointmentTime;
        row.children[6].textContent = appointment.status;

        const actions = row.children[7];
        const active = appointment.status === "SCHEDULED";

        const btnUpdate = document.createElement("button");
        btnUpdate.textContent = "Update";
        btnUpdate.disabled = !active;
        btnUpdate.addEventListener("click", () => {
            window.location.href = "appointment-form.html?mode=update&appointmentNo="
                + encodeURIComponent(appointment.appointmentNo);
        });

        const btnCancel = document.createElement("button");
        btnCancel.textContent = "Cancel";
        btnCancel.disabled = !active;
        btnCancel.addEventListener("click", async () => {
            if (!confirm("Cancel appointment " + appointment.appointmentNo + "?")) {
                return;
            }
            const cancelResult = await apiFetch(
                "/appointments/" + encodeURIComponent(appointment.appointmentNo) + "/cancel",
                { method: "POST" },
            );
            if (cancelResult.success) {
                refreshTable();
            } else {
                messageEl.className = "message error";
                messageEl.textContent = cancelResult.message;
            }
        });

        const btnBill = document.createElement("button");
        btnBill.textContent = "Bill";
        btnBill.disabled = appointment.status === "CANCELLED";
        btnBill.addEventListener("click", async () => {
            const billResult = await apiFetch(
                "/bills/" + encodeURIComponent(appointment.appointmentNo) + "/generate",
                { method: "POST" },
            );
            if (billResult.success) {
                sessionStorage.setItem("receiptText", billResult.data);
                sessionStorage.setItem("receiptAppointmentNo", appointment.appointmentNo);
                window.location.href = "receipt.html";
            } else {
                messageEl.className = "message error";
                messageEl.textContent = billResult.message;
            }
        });

        actions.appendChild(btnUpdate);
        actions.appendChild(btnCancel);
        actions.appendChild(btnBill);
        tableBody.appendChild(row);
    }
}

document.getElementById("filterForm").addEventListener("submit", (e) => {
    e.preventDefault();
    refreshTable();
});

document.getElementById("btnClear").addEventListener("click", () => {
    document.getElementById("filterForm").reset();
    dentistSelect.value = "";
    refreshTable();
});

(async () => {
    await loadDentists();
    await refreshTable();
})();
