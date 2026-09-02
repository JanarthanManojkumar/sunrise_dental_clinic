const user = requireAuth();

if (user) {
    document.getElementById("welcome").textContent =
        "Welcome, " + user.username + "  (" + user.role + ")";

    if (user.role === "ADMIN") {
        document.getElementById("btnDentists").hidden = false;
        document.getElementById("btnTreatments").hidden = false;
        document.getElementById("btnReports").hidden = false;
        document.getElementById("btnStaff").hidden = false;
    }
}
