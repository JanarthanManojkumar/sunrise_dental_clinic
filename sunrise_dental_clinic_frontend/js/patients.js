const user = requireAuth();

const tableBody = document.getElementById("tableBody");
const historyBody = document.getElementById("historyBody");
const messageEl = document.getElementById("message");
const detailPanel = document.getElementById("detailPanel");
const detailPatientEl = document.getElementById("detailPatient");

async function search() {
    const query = document.getElementById("query").value.trim();
    const result = await apiFetch("/patients?q=" + encodeURIComponent(query));
    tableBody.innerHTML = "";
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    messageEl.textContent = "";
    for (const patient of result.data) {
        const row = document.createElement("tr");
        row.innerHTML = "<td></td><td></td><td></td><td></td>";
        row.children[0].textContent = patient.id;
        row.children[1].textContent = patient.name;
        row.children[2].textContent = patient.contactNumber;
        row.children[3].textContent = patient.address || "";
        row.addEventListener("click", () => loadDetail(patient.id));
        tableBody.appendChild(row);
    }
}

async function loadDetail(id) {
    const result = await apiFetch("/patients/" + id);
    if (!result.success) {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
        return;
    }
    const patient = result.data;
    detailPanel.hidden = false;
    detailPatientEl.textContent = patient.name + " - " + patient.contactNumber
        + (patient.address ? " - " + patient.address : "");

    historyBody.innerHTML = "";
    for (const appointment of patient.appointments) {
        const row = document.createElement("tr");
        row.innerHTML = "<td></td><td></td><td></td><td></td><td></td><td></td>";
        row.children[0].textContent = appointment.appointmentNo;
        row.children[1].textContent = appointment.dentist.name;
        row.children[2].textContent = appointment.treatment.name;
        row.children[3].textContent = appointment.appointmentDate;
        row.children[4].textContent = appointment.appointmentTime;
        row.children[5].textContent = appointment.status;
        historyBody.appendChild(row);
    }
}

document.getElementById("searchForm").addEventListener("submit", (e) => {
    e.preventDefault();
    search();
});

search();
