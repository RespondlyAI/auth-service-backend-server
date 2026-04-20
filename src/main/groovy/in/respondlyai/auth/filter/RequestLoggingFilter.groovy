package in.respondlyai.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(1)
class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter)

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis()

        String method = request.method
        String uri = request.requestURI
        String query = request.queryString ? "?${request.queryString}" : ""
        String remoteAddr = request.remoteAddr

        log.info("--> {} {}{} from [{}]", method, uri, query, remoteAddr)

        try {
            filterChain.doFilter(request, response)
        } finally {
            long duration = System.currentTimeMillis() - start
            int status = response.status
            String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO"

            if (level == "ERROR") {
                log.error("<-- {} {} {} ({}ms)", method, uri, status, duration)
            } else if (level == "WARN") {
                log.warn("<-- {} {} {} ({}ms)", method, uri, status, duration)
            } else {
                log.info("<-- {} {} {} ({}ms)", method, uri, status, duration)
            }
        }
    }
}
