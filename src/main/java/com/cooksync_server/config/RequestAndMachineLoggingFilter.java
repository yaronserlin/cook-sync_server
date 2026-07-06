package com.cooksync_server.config;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestAndMachineLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestAndMachineLoggingFilter.class);

    private final String hostName;
    private final String hostAddress;
    private final String processId;

    // Cache machine info at startup so we don't fetch it on every request
    public RequestAndMachineLoggingFilter() {
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

        // Gets Process ID (PID)
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        this.processId = jvmName.contains("@") ? jvmName.split("@")[0] : jvmName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            // Continue the request chain
            filterChain.doFilter(request, response);
        } finally {
            // Ensure we log even if the request fails
            long duration = System.currentTimeMillis() - startTime;
            logDetails(request, response, duration);
        }
    }

    private void logDetails(HttpServletRequest request, HttpServletResponse response, long duration) {
        // Request Info
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        int status = response.getStatus();
        String clientIp = request.getRemoteAddr();

        // System Specs Info
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = runtime.freeMemory() / (1024 * 1024);
        long totalMemoryMb = runtime.totalMemory() / (1024 * 1024);
        int availableProcessors = runtime.availableProcessors();

        // Single formatted log line for high readability
        logger.info("[REQ] {} {}{} | Status: {} | Client: {} | Duration: {}ms | "
                + "[MACHINE] Host: {} ({}) | PID: {} | CPUs: {} | Mem Free/Total: {}MB/{}MB",
                method, uri, queryString, status, clientIp, duration,
                hostName, hostAddress, processId, availableProcessors, freeMemoryMb, totalMemoryMb);
    }
}
