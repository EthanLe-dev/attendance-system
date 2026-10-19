// DOM Elements
const dashboardContainer = document.getElementById("dashboardContainer");
const backToStudentBtn = document.getElementById("backToStudentBtn");
const refreshBtn = document.getElementById("refreshBtn");
const copyReportBtn = document.getElementById("copyReportBtn");
const toggleSessionBtn = document.getElementById("toggleSessionBtn");

const sessionStatus = document.getElementById("sessionStatus");
const sessionPulse = document.getElementById("sessionPulse");
const progressBar = document.getElementById("progressBar");
const progressPercent = document.getElementById("progressPercent");

const totalStudents = document.getElementById("totalStudents");
const attendedStudents = document.getElementById("attendedStudents");
const unattendedCount = document.getElementById("unattendedCount");

// Search
const searchInput = document.getElementById("searchInput");
const clearSearchBtn = document.getElementById("clearSearchBtn");

// Tabs
const tabAttendedBtn = document.getElementById("tabAttendedBtn");
const tabUnattendedBtn = document.getElementById("tabUnattendedBtn");
const tabAttendedContent = document.getElementById("tabAttendedContent");
const tabUnattendedContent = document.getElementById("tabUnattendedContent");
const tabAttendedCount = document.getElementById("tabAttendedCount");
const tabUnattendedBadge = document.getElementById("tabUnattendedBadge");

const attendanceList = document.getElementById("attendanceList");
const unattendedList = document.getElementById("unattendedList");

// Toast
const toastNotification = document.getElementById("toastNotification");

// Modals
const pinModal = document.getElementById("pinModal");
const pinInput = document.getElementById("pinInput");
const pinErrorMessage = document.getElementById("pinErrorMessage");
const submitPinBtn = document.getElementById("submitPinBtn");

const manualModal = document.getElementById("manualModal");
const manualModalDesc = document.getElementById("manualModalDesc");
const cancelManualBtn = document.getElementById("cancelManualBtn");
const confirmManualBtn = document.getElementById("confirmManualBtn");

const imagePreviewModal = document.getElementById("imagePreviewModal");
const enlargedSelfie = document.getElementById("enlargedSelfie");
const imageCaption = document.getElementById("imageCaption");

// State
let selectedStudentForManual = null;
let currentActiveTab = "attended"; // "attended" or "unattended"
let currentDashboardData = null;
let toastTimeout = null;

// Event Listeners
document.addEventListener("DOMContentLoaded", initDashboard);
refreshBtn.addEventListener("click", loadDashboard);
copyReportBtn.addEventListener("click", handleCopyZaloReport);
toggleSessionBtn.addEventListener("click", toggleSession);

tabAttendedBtn.addEventListener("click", () => switchTab("attended"));
tabUnattendedBtn.addEventListener("click", () => switchTab("unattended"));

searchInput.addEventListener("input", handleSearch);
clearSearchBtn.addEventListener("click", clearSearch);

cancelManualBtn.addEventListener("click", closeManualModal);
confirmManualBtn.addEventListener("click", handleConfirmManualCheckin);

if (backToStudentBtn) {
    backToStudentBtn.addEventListener("click", async (e) => {
        e.preventDefault();
        try {
            await fetch("/api/dashboard/logout", { method: "POST" });
        } catch (err) {
            console.error("Logout error:", err);
        }
        window.location.replace("/index.html");
    });
}

window.addEventListener("pageshow", function (event) {
    if (event.persisted) {
        if (dashboardContainer) {
            dashboardContainer.classList.add("hidden");
        }
        loadDashboard();
    }
});

/* ==============================================
   AUTHENTICATION (PIN)
   ============================================== */

function initDashboard() {
    loadDashboard();
}

function openPinModal() {
    pinModal.classList.remove("hidden");
    pinInput.value = "";
    pinErrorMessage.classList.add("hidden");
    setTimeout(() => pinInput.focus(), 200);
}

function closePinModal() {
    pinModal.classList.add("hidden");
}

function lockDashboard() {
    if (dashboardContainer) {
        dashboardContainer.classList.add("hidden");
    }
    openPinModal();
}

