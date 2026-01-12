package br.com.jmcodestudio.autenticacaotemplate.adapters.security.rate;

import br.com.jmcodestudio.autenticacaotemplate.application.port.out.LoginRateLimiterPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LoginRateLimiterPort interface using an in-memory storage model.
 * This class is designed to handle rate limiting for login attempts based on a specified key,
 * such as an IP address or email.
 *
 * Responsibilities:
 * - Tracks and enforces login rate-limiting rules using an in-memory map.
 * - Calculates and maintains a failure window and lockout period for each key.
 * - Resets counters and lock status upon successful login or upon window reset.
 *
 * Behavior:
 * - The maximum number of allowed failures before locking is defined by {@code maxFailures}.
 * - The failure window duration is governed by {@code windowSeconds}.
 * - Lockout duration after exceeding the allowed failures is controlled by {@code lockSeconds}.
 * - Failed logins are tracked and, when the threshold is exceeded, the key is locked for a period.
 * - Successful logins reset failure and lock counters for the key.
 *
 * Limitations:
 * - This implementation uses in-memory storage (a {@code ConcurrentHashMap}),
 *   which makes it unsuitable for distributed or large-scale deployments.
 * - Does not handle eviction of old entries, which may lead to unbounded memory growth over time.
 *
 * Usage context:
 * - Intended for use in local or single-node environments to protect against brute-force attacks
 *   by rate limiting login attempts.
 * - Can be replaced with a distributed implementation, like one based on Redis, for production use cases.
 *
 * Thread-safety:
 * - The in-memory map is a {@code ConcurrentHashMap}, ensuring thread-safe operations.
 * - Updates to individual entries rely on safe computations and synchronization mechanisms provided
 *   by the map's atomic operations.
 *
 *   Adapter simples in-memory
 */
@Component
public class InMemoryLoginRateLimiterAdapter implements LoginRateLimiterPort {

    static class Entry {
        int failures;
        Instant windowStart;
        boolean locked;
        Instant lockUntil;
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final int maxFailures = 5;
    private final long windowSeconds = 60; // 1 min
    private final long lockSeconds = 300;  // 5 min

    @Override
    public boolean allow(String key) {
        var e = store.computeIfAbsent(key, k -> { var x = new Entry(); x.windowStart = Instant.now(); return x; });
        var now = Instant.now();

        if (e.locked && e.lockUntil != null && now.isBefore(e.lockUntil)) {
            return false;
        }

        if (now.isAfter(e.windowStart.plusSeconds(windowSeconds))) {
            e.windowStart = now;
            e.failures = 0;
            e.locked = false;
            e.lockUntil = null;
        }
        return true;
    }

    @Override
    public void recordFailure(String key) {
        var e = store.computeIfAbsent(key, k -> { var x = new Entry(); x.windowStart = Instant.now(); return x; });
        e.failures++;
        if (e.failures >= maxFailures) {
            e.locked = true;
            e.lockUntil = Instant.now().plusSeconds(lockSeconds);
        }
    }

    @Override
    public void recordSuccess(String key) {
        var e = store.computeIfAbsent(key, k -> { var x = new Entry(); x.windowStart = Instant.now(); return x; });
        e.failures = 0;
        e.locked = false;
        e.lockUntil = null;
    }
}