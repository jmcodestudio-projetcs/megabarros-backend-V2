package br.com.jmcodestudio.megabarros.adapters.security.rate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLoginRateLimiterAdapterTest {

    @Test
    void locksAfterMaxFailures() {
        var limiter = new InMemoryLoginRateLimiterAdapter();
        String key = "127.0.0.1|user@example.com";

        assertTrue(limiter.allow(key));
        // Registrar 5 falhas (limite definido no adapter)
        limiter.recordFailure(key);
        limiter.recordFailure(key);
        limiter.recordFailure(key);
        limiter.recordFailure(key);
        limiter.recordFailure(key);

        // Deve bloquear
        assertFalse(limiter.allow(key));
    }

    @Test
    void resetsOnSuccess() {
        var limiter = new InMemoryLoginRateLimiterAdapter();
        String key = "127.0.0.1|user@example.com";

        limiter.recordFailure(key);
        limiter.recordSuccess(key);
        assertTrue(limiter.allow(key));
    }
}