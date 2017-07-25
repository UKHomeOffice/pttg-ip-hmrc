package uk.gov.digital.ho.pttg;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class UserHeaderFilter implements Filter {

    public static final String USER_ID_HEADER = "userId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //does nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader(USER_ID_HEADER);

        try {
            // Setup MDC data:
            MDC.put(USER_ID_HEADER, StringUtils.isNotBlank(userId) ? userId : "Anonymous");
            chain.doFilter(request, response);
        } finally {
            MDC.remove(USER_ID_HEADER);
        }
    }

    @Override
    public void destroy() {
        //does nothing
    }
}
