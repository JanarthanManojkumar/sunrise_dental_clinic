const user = requireAdmin();

const tableBody = document.getElementById("tableBody");
const messageEl = document.getElementById("message");
const nameInput = document.getElementById("name");
const specializationInput = document.getElementById("specialization");

let selectedDentistId = null;

async function refreshTable() {
    const result = await apiFetch("/dentists");
    tableBody.innerHTML = "";
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    for (const dentist of result.data) {
        const row = document.createElement("tr");
        row.innerHTML = "<td>" + dentist.id + "</td><td></td><td></td>";
        row.children[1].textContent = dentist.name;
        row.children[2].textContent = dentist.specialization || "";
        row.addEventListener("click", () => {
            selectedDentistId = dentist.id;
            nameInput.value = dentist.name;
            specializationInput.value = dentist.specialization || "";
        });
        tableBody.appendChild(row);
    }
}

function handleResult(result) {
    if (result.success) {
        messageEl.className = "message success";
        messageEl.textContent = "Saved.";
        nameInput.value = "";
        specializationInput.value = "";
        selectedDentistId = null;
        refreshTable();
    } else {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
    }
}

document.getElementById("dentistForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const result = await apiFetch("/dentists", {
        method: "POST",
        body: JSON.stringify({ name: nameInput.value, specialization: specializationInput.value }),
    });
    handleResult(result);
});

document.getElementById("btnUpdate").addEventListener("click", async () => {
    if (selectedDentistId === null) {
        messageEl.className = "message error";
        messageEl.textContent = "Select a dentist row first.";
        return;
    }
    const result = await apiFetch("/dentists/" + selectedDentistId, {
        method: "PUT",
        body: JSON.stringify({ name: nameInput.value, specialization: specializationInput.value }),
    });
    handleResult(result);
});

refreshTable();
