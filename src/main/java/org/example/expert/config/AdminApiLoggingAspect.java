package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@Component
@Aspect
public class AdminApiLoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpServletRequest = getCurrentHttpRequest();
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        String requestUrl = httpServletRequest.getRequestURI();
        LocalDateTime requestedAt = LocalDateTime.now();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] parameterAnnotations = methodSignature.getMethod().getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        String requestBody = "null";

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType() == RequestBody.class) {
                    requestBody = objectMapper.writeValueAsString(args[i]);
                    break;
                }
            }
        }

        log.info("""
                        요청한 사용자 ID = {}
                        요청 URL = {}
                        요청 시각 = {}
                        요청 본문 = {}
                        """,
                userId, requestUrl, requestedAt, requestBody);

        try {
            Object response = joinPoint.proceed();
            LocalDateTime respondedAt = LocalDateTime.now();
            String responseBody = objectMapper.writeValueAsString(response);

            log.info("""
                            요청한 사용자 ID = {}
                            요청 URL = {}
                            응답 시각 = {}
                            응답 본문 = {}
                            """,
                    userId, requestUrl, respondedAt, responseBody);

            return response;
        } catch (Throwable e) {
            LocalDateTime respondedAt = LocalDateTime.now();

            log.error("""
                            요청한 사용자 ID = {}
                            요청 URL = {}
                            응답 시각 = {}
                            예외 타입 = {}
                            예외 메시지 = {}
                            """,
                    userId, requestUrl, respondedAt, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("HTTP 요청이 없습니다.");
        }
        return attributes.getRequest();
    }
}
