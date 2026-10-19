const mssvInput = document.getElementById("mssv");
const checkStudentBtn = document.getElementById("checkStudentBtn");
const mssvSection = document.getElementById("mssvSection");

const studentInfo = document.getElementById("studentInfo");
const errorMessage = document.getElementById("errorMessage");
const changeMssvBtn = document.getElementById("changeMssvBtn");

const studentMssv = document.getElementById("studentMssv");
const studentName = document.getElementById("studentName");
const studentGroupNumber = document.getElementById("studentGroupNumber");

const cameraSection = document.getElementById("cameraSection");
const camera = document.getElementById("camera");
const photoCanvas = document.getElementById("photoCanvas");
const captureBtn = document.getElementById("captureBtn");
const retakeBtn = document.getElementById("retakeBtn");
const previewSection = document.getElementById("previewSection");
const photoPreview = document.getElementById("photoPreview");

const confirmBtn = document.getElementById("confirmBtn");
const confirmModal = document.getElementById("confirmModal");
const cancelConfirmBtn = document.getElementById("cancelConfirmBtn");
const confirmAttendanceBtn = document.getElementById("confirmAttendanceBtn");
const confirmMssv = document.getElementById("confirmMssv");
const confirmName = document.getElementById("confirmName");
const confirmPhoto = document.getElementById("confirmPhoto");
const confirmGroupNumber = document.getElementById("confirmGroupNumber");

const locationStatus = document.getElementById("locationStatus");
const successMessage = document.getElementById("successMessage");

const errorModal = document.getElementById("errorModal");
const closeErrorBtn = document.getElementById("closeErrorBtn");

const successModal = document.getElementById("successModal");

let cameraStream = null;
let selfieBlob = null;
let currentLocation = null;

// Đảm bảo không bao giờ giữ phiên đăng nhập của Lớp trưởng khi đang ở trang sinh viên
function clearDashboardSession() {
    fetch("/api/dashboard/logout", { method: "POST" }).catch(() => {});
}

document.addEventListener("DOMContentLoaded", () => {
    clearDashboardSession();
    checkAttendanceSession();
});

window.addEventListener("pageshow", (event) => {
    if (event.persisted) {
        clearDashboardSession();
    }
});
checkStudentBtn.addEventListener("click", checkStudent);
captureBtn.addEventListener("click", capturePhoto);
retakeBtn.addEventListener("click", retakePhoto);
confirmBtn.addEventListener("click", openConfirmModal);
cancelConfirmBtn.addEventListener("click", closeConfirmModal);
confirmAttendanceBtn.addEventListener("click", handleConfirmAttendance);
closeErrorBtn.addEventListener("click", closeErrorModal);
changeMssvBtn.addEventListener("click", changeMssv);

async function checkAttendanceSession() {

    try {

        const response = await fetch(
            "/api/attendance-session"
        );

        if (!response.ok) {
            throw new Error(
                "Không thể kiểm tra trạng thái điểm danh."
            );
        }

        const session = await response.json();

        console.log(
            "Attendance session:",
            session
        );

        if (session.status === "OPEN") {

            mssvSection.classList.remove("hidden");

        } else {

            showError(
                "Hiện tại không trong thời gian điểm danh."
            );
        }

    } catch (error) {

        console.error(
            "Session check error:",
            error
        );

        showError(
            "Không thể kết nối tới máy chủ."
        );
    }
}

async function checkStudent() {

    const mssv = mssvInput.value.trim();

    studentInfo.classList.add("hidden");
    clearError();

    if (!mssv) {
        showError("Vui lòng nhập MSSV.");
        return;
    }

    try {

        const response = await fetch(
            `/api/students/${encodeURIComponent(mssv)}`
        );

        if (!response.ok) {

            const error = await response.json();

            showError(error.message);
            mssvInput.focus();
            mssvInput.select();

            return;
        }

        const student = await response.json();

        studentMssv.textContent = student.mssv;
        studentName.textContent = student.name;
        studentGroupNumber.textContent = student.groupNumber != null ? student.groupNumber : "";

        studentInfo.classList.remove("hidden");
        mssvSection.classList.add("hidden");

        await startCamera();
        cameraSection.classList.remove("hidden");

    } catch (error) {

        console.error(error);

        showError(
            "Không thể kết nối tới máy chủ."
        );
    }
}

