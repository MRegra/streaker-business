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
import java.util.Collections;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        StatusCaptureWrapper resWrapper = new StatusCaptureWrapper((HttpServletResponse) response);

        MDC.put("method", req.getMethod());
        MDC.put("uri", req.getRequestURI());
        MDC.put("remote_ip", req.getRemoteAddr());

        try {
            chain.doFilter(request, response);
        } finally {
            log.info("Incoming request: {} {} from {}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
            Collections.list(req.getHeaderNames()).forEach(name ->
                    log.debug("Header: {}={}", name, req.getHeader(name))
            );
            MDC.clear();
        }
    }
}
