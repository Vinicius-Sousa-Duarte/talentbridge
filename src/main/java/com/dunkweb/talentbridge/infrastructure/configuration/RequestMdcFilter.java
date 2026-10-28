package com.dunkweb.talentbridge.infrastructure.configuration;

import com.dunkweb.talentbridge.shared.constants.LogConstants;
import com.dunkweb.talentbridge.shared.constants.MdcConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request);

        MDC.put(MdcConstants.REQUEST_ID, requestId);
        MDC.put(MdcConstants.HTTP_METHOD, request.getMethod());
        MDC.put(MdcConstants.REQUEST_PATH, request.getRequestURI());
        response.setHeader(MdcConstants.REQUEST_ID_HEADER, requestId);

        long startedAt = System.nanoTime();
        try {
            log.debug("{} HTTP request started method={} path={}",
                    LogConstants.REQUEST,
                    request.getMethod(),
                    request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{} HTTP request finished method={} path={} status={} durationMs={}",
                    LogConstants.REQUEST,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);
            MDC.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestIdHeader = request.getHeader(MdcConstants.REQUEST_ID_HEADER);
        if (requestIdHeader == null || requestIdHeader.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestIdHeader.trim();
    }
}