function showError(message) {
    errorMessage.textContent = message;
    errorModal.classList.remove("hidden");
}

async function startCamera() {

    try {

        cameraStream =
            await navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: "user"
                },
                audio: false
            });

        camera.srcObject = cameraStream;

    } catch (error) {

        console.error(error);

        showError(
            "Không thể truy cập camera. Vui lòng cấp quyền camera."
        );
    }
}

function capturePhoto() {

    const width = camera.videoWidth;
    const height = camera.videoHeight;

    if (!width || !height) {
        showError("Camera chưa sẵn sàng.");
        return;
    }

    photoCanvas.width = width;
    photoCanvas.height = height;

    const context =
        photoCanvas.getContext("2d");

    context.drawImage(
        camera,
        0,
        0,
        width,
        height
    );

    photoCanvas.toBlob(
        function (blob) {

            selfieBlob = blob;

            const imageUrl =
                URL.createObjectURL(blob);

            photoPreview.src = imageUrl;

            previewSection.classList.remove("hidden");
            captureBtn.classList.add("hidden");
            retakeBtn.classList.remove("hidden");
            confirmBtn.classList.remove("hidden");

            stopCamera();
            camera.classList.add("hidden");

        },
        "image/jpeg",
        0.9
    );
}

async function retakePhoto() {

    selfieBlob = null;

    previewSection.classList.add("hidden");
    retakeBtn.classList.add("hidden");
    captureBtn.classList.remove("hidden");
    confirmBtn.classList.add("hidden");
    camera.classList.remove("hidden");

    await startCamera();
}

function stopCamera() {

    if (!cameraStream) {
        return;
    }

    cameraStream
        .getTracks()
        .forEach(track => track.stop());

    cameraStream = null;

    camera.srcObject = null;
}

function openConfirmModal() {
    if (!selfieBlob) {
        showError("Vui lòng chụp ảnh trước.");
        return;
    }

    confirmMssv.textContent = studentMssv.textContent;
    confirmName.textContent = studentName.textContent;
    confirmGroupNumber.textContent = studentGroupNumber.textContent;
    confirmPhoto.src = photoPreview.src;

    locationStatus.classList.add("hidden");
    locationStatus.textContent = "";

    confirmModal.classList.remove("hidden");
}

function closeConfirmModal() {
    confirmModal.classList.add("hidden");
}

async function handleConfirmAttendance() {
    if (!selfieBlob) {
        showError("Vui lòng chụp ảnh trước.");
        return;
    }

    clearError();

    confirmAttendanceBtn.disabled = true;
    confirmAttendanceBtn.textContent = "Đang xử lý...";

    locationStatus.textContent =
        "Đang xác định vị trí của bạn...";
    locationStatus.classList.remove("hidden");

    try {
        const location = await getCurrentLocation();

        currentLocation = location;

        console.log("GPS:", currentLocation);

        locationStatus.textContent =
            "Đã lấy vị trí thành công.";

        await submitAttendance(location);

    } catch (error) {
        console.error(
            "Attendance error:",
            error
        );

        closeConfirmModal();
        locationStatus.classList.add("hidden");

        showError(error.message || "Không thể xác định vị trí. Vui lòng bật GPS và thử lại, hoặc liên hệ lớp trưởng để được điểm danh thủ công.");
    } finally {
        confirmAttendanceBtn.disabled = false;
        confirmAttendanceBtn.textContent =
            "Điểm danh ngay";
    }
}