async function handlePinSubmit() {
    const pin = pinInput.value.trim();
    if (!/^\d{10}$/.test(pin)) {
        pinErrorMessage.textContent = "PIN phải gồm đúng 10 chữ số.";
        pinErrorMessage.classList.remove("hidden");
        return;
    }

    submitPinBtn.disabled = true;
    submitPinBtn.textContent = "Đang kiểm tra...";
    pinErrorMessage.classList.add("hidden");

    try {
        const response = await fetch("/api/dashboard/verify-pin", {
            method: "POST",
            headers: {
                "X-Dashboard-Pin": pin
            }
        });

        if (!response.ok) {
            let errorMsg = "Mã PIN không chính xác.";
            try {
                const errJson = await response.json();
                if (errJson.message) errorMsg = errJson.message;
            } catch (e) {}
            throw new Error(errorMsg);
        }

        closePinModal();
        loadDashboard();

    } catch (error) {
        pinErrorMessage.textContent = error.message || "Mã PIN không chính xác.";
        pinErrorMessage.classList.remove("hidden");
        pinInput.focus();
        pinInput.select();
    } finally {
        submitPinBtn.disabled = false;
        submitPinBtn.textContent = "Mở khóa Dashboard";
    }
}

// Dashboard dùng cookie HttpOnly do server tạo sau khi xác thực PIN.
async function authFetch(url, options = {}) {
    const response = await fetch(url, options);

    if (response.status === 401) {
        if (dashboardContainer) {
            dashboardContainer.classList.add("hidden");
        }
        openPinModal();
        throw new Error("Phiên làm việc hết hạn hoặc PIN không đúng. Vui lòng nhập lại PIN.");
    }

    return response;
}

/* ==============================================
   TABS MANAGEMENT
   ============================================== */

function switchTab(tabName) {
    currentActiveTab = tabName;
    if (tabName === "attended") {
        tabAttendedBtn.classList.add("active");
        tabUnattendedBtn.classList.remove("active");
        tabAttendedContent.classList.remove("hidden");
        tabUnattendedContent.classList.add("hidden");
    } else {
        tabUnattendedBtn.classList.add("active");
        tabAttendedBtn.classList.remove("active");
        tabUnattendedContent.classList.remove("hidden");
        tabAttendedContent.classList.add("hidden");
    }
}

/* ==============================================
   SEARCH & FILTER
   ============================================== */

function handleSearch() {
    const keyword = searchInput.value.trim().toLowerCase();

    if (keyword) {
        clearSearchBtn.classList.remove("hidden");
    } else {
        clearSearchBtn.classList.add("hidden");
    }

    if (!currentDashboardData) return;

    const filteredAttended = (currentDashboardData.attendances || []).filter(student =>
        student.name.toLowerCase().includes(keyword) ||
        student.mssv.toLowerCase().includes(keyword)
    );

    const filteredUnattended = (currentDashboardData.unattendedStudents || []).filter(student =>
        student.name.toLowerCase().includes(keyword) ||
        student.mssv.toLowerCase().includes(keyword)
    );

    renderAttendanceList(filteredAttended);
    renderUnattendedList(filteredUnattended);
}

function clearSearch() {
    searchInput.value = "";
    clearSearchBtn.classList.add("hidden");
    if (currentDashboardData) {
        renderAttendanceList(currentDashboardData.attendances || []);
        renderUnattendedList(currentDashboardData.unattendedStudents || []);
    }
}

/* ==============================================
   COPY ZALO REPORT
   ============================================== */

async function handleCopyZaloReport() {
    if (!currentDashboardData) return;

    const total = Number(currentDashboardData.totalStudents) || 0;
    const attended = Number(currentDashboardData.attendedStudents) || 0;
    const unattended = Math.max(0, total - attended);
    const percent = currentDashboardData.attendancePercent != null
        ? Number(currentDashboardData.attendancePercent)
        : (total > 0 ? Math.round((attended / total) * 100) : 0);

    const now = new Date();
    const timeString = now.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }) +
                       " ngày " + now.toLocaleDateString("vi-VN");

    const unattendedListNames = (currentDashboardData.unattendedStudents || [])
        .map((s, idx) => `  ${idx + 1}. ${s.name} (MSSV: ${s.mssv}${s.groupNumber != null ? ` - STT Nhóm: ${s.groupNumber}` : ""})`)
        .join("\n");

    let reportText = `[BÁO CÁO ĐIỂM DANH NGOẠI KHÓA - BẢO TÀNG HỒ CHÍ MINH]\n`;
    reportText += `Thời gian: ${timeString}\n`;
    reportText += `Sĩ số lớp: ${total} sinh viên\n`;
    reportText += `Đã có mặt: ${attended}/${total} (${percent}%)\n`;
    reportText += `Còn vắng: ${unattended} sinh viên\n`;

    if (unattended > 0) {
        reportText += `-------------------------\n`;
        reportText += `Danh sách sinh viên chưa có mặt:\n${unattendedListNames}\n`;
    } else {
        reportText += `Toàn bộ lớp đã có mặt đông đủ.\n`;
    }

    try {
        await navigator.clipboard.writeText(reportText);
        showToast("Đã sao chép báo cáo Zalo thành công!");
    } catch (e) {
        showToast("Không thể sao chép tự động. Hãy thử lại!");
    }
}

