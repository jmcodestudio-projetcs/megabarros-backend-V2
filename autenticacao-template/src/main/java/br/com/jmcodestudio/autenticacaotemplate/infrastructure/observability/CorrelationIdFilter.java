package br.com.jmcodestudio.autenticacaotemplate.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * A filter that ensures each HTTP request contains a unique correlation ID,
 * which is used for tracking and logging purposes. If a correlation ID is
 * not provided in the request header, a new one will be generated.
 *
 * The correlation ID is added to the MDC (Mapped Diagnostic Context) so that
 * logs can include it for tracing purposes. It is also included in the
 * response header to allow clients to reference the same ID for follow-up
 * interactions.
 *
 * This filter operates once per request and ensures that the correlation ID
 * is consistently managed throughout the lifecycle of the request.
 *
 * Key functionalities:
 * - Retrieves the correlation ID from the request header, falling back to
 *   generating a new UUID if not provided.
 * - Stores the correlation ID in the MDC for logging context.
 * - Adds the correlation ID to the response header for client-side reference.
 * - Cleans up the MDC to prevent leakage of data across threads.
 *
 * Facilita rastreamento de requisições em logs e suporte. Adiciona X-Correlation-ID no header de resposta e carrega em MDC para logs.
 * Adiciona e propaga correlation ID em cada requisição; facilita depuração
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cid = request.getHeader(HEADER);
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();

        MDC.put(MDC_KEY, cid);
        response.setHeader(HEADER, cid);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}