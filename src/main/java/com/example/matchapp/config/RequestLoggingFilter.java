package com.example.matchapp.config;

import com.example.matchapp.util.LoggingUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add request ID to MDC context for each HTTP request.
 * This enables request tracking across log messages.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Get request ID from header or generate a new one
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            requestId = LoggingUtils.setRequestId(requestId);
            
            // Add request ID to response header
            response.setHeader(REQUEST_ID_HEADER, requestId);
            
            // Log request details
            logger.info("Received request: {} {} (Request ID: {})",
                    request.getMethod(),
                    request.getRequestURI(),
                    requestId);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Log response status
            logger.info("Completed request with status: {} (Request ID: {})",
                    response.getStatus(),
                    LoggingUtils.getRequestId());
            
            // Clear MDC context after request is processed
            LoggingUtils.clearMDC();
        }
    }
}