function showToast(message) {
    if (toastTimeout) clearTimeout(toastTimeout);
    toastNotification.textContent = message;
    toastNotification.classList.remove("hidden");
    toastTimeout = setTimeout(() => {
        toastNotification.classList.add("hidden");
    }, 2800);
}

/* ==============================================
   DASHBOARD DATA & RENDERING
   ============================================== */

async function loadDashboard() {
    setLoadingState();

    try {
        const response = await authFetch("/api/dashboard");

        if (!response.ok) {
            let errorMsg = "Không thể tải dữ liệu Dashboard.";
            try {
                const errJson = await response.json();
                if (errJson.message) errorMsg = errJson.message;
            } catch (e) {}
            throw new Error(errorMsg);
        }

        const dashboard = await response.json();
        closePinModal();
        currentDashboardData = dashboard;
        if (dashboardContainer) {
            dashboardContainer.classList.remove("hidden");
        }
        renderDashboard(dashboard);

    } catch (error) {
        console.error("Dashboard error:", error);
        if (dashboardContainer) {
            dashboardContainer.classList.add("hidden");
        }
        showError(error.message || "Không thể kết nối tới máy chủ.");
    }
}

function renderDashboard(dashboard) {
    const total = Number(dashboard.totalStudents) || 0;
    const attended = Number(dashboard.attendedStudents) || 0;
    const unattended = Math.max(0, total - attended);
    const percent = dashboard.attendancePercent != null
        ? Number(dashboard.attendancePercent)
        : (total > 0 ? Math.round((attended / total) * 100) : 0);

    totalStudents.textContent = total;
    attendedStudents.textContent = attended;
    unattendedCount.textContent = unattended;

    // Progress bar & percent
    if (progressPercent) {
        progressPercent.textContent = `${percent}% (${attended}/${total})`;
    }
    if (progressBar) {
        progressBar.style.width = `${percent}%`;
    }

    tabAttendedCount.textContent = attended;
    tabUnattendedBadge.textContent = unattended;

    updateSessionStatus(dashboard.sessionStatus);

    // Kiểm tra xem có đang tìm kiếm không
    const searchVal = searchInput.value.trim().toLowerCase();
    if (searchVal) {
        handleSearch();
    } else {
        renderAttendanceList(dashboard.attendances || []);
        renderUnattendedList(dashboard.unattendedStudents || []);
    }
}

/* Render danh sách ĐÃ ĐIỂM DANH */
function renderAttendanceList(attendances) {
    attendanceList.replaceChildren();

    if (!attendances || attendances.length === 0) {
        renderListMessage(attendanceList, "empty-message", "Chưa có sinh viên nào điểm danh.");
        return;
    }

    attendances.forEach((attendance, index) => {
        const card = createAttendanceCard(attendance, index + 1);
        attendanceList.appendChild(card);
    });
}

