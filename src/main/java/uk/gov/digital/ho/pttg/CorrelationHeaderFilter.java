package uk.gov.digital.ho.sponsorship.casework;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
/**
 * Correlation Filter - adds a correlationId into the MDC thread for logging purposes, creating one if non exists
 * The correlationId is obtained from the HTTP request header.
 */
public class CorrelationHeaderFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "correlationId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no initialisation required.
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String currentCorrId = httpServletRequest.getHeader(CORRELATION_ID_HEADER);

        try {
            MDC.put(CORRELATION_ID_HEADER, generateCorrelationIdIfNeeded(currentCorrId));
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }

    /**
     * Generate a correlation ID is one is not provided.
     * @param currentCorrId the current correlation ID
     * @return the existing or generated correlation ID if blank
     */
    public static String generateCorrelationIdIfNeeded(final String currentCorrId) {
        return StringUtils.isBlank(currentCorrId) ? UUID.randomUUID().toString() : currentCorrId;
    }


    @Override
    public void destroy() {
        // no destroy changes required.
    }

}
