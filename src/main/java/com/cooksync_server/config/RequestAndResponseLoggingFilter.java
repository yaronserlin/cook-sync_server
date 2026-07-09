package com.cooksync_server.config;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

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

    private final String hostName;
    private final String hostAddress;
    private final String processId;

    public RequestAndResponseLoggingFilter() {
        String tempName;
        String tempAddress;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            tempName = localHost.getHostName();
            tempAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            tempName = "Unknown-Host";
            tempAddress = "Unknown-IP";
        }
        this.hostName = tempName;
        this.hostAddress = tempAddress;

        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        this.processId = jvmName.contains("@") ? jvmName.split("@")[0] : jvmName;
    }

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
            logResponse(wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String requestData = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray(), StandardCharsets.UTF_8);
        String clientIp = request.getRemoteAddr();

        String json = String.format(
                "{\"type\": \"request\", \"method\": \"%s\", \"uri\": \"%s\", \"ip\": \"%s\", \"data\": \"%s\", \"machine\": {\"host\": \"%s\", \"hostAddress\": \"%s\", \"pid\": \"%s\"}}",
                escapeJson(method),
                escapeJson(uri + queryString),
                escapeJson(clientIp),
                escapeJson(requestData),
                escapeJson(hostName),
                escapeJson(hostAddress),
                escapeJson(processId));

        LOG.info("{}", json);
    }

    private void logResponse(HttpServletResponse response, long duration) {
        String responseData = new String(((ContentCachingResponseWrapper) response).getContentAsByteArray(), StandardCharsets.UTF_8);
        int status = response.getStatus();

        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = runtime.freeMemory() / (1024 * 1024);
        long totalMemoryMb = runtime.totalMemory() / (1024 * 1024);
        long usedMemoryMb = totalMemoryMb - freeMemoryMb;

        String json = String.format(
                "{\"type\": \"response\", \"status\": %d, \"data\": \"%s\", \"processTimeMs\": %d, \"memoryUsedMb\": %d, \"machine\": {\"host\": \"%s\", \"hostAddress\": \"%s\", \"pid\": \"%s\", \"memFreeMb\": %d, \"memTotalMb\": %d}}",
                status,
                escapeJson(responseData),
                duration,
                usedMemoryMb,
                escapeJson(hostName),
                escapeJson(hostAddress),
                escapeJson(processId),
                freeMemoryMb,
                totalMemoryMb);

        LOG.info("{}", json);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
