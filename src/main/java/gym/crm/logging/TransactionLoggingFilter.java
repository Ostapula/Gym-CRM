package gym.crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);
        request.setAttribute(TransactionContext.HEADER_NAME, transactionId);
        response.setHeader(TransactionContext.HEADER_NAME, transactionId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        Throwable failure = null;

        try {
            TransactionContext.setTransactionId(transactionId);
            log.info("REST request started method={} uri={} transactionId={}", requestWrapper.getMethod(),
                    getRequestUri(requestWrapper), transactionId);
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            failure = ex;
            log.error("REST request failed method={} uri={} transactionId={} error={}", requestWrapper.getMethod(),
                    getRequestUri(requestWrapper), transactionId, ex.getMessage(), ex);
            throw ex;
        } finally {
            try {
                logRestCall(requestWrapper, responseWrapper, transactionId, failure);
                responseWrapper.copyBodyToResponse();
            } finally {
                TransactionContext.clear();
            }
        }
    }

    private void logRestCall(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper,
                             String transactionId, Throwable failure) {
        String requestBody = getPayload(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
        String responseBody = getPayload(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
        int status = responseWrapper.getStatus();
        String failureMessage = failure == null ? null : failure.getMessage();

        if (status >= 400 || failure != null) {
            log.warn("REST call completed method={} uri={} transactionId={} status={} requestBody={} responseBody={} errorMessage={}",
                    requestWrapper.getMethod(), getRequestUri(requestWrapper), transactionId, status, requestBody,
                    responseBody, failureMessage);
        } else {
            log.info("REST call completed method={} uri={} transactionId={} status={} requestBody={} responseBody={}",
                    requestWrapper.getMethod(), getRequestUri(requestWrapper), transactionId, status, requestBody,
                    responseBody);
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(TransactionContext.HEADER_NAME);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = TransactionContext.newTransactionId();
        }
        return transactionId;
    }

    private String getRequestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    private String getPayload(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        String body = new String(content, charset);
        return body.length() <= 2000 ? body : body.substring(0, 2000) + "...(truncated)";
    }
}



