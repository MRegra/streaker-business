package com.streaker.utlis.logs;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        StatusCaptureWrapper resWrapper = new StatusCaptureWrapper((HttpServletResponse) response);

        String path = req.getRequestURI();
        String method = req.getMethod();
        String ip = req.getRemoteAddr();

        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("path", path);
        MDC.put("method", method);
        MDC.put("ip", ip);

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, resWrapper);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = resWrapper.getStatusCode();

            log.info("{} {} from {} -> {} ({} ms)", method, path, ip, status, duration);
            MDC.clear();
        }
    }
}