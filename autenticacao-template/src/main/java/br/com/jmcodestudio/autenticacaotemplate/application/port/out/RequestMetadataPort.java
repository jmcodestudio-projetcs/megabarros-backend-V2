package br.com.jmcodestudio.autenticacaotemplate.application.port.out;

/**
 * Interface representing the port for retrieving request metadata.
 * Provides methods to access information about the current request, including IP address and user-agent.
 * Enables decoupling use cases from specific HTTP frameworks or context implementations.
 *
 * Interface para o core obter IP/User-Agent do contexto atual da requisição sem acoplar a Servlet API.
 */
public interface RequestMetadataPort {
    String ip();
    String userAgent();
}