function createAttendanceCard(attendance, defaultIndex) {
    const card = document.createElement("div");
    card.className = "attendance-card";

    const info = document.createElement("div");
    info.className = "attendance-card-info";

    const orderTag = document.createElement("span");
    orderTag.className = "student-order-tag";
    orderTag.textContent = attendance.groupNumber != null ? `STT Nhóm: ${attendance.groupNumber}` : "Chưa có nhóm";

    const studentName = document.createElement("p");
    studentName.className = "student-name";
    studentName.textContent = attendance.name;

    const studentMssv = document.createElement("p");
    studentMssv.className = "student-mssv";
    studentMssv.textContent = `MSSV: ${attendance.mssv}`;

    const checkInTime = document.createElement("p");
    checkInTime.className = "check-in-time";
    checkInTime.textContent = `Thời gian: ${formatDateTime(attendance.checkedInAt)}`;

    info.appendChild(orderTag);
    info.appendChild(studentName);
    info.appendChild(studentMssv);
    info.appendChild(checkInTime);

    // Cảnh báo & Trạng thái
    if (attendance.isManual) {
        const badge = document.createElement("div");
        badge.className = "badge badge-manual";
        badge.textContent = "Điểm danh thủ công";
        info.appendChild(badge);
    } else if (attendance.gpsBypassed) {
        const badge = document.createElement("div");
        badge.className = "badge badge-warning";
        badge.textContent = "Không có GPS (Bỏ qua vị trí)";
        info.appendChild(badge);
    }

    card.appendChild(info);

    // Ảnh Selfie hoặc Avatar thủ công
    if (attendance.selfieUrl) {
        const selfie = document.createElement("img");
        selfie.src = attendance.selfieUrl;
        selfie.alt = `Ảnh selfie của ${attendance.name}`;
        selfie.loading = "lazy";
        selfie.title = "Chạm để phóng to ảnh";
        selfie.addEventListener("click", () => {
            openImagePreview(attendance.selfieUrl, `${attendance.name} (${attendance.mssv})`);
        });
        card.appendChild(selfie);
    } else {
        const manualAvatar = document.createElement("div");
        manualAvatar.className = "manual-avatar";
        const label = document.createElement("span");
        label.textContent = "Thủ công";
        manualAvatar.appendChild(label);
        card.appendChild(manualAvatar);
    }

    return card;
}

/* Render danh sách CHƯA ĐIỂM DANH */
function renderUnattendedList(unattendedStudents) {
    unattendedList.replaceChildren();

    if (!unattendedStudents || unattendedStudents.length === 0) {
        renderListMessage(unattendedList, "empty-message", "Tất cả sinh viên đã điểm danh đầy đủ.");
        return;
    }

    unattendedStudents.forEach((student, index) => {
        const card = createUnattendedCard(student, index + 1);
        unattendedList.appendChild(card);
    });
}

function createUnattendedCard(student, defaultIndex) {
    const card = document.createElement("div");
    card.className = "unattended-card";

    const info = document.createElement("div");
    info.className = "unattended-info";

    const orderTag = document.createElement("span");
    orderTag.className = "student-order-tag";
    orderTag.textContent = student.groupNumber != null ? `STT Nhóm: ${student.groupNumber}` : "Chưa có nhóm";

    const studentName = document.createElement("p");
    studentName.className = "student-name";
    studentName.textContent = student.name;

    const studentMssv = document.createElement("p");
    studentMssv.className = "student-mssv";
    studentMssv.textContent = `MSSV: ${student.mssv}`;

    info.appendChild(orderTag);
    info.appendChild(studentName);
    info.appendChild(studentMssv);

    const actionBtn = document.createElement("button");
    actionBtn.className = "btn-manual-checkin";
    actionBtn.textContent = "Điểm danh tay";
    actionBtn.addEventListener("click", () => {
        openManualModal(student);
    });

    card.appendChild(info);
    card.appendChild(actionBtn);

    return card;
}

/* ==============================================
   MANUAL CHECK-IN
   ============================================== */

function openManualModal(student) {
    selectedStudentForManual = student;
    manualModalDesc.textContent = `Bạn có chắc muốn điểm danh thủ công cho sinh viên: ${student.name} (MSSV: ${student.mssv})?`;
    manualModal.classList.remove("hidden");
}

function closeManualModal() {
    selectedStudentForManual = null;
    manualModal.classList.add("hidden");
}

async function handleConfirmManualCheckin() {
    if (!selectedStudentForManual) return;

    const studentName = selectedStudentForManual.name;
    const studentMssv = selectedStudentForManual.mssv;

    confirmManualBtn.disabled = true;
    confirmManualBtn.textContent = "Đang xử lý...";

    try {
        const response = await authFetch(`/api/dashboard/manual-checkin/${encodeURIComponent(studentMssv)}`, {
            method: "POST"
        });

        if (!response.ok) {
            let errorMsg = "Điểm danh thủ công thất bại.";
            try {
                const errJson = await response.json();
                if (errJson.message) errorMsg = errJson.message;
            } catch (e) {}
            throw new Error(errorMsg);
        }

        closeManualModal();
        showToast(`Đã điểm danh thủ công cho ${studentName}!`);
        await loadDashboard();

    } catch (error) {
        alert(error.message || "Điểm danh thủ công thất bại.");
    } finally {
        confirmManualBtn.disabled = false;
        confirmManualBtn.textContent = "Xác nhận có mặt";
    }
}

