package br.com.jmcodestudio.megabarros.application.port.out;

/**
 * Interface for managing login rate limiting.
 * Provides methods to control and track login attempts based on a specific key (e.g., IP address or email).
 *
 * Responsibilities:
 * - Allowing or denying login attempts based on defined rate-limiting rules.
 * - Recording failed login attempts to influence the rate-limiting logic.
 * - Recording successful logins to reset relevant counters or metrics.
 *
 * Usage context:
 * - Used to mitigate brute force attacks by limiting the number of login attempts.
 * - Helps protect user accounts and system resources by enforcing rate limits.
 *
 * Evita brute-force em /auth/login. Implementação simples in-memory para dev; depois você pode trocar por Redis.
 */
public interface LoginRateLimiterPort {
    boolean allow(String key);           // IP ou email
    void recordFailure(String key);
    void recordSuccess(String key);
}