const user = requireAdmin();

const tableHead = document.getElementById("tableHead");
const tableBody = document.getElementById("tableBody");
const messageEl = document.getElementById("message");

function setColumns(labels) {
    tableHead.innerHTML = "<tr>" + labels.map((l) => "<th>" + l + "</th>").join("") + "</tr>";
}

function setRows(rows) {
    tableBody.innerHTML = "";
    for (const cells of rows) {
        const row = document.createElement("tr");
        row.innerHTML = cells.map(() => "<td></td>").join("");
        cells.forEach((value, i) => { row.children[i].textContent = value; });
        tableBody.appendChild(row);
    }
}

async function generate() {
    messageEl.textContent = "";
    const type = document.getElementById("reportType").value;

    if (type === "appointments-per-day") {
        const result = await apiFetch("/reports/appointments-per-day");
        if (!result.success) { messageEl.textContent = result.message; return; }
        setColumns(["Date", "Appointments"]);
        setRows(result.data.map((r) => [r.date, r.count]));
    } else if (type === "revenue-per-dentist") {
        const result = await apiFetch("/reports/revenue-per-dentist");
        if (!result.success) { messageEl.textContent = result.message; return; }
        setColumns(["Dentist", "Revenue (Rs.)"]);
        setRows(result.data.map((r) => [r.dentistName, r.revenue]));
    } else {
        const result = await apiFetch("/reports/upcoming");
        if (!result.success) { messageEl.textContent = result.message; return; }
        setColumns(["Appointment No", "Patient", "Dentist", "Date", "Time"]);
        setRows(result.data.map((a) => [
            a.appointmentNo, a.patient.name, a.dentist.name, a.appointmentDate, a.appointmentTime,
        ]));
    }
}

document.getElementById("btnGenerate").addEventListener("click", generate);
