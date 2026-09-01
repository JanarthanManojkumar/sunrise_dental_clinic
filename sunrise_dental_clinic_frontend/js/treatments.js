const user = requireAdmin();

const tableBody = document.getElementById("tableBody");
const messageEl = document.getElementById("message");
const nameInput = document.getElementById("name");
const feeInput = document.getElementById("fee");

let selectedTreatmentId = null;

async function refreshTable() {
    const result = await apiFetch("/treatments");
    tableBody.innerHTML = "";
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    for (const treatment of result.data) {
        const row = document.createElement("tr");
        row.innerHTML = "<td>" + treatment.id + "</td><td></td><td></td>";
        row.children[1].textContent = treatment.name;
        row.children[2].textContent = treatment.fee;
        row.addEventListener("click", () => {
            selectedTreatmentId = treatment.id;
            nameInput.value = treatment.name;
            feeInput.value = treatment.fee;
        });
        tableBody.appendChild(row);
    }
}

function handleResult(result) {
    if (result.success) {
        messageEl.className = "message success";
        messageEl.textContent = "Saved.";
        nameInput.value = "";
        feeInput.value = "";
        selectedTreatmentId = null;
        refreshTable();
    } else {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
    }
}

document.getElementById("treatmentForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const result = await apiFetch("/treatments", {
        method: "POST",
        body: JSON.stringify({ name: nameInput.value, fee: feeInput.value }),
    });
    handleResult(result);
});

document.getElementById("btnUpdate").addEventListener("click", async () => {
    if (selectedTreatmentId === null) {
        messageEl.className = "message error";
        messageEl.textContent = "Select a treatment row first.";
        return;
    }
    const result = await apiFetch("/treatments/" + selectedTreatmentId, {
        method: "PUT",
        body: JSON.stringify({ name: nameInput.value, fee: feeInput.value }),
    });
    handleResult(result);
});

refreshTable();
