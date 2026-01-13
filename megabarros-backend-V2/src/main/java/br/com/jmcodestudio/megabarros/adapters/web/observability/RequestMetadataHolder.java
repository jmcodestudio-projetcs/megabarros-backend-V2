package br.com.jmcodestudio.megabarros.adapters.web.observability;

/**
 * The RequestMetadataHolder class provides a utility for storing and retrieving
 * request-specific metadata (such as IP address and User-Agent) using ThreadLocal.
 * This ensures that metadata remains isolated to the current thread, which is particularly
 * useful in multi-threaded or web-based applications where request data is thread-scoped.
 *
 * It supports methods for setting, retrieving, and clearing metadata for
 * IP address and User-Agent.
 *
 * RequestMetadataFilter/Holder/Adapter: filtro web captura IP/User-Agent por requisição; o Adapter expõe via RequestMetadataPort.
 */
class RequestMetadataHolder {
    private static final ThreadLocal<String> IP = new ThreadLocal<>();
    private static final ThreadLocal<String> UA = new ThreadLocal<>();

    static void setIp(String ip) { IP.set(ip); }
    static void setUa(String ua) { UA.set(ua); }
    static String ip() { return IP.get(); }
    static String ua() { return UA.get(); }
    static void clear() { IP.remove(); UA.remove(); }
}