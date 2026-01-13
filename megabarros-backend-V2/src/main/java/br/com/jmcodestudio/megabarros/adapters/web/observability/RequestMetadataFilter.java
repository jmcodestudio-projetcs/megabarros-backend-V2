package br.com.jmcodestudio.megabarros.adapters.web.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The RequestMetadataFilter class is a web filter that captures request-specific metadata,
 * such as the IP address and User-Agent, and sets it into a thread-local holder
 * (RequestMetadataHolder). This metadata is associated with the current HTTP request context
 * and is cleared after the request processing is completed.
 *
 * This filter extends the OncePerRequestFilter, ensuring that it is executed once for every
 * request in a servlet-based application.
 *
 * RequestMetadataFilter/Holder/Adapter: filtro web captura IP/User-Agent por requisição; o Adapter expõe via RequestMetadataPort.
 *
 **/
@Component
public class RequestMetadataFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            var ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
            RequestMetadataHolder.setIp(ip);
            RequestMetadataHolder.setUa(request.getHeader("User-Agent"));
            filterChain.doFilter(request, response);
        } finally {
            RequestMetadataHolder.clear();
        }
    }
}