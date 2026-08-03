package com.cooksync_server.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Unit test for RequestAndResponseLoggingFilter ensuring request/response metadata is logged.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
class RequestAndMachineLoggingFilterTest {

    /**
     * Verifies that request and response details are formatted into logging events.
     *
     * @throws Exception if filter execution fails
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void logsRequestAndResponseDetailsForEachRequest() throws Exception {
        RequestAndResponseLoggingFilter filter = new RequestAndResponseLoggingFilter();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/recipes");
        request.setContent("{\"title\":\"Soup\"}".getBytes(StandardCharsets.UTF_8));
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new Servlet() {
            @Override
            public void init(ServletConfig config) {
            }

            @Override
            public ServletConfig getServletConfig() {
                return null;
            }

            @Override
            public void service(ServletRequest req, ServletResponse res) throws ServletException, java.io.IOException {
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write("{\"id\":1}");
                ((jakarta.servlet.http.HttpServletResponse) res).setStatus(201);
            }

            @Override
            public String getServletInfo() {
                return "";
            }

            @Override
            public void destroy() {
            }
        });

        Logger logger = (Logger) LoggerFactory.getLogger(RequestAndResponseLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            filter.doFilter(request, response, chain);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        List<String> messages = appender.list.stream()
                .map(event -> event.getFormattedMessage())
                .toList();

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).contains("REQUEST");
        assertThat(messages.get(0)).contains("POST");
        assertThat(messages.get(0)).contains("/recipes");
        assertThat(messages.get(0)).contains("127.0.0.1");
        assertThat(messages.get(1)).contains("RESPONSE");
        assertThat(messages.get(1)).contains("201");
        assertThat(messages.get(1)).contains("/recipes");
        assertThat(messages.get(1)).contains("durationMs");
    }
}
