package com.attendance_system.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class DashboardAuthInterceptor implements HandlerInterceptor {

    public static final String PIN_HEADER = "X-Dashboard-Pin";
    public static final String AUTHENTICATED_SESSION_ATTRIBUTE = "dashboardAuthenticated";
    private final DashboardProperties dashboardProperties;
    private final PinAttemptRateLimiter pinAttemptRateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Cho qua CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // Xử lý bảo vệ trang giao diện /dashboard.html: nếu chưa xác thực, tự động redirect về /index.html
        if ("/dashboard.html".equals(uri)) {
            boolean isAuthenticated = Boolean.TRUE.equals(request.getSession(false) == null ? null
                    : request.getSession(false).getAttribute(AUTHENTICATED_SESSION_ATTRIBUTE));
            if (!isAuthenticated) {
                response.sendRedirect("/index.html");
                return false;
            }
            // Ngăn chặn tuyệt đối trình duyệt cache trang dashboard.html (chống bfcache khi vuốt màn hình)
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            return true;
        }

        // Cho phép logout mà không cần PIN
        if ("/api/dashboard/logout".equals(uri) && "POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Cho phép sinh viên kiểm tra trạng thái phiên mở/đóng mà không cần PIN
        if ("/api/attendance-session".equals(uri) && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Sinh viên chỉ được phép GỬI điểm danh; các thao tác đọc danh sách đều phải xác thực dashboard.
        if ("/api/attendance".equals(uri) && "POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (Boolean.TRUE.equals(request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(AUTHENTICATED_SESSION_ATTRIBUTE))) {
            return true;
        }

        String providedPin = request.getHeader(PIN_HEADER);
        String expectedPin = dashboardProperties.getPin();

        if (expectedPin != null && !expectedPin.isBlank() && expectedPin.equals(providedPin)) {
            pinAttemptRateLimiter.reset(request);
            return true;
        }

        if ("/api/dashboard/verify-pin".equals(uri) && "POST".equalsIgnoreCase(request.getMethod())) {
            if (pinAttemptRateLimiter.isBlocked(request)) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"PIN_RATE_LIMITED\",\"message\":\"Bạn đã thử quá nhiều lần. Vui lòng thử lại sau 15 phút.\"}");
                return false;
            }
            pinAttemptRateLimiter.recordFailure(request);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"code":"UNAUTHORIZED","message":"Mã PIN quản trị không chính xác hoặc chưa được cung cấp."}
                """);
        return false;
    }
}
