package com.streaker.annotations;

import com.streaker.config.RedisRateLimiterService;
import com.streaker.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class CustomRateLimitAspect {

    @Value("${rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    private final RedisRateLimiterService limiter;

    @Around("@annotation(limitAnno)")
    public Object around(ProceedingJoinPoint pjp, CustomRateLimit limitAnno) throws Throwable {
        if (!rateLimitingEnabled) {
            return pjp.proceed(); // Bypass rate limiting
        }
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new IllegalStateException("No HTTP request context found. Cannot apply rate limiting.");
        }

        String ip = attrs.getRequest().getRemoteAddr();
        boolean allowed = limiter.tryAcquire(ip);

        if (!allowed) {
            throw new TooManyRequestsException("Too many login attempts. Please try again later.");
        }

        return pjp.proceed();
    }
}

