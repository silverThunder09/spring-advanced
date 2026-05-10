package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.exception.ForbiddenException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AdminApiCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        String requestURI = request.getRequestURI();

        if (userId == null || userRole == null) {
            throw new AuthException("인증이 필요합니다");
        }

        if (!UserRole.ADMIN.equals(UserRole.of(userRole))) {
            throw new ForbiddenException("어드민 권한이 필요합니다.");
        }

        log.info("어드민 API : 요청 시각 = {}, URL = {}", LocalDateTime.now(), requestURI);

        return true;
    }
}
