package com.cooksync_server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestAndResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestAndResponseLoggingFilter.class);

    // Matches "password"/"token"/"secret"/"apiKey"-style JSON keys (any case, optional
    // separators) so credentials submitted to /api/auth/** never reach the DEBUG log,
    // which is enabled by default for this package (see application.properties).
    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(?i)(\"[^\"]*(password|token|secret|apiKey)[^\"]*\"\\s*:\\s*)\"[^\"]*\"");
    private static final String REDACTED_JSON_VALUE = "$1\"***REDACTED***\"";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            logRequest(wrappedRequest);
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedResponse, duration, request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String requestData = readBody(((ContentCachingRequestWrapper) request).getContentAsByteArray());
        String clientIp = request.getRemoteAddr();

        LOG.info(
                "REQUEST | method={} uri={} ip={}",
                method,
                uri + queryString,
                clientIp);

        if (LOG.isDebugEnabled()) {
            LOG.debug("REQUEST payload={}", truncatePayload(requestData));
        }
    }

    private void logResponse(HttpServletResponse response, long duration, String requestUri) {
        String responseData = readBody(((ContentCachingResponseWrapper) response).getContentAsByteArray());
        int status = response.getStatus();

        LOG.info(
                "RESPONSE | status={} uri={} durationMs={}",
                status,
                requestUri,
                duration);

        if (LOG.isDebugEnabled()) {
            LOG.debug("RESPONSE payload={}", truncatePayload(responseData));
        }
    }

    private String readBody(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String truncatePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "<empty>";
        }
        String compact = payload.replace("\n", " ").replace("\r", " ").trim();
        compact = SENSITIVE_JSON_FIELD.matcher(compact).replaceAll(REDACTED_JSON_VALUE);
        if (compact.length() <= 220) {
            return compact;
        }
        return compact.substring(0, 217) + "...";
    }
}
