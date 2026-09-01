const API_BASE = "http://localhost:8080/api";

function getToken() {
    return localStorage.getItem("token");
}

function getUser() {
    try {
        return JSON.parse(localStorage.getItem("user"));
    } catch (e) {
        return null;
    }
}

function clearSession() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

/**
 * Calls the REST API and always resolves with {status, success, message, data}.
 * A 401 means the token is missing/expired (mirrors SessionManager's 15-min
 * timeout on the desktop app) so it clears the session and bounces to login.
 */
async function apiFetch(path, options = {}) {
    const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
    const token = getToken();
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    let response;
    try {
        response = await fetch(API_BASE + path, { ...options, headers });
    } catch (e) {
        return { status: 0, success: false, message: "Cannot reach the API server. Is it running?" };
    }

    let body;
    try {
        body = await response.json();
    } catch (e) {
        body = { success: false, message: "Invalid response from server" };
    }

    if (response.status === 401) {
        clearSession();
        window.location.href = "index.html?expired=1";
    }

    return { status: response.status, success: body.success, message: body.message, data: body.data };
}