async function submitAttendance(location) {
    const formData = new FormData();

    formData.append(
        "mssv",
        studentMssv.textContent
    );

    formData.append(
        "selfie",
        selfieBlob,
        "selfie.jpg"
    );

    formData.append("latitude", location.latitude);
    formData.append("longitude", location.longitude);
    formData.append("accuracy", location.accuracy);

    const response = await fetch(
        "/api/attendance",
        {
            method: "POST",
            body: formData
        }
    );

    if (!response.ok) {
        let errorMessage = "Điểm danh thất bại.";
        let errorCode = "";

        try {
            const error = await response.json();

            if (error.message) {
                errorMessage = error.message;
            }
            if (error.code) {
                errorCode = error.code;
            }
        } catch (e) {
            console.error(
                "Không đọc được error response:",
                e
            );
        }

        throw new Error(errorMessage);
    }

    closeConfirmModal();
    openSuccessModal();

    captureBtn.disabled = true;
    retakeBtn.disabled = true;
    confirmBtn.disabled = true;
}

function getCurrentLocation() {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(
                new Error("Trình duyệt không hỗ trợ GPS."));
            return;
        }

        navigator.geolocation.getCurrentPosition(position => {

                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;
                const accuracy = position.coords.accuracy;

                resolve({latitude, longitude, accuracy});
            },

            error => {
                console.error(
                    "GPS error:",
                    error
                );

                let message;
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        message =
                            "Bạn chưa cho phép truy cập vị trí.";
                        break;
                    case error.POSITION_UNAVAILABLE:
                        message =
                            "Không thể xác định vị trí hiện tại.";
                        break;
                    case error.TIMEOUT:
                        message =
                            "Lấy vị trí quá lâu. Vui lòng thử lại.";
                        break;
                    default:
                        message =
                            "Không thể lấy vị trí.";
                }

                reject(
                    new Error(message)
                );
            },

            {enableHighAccuracy: true, timeout: 10000, maximumAge: 0}
        );
    });
}

function clearError() {
    errorMessage.textContent = "";
    errorModal.classList.add("hidden");
}

function closeErrorModal() {
    errorModal.classList.add("hidden");
    if (!studentInfo || studentInfo.classList.contains("hidden")) {
        mssvInput.focus();
        mssvInput.select();
    }
}

function changeMssv() {

    stopCamera();

    selfieBlob = null;

    if (photoPreview.src) {
        URL.revokeObjectURL(photoPreview.src);
    }

    photoPreview.src = "";

    camera.classList.remove("hidden");
    previewSection.classList.add("hidden");

    captureBtn.classList.remove("hidden");
    retakeBtn.classList.add("hidden");
    confirmBtn.classList.add("hidden");

    studentMssv.textContent = "";
    studentName.textContent = "";
    studentGroupNumber.textContent = "";

    studentInfo.classList.add("hidden");
    cameraSection.classList.add("hidden");

    mssvSection.classList.remove("hidden");

    mssvInput.value = "";
    mssvInput.focus();
}

function openSuccessModal() {
    successModal.classList.remove("hidden");
}

/* =========================
   DASHBOARD PIN ON INDEX
   ========================= */

const openPinModalBtn = document.getElementById("openPinModalBtn");
const pinModal = document.getElementById("pinModal");
const cancelPinBtn = document.getElementById("cancelPinBtn");
const submitPinBtn = document.getElementById("submitPinBtn");
const pinInput = document.getElementById("pinInput");
const pinErrorMessage = document.getElementById("pinErrorMessage");

if (openPinModalBtn) {
    openPinModalBtn.addEventListener("click", () => {
        pinModal.classList.remove("hidden");
        pinInput.value = "";
        pinErrorMessage.classList.add("hidden");
        setTimeout(() => pinInput.focus(), 150);
    });
}

if (cancelPinBtn) {
    cancelPinBtn.addEventListener("click", () => {
        pinModal.classList.add("hidden");
    });
}

if (submitPinBtn) {
    submitPinBtn.addEventListener("click", handleVerifyPin);
}

if (pinInput) {
    pinInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            handleVerifyPin();
        }
    });
}

async function handleVerifyPin() {
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

        window.location.href = "/dashboard.html";

    } catch (error) {
        pinErrorMessage.textContent = error.message || "Mã PIN không chính xác.";
        pinErrorMessage.classList.remove("hidden");
        pinInput.focus();
        pinInput.select();
    } finally {
        submitPinBtn.disabled = false;
        submitPinBtn.textContent = "Mở khóa";
    }
}
