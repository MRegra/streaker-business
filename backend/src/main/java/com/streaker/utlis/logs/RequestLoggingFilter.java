package com.streaker.utlis.logs;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        MDC.put("method", req.getMethod());
        MDC.put("uri", req.getRequestURI());
        MDC.put("remote_ip", req.getRemoteAddr());

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // VERY IMPORTANT to avoid memory leaks
        }
    }
}
