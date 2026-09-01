const user = requireAuth();

const appointmentNo = sessionStorage.getItem("receiptAppointmentNo");
const receiptText = sessionStorage.getItem("receiptText");

if (!receiptText || !appointmentNo) {
    window.location.href = "appointment-search.html";
} else {
    document.getElementById("receiptText").textContent = receiptText;
}

document.getElementById("btnEmail").addEventListener("click", async () => {
    const result = await apiFetch("/bills/" + encodeURIComponent(appointmentNo) + "/email", { method: "POST" });
    alert(result.success ? result.data : result.message);
});
