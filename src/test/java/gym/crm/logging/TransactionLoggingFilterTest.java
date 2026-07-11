package gym.crm.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionLoggingFilterTest {
    private final TransactionLoggingFilter filter = new TransactionLoggingFilter();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(TransactionLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
    }

    @Test
    void doFilterAddsTransactionIdAndLogsRequestDetails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/trainees");
        request.addHeader(TransactionContext.HEADER_NAME, "tx-123");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent("{\"firstName\":\"John\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (servletRequest, servletResponse) -> {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            try (var in = httpRequest.getInputStream()) {
                in.readAllBytes();
            }
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setStatus(200);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"ok\":true}");
        };

        filter.doFilter(request, response, chain);

        assertEquals("tx-123", response.getHeader(TransactionContext.HEADER_NAME));
        assertEquals(200, response.getStatus());
        assertEquals("{\"ok\":true}", response.getContentAsString());
        assertNotNull(request.getContentAsByteArray());
        assertEquals("{\"firstName\":\"John\"}", new String(request.getContentAsByteArray(), StandardCharsets.UTF_8));
        assertTrue(MDC.getCopyOfContextMap() == null || !MDC.getCopyOfContextMap().containsKey(TransactionContext.MDC_KEY));
        assertTrue(appender.list.stream().anyMatch(event -> "tx-123".equals(event.getMDCPropertyMap().get(TransactionContext.MDC_KEY))
                && event.getFormattedMessage().contains("REST request started")));
        assertTrue(appender.list.stream().anyMatch(event -> "tx-123".equals(event.getMDCPropertyMap().get(TransactionContext.MDC_KEY))
                && event.getFormattedMessage().contains("REST call completed")
                && event.getFormattedMessage().contains("requestBody={\"firstName\":\"John\"}")));
        assertNotNull(appender.list);
    }
}


