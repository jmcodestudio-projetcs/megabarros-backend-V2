package br.com.jmcodestudio.megabarros.adapters.web.observability;

import br.com.jmcodestudio.megabarros.application.port.out.RequestMetadataPort;
import org.springframework.stereotype.Component;

/**
 * Adapter class that implements the RequestMetadataPort interface.
 * This class retrieves request-specific metadata such as the IP address
 * and User-Agent from the RequestMetadataHolder.
 *
 * The purpose of this adapter is to expose the captured metadata through
 * the RequestMetadataPort, enabling use cases to access the metadata
 * without coupling to specific HTTP or web framework details.
 *
 * This implementation delegates to the RequestMetadataHolder for fetching
 * the request-scoped data stored in thread-local variables.
 * It acts as a bridge between the thread-local holder and the
 * interfaces used by the core application.
 *
 * RequestMetadataFilter/Holder/Adapter: filtro web captura IP/User-Agent por requisição; o Adapter expõe via RequestMetadataPort.
 */
@Component
public class RequestMetadataAdapter implements RequestMetadataPort {
    @Override public String ip() { return RequestMetadataHolder.ip(); }
    @Override public String userAgent() { return RequestMetadataHolder.ua(); }
}