/** Redirects to login if there's no session; returns the logged-in user otherwise. */
function requireAuth() {
    const token = getToken();
    const user = getUser();
    if (!token || !user) {
        window.location.href = "index.html";
        return null;
    }
    return user;
}

/** Same as requireAuth but also bounces non-admins back to the dashboard. */
function requireAdmin() {
    const user = requireAuth();
    if (user && user.role !== "ADMIN") {
        window.location.href = "dashboard.html";
        return null;
    }
    return user;
}

function logout() {
    if (!confirm("Log out and return to the login screen?")) {
        return;
    }
    apiFetch("/logout", { method: "POST" }).finally(() => {
        clearSession();
        window.location.href = "index.html";
    });
}