/* ==============================================
   SESSION TOGGLE
   ============================================== */

async function toggleSession() {
    const currentStatus = toggleSessionBtn.dataset.status;
    if (!currentStatus) return;

    const isOpen = currentStatus === "OPEN";
    const endpoint = isOpen
        ? "/api/attendance-session/close"
        : "/api/attendance-session/open";

    toggleSessionBtn.disabled = true;
    toggleSessionBtn.textContent = isOpen ? "Đang đóng..." : "Đang mở...";

    try {
        const response = await authFetch(endpoint, { method: "POST" });

        if (!response.ok) {
            let message = "Không thể thay đổi trạng thái điểm danh.";
            try {
                const error = await response.json();
                if (error.message) message = error.message;
            } catch (e) {}
            throw new Error(message);
        }

        const session = await response.json();
        updateSessionStatus(session.status);
        showToast(session.status === "OPEN" ? "Đã mở phiên điểm danh!" : "Đã đóng phiên điểm danh!");

    } catch (error) {
        console.error("Toggle session error:", error);
        alert(error.message || "Không thể thay đổi trạng thái điểm danh.");
    } finally {
        toggleSessionBtn.disabled = false;
    }
}

function updateSessionStatus(status) {
    if (status === "OPEN") {
        sessionStatus.textContent = "ĐANG MỞ";
        sessionStatus.className = "session-status open";
        sessionPulse.className = "pulse-dot open";
        toggleSessionBtn.textContent = "Đóng phiên";
        toggleSessionBtn.dataset.status = "OPEN";
        toggleSessionBtn.style.background = "#dc2626";
    } else {
        sessionStatus.textContent = "ĐÃ ĐÓNG";
        sessionStatus.className = "session-status closed";
        sessionPulse.className = "pulse-dot closed";
        toggleSessionBtn.textContent = "Mở phiên";
        toggleSessionBtn.dataset.status = "CLOSED";
        toggleSessionBtn.style.background = "#10b981";
    }
    toggleSessionBtn.disabled = false;
}

/* ==============================================
   IMAGE PREVIEW
   ============================================== */

function openImagePreview(url, caption) {
    enlargedSelfie.src = url;
    imageCaption.textContent = caption;
    imagePreviewModal.classList.remove("hidden");
}

function closeImagePreview() {
    imagePreviewModal.classList.add("hidden");
    enlargedSelfie.src = "";
}

/* ==============================================
   HELPERS & STATES
   ============================================== */

function formatDateTime(dateTime) {
    if (!dateTime) return "Không xác định";
    const date = new Date(dateTime);
    if (Number.isNaN(date.getTime())) return dateTime;

    return date.toLocaleString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    });
}

function setLoadingState() {
    sessionStatus.textContent = "Đang tải...";
    sessionStatus.className = "session-status";
    totalStudents.textContent = "...";
    attendedStudents.textContent = "...";
    unattendedCount.textContent = "...";
    if (progressPercent) {
        progressPercent.textContent = "...%";
    }
    if (progressBar) {
        progressBar.style.width = "0%";
    }

    toggleSessionBtn.disabled = true;
    toggleSessionBtn.textContent = "Đang tải...";

    renderListMessage(attendanceList, "loading-message", "Đang tải danh sách điểm danh...");
    renderListMessage(unattendedList, "loading-message", "Đang tải danh sách vắng mặt...");
}

function showError(message) {
    sessionStatus.textContent = "Không thể tải";
    sessionStatus.className = "session-status closed";
    sessionPulse.className = "pulse-dot closed";
    totalStudents.textContent = "-";
    attendedStudents.textContent = "-";
    unattendedCount.textContent = "-";
    if (progressPercent) {
        progressPercent.textContent = "-";
    }
    if (progressBar) {
        progressBar.style.width = "0%";
    }

    toggleSessionBtn.textContent = "Thử lại";
    toggleSessionBtn.disabled = false;

    renderListMessage(attendanceList, "dashboard-error", message);
    renderListMessage(unattendedList, "dashboard-error", message);
}

function renderListMessage(container, className, message) {
    const element = document.createElement("div");
    element.className = className;
    element.textContent = message;
    container.replaceChildren(element);
}
