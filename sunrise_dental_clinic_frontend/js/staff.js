const user = requireAdmin();

const tableBody = document.getElementById("tableBody");
const messageEl = document.getElementById("message");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const roleSelect = document.getElementById("role");

async function refreshTable() {
    const result = await apiFetch("/users");
    tableBody.innerHTML = "";
    if (!result.success) {
        messageEl.textContent = result.message;
        return;
    }
    for (const staff of result.data) {
        const row = document.createElement("tr");
        row.innerHTML = "<td></td><td></td><td></td><td></td><td></td>";
        row.children[0].textContent = staff.id;
        row.children[1].textContent = staff.username;
        row.children[2].textContent = staff.role;
        row.children[3].textContent = staff.active ? "Active" : "Deactivated";

        if (staff.id !== user.id) {
            const btnToggle = document.createElement("button");
            btnToggle.textContent = staff.active ? "Deactivate" : "Reactivate";
            btnToggle.addEventListener("click", async () => {
                const toggleResult = await apiFetch("/users/" + staff.id, {
                    method: "PUT",
                    body: JSON.stringify({ active: !staff.active }),
                });
                if (toggleResult.success) {
                    refreshTable();
                } else {
                    messageEl.className = "message error";
                    messageEl.textContent = toggleResult.message;
                }
            });
            row.children[4].appendChild(btnToggle);
        }

        tableBody.appendChild(row);
    }
}

document.getElementById("staffForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const result = await apiFetch("/users", {
        method: "POST",
        body: JSON.stringify({
            username: usernameInput.value,
            password: passwordInput.value,
            role: roleSelect.value,
        }),
    });
    if (result.success) {
        messageEl.className = "message success";
        messageEl.textContent = "Account created.";
        usernameInput.value = "";
        passwordInput.value = "";
        refreshTable();
    } else {
        messageEl.className = "message error";
        messageEl.textContent = result.message;
    }
});

refreshTable();
