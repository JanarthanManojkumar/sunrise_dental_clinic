const messageEl = document.getElementById("message");

const params = new URLSearchParams(window.location.search);
if (params.get("expired") === "1") {
    messageEl.textContent = "Your session has expired due to inactivity. Please log in again.";
    messageEl.className = "message error";
}

document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const result = await apiFetch("/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
    });

    if (result.success) {
        localStorage.setItem("token", result.data.token);
        localStorage.setItem("user", JSON.stringify({
            id: result.data.id,
            username: result.data.username,
            role: result.data.role,
        }));
        messageEl.textContent = "Login successful!";
        messageEl.className = "message success";
        window.location.href = "dashboard.html";
    } else {
        messageEl.textContent = result.message;
        messageEl.className = "message error";
    }
});
