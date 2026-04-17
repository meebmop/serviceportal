package com.example.serviceportal.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final long lockDurationSeconds;

    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-duration-seconds:900}") long lockDurationSeconds) {
        this.maxAttempts = maxAttempts;
        this.lockDurationSeconds = lockDurationSeconds;
    }

    public boolean isLocked(String email) {
        if (email == null) {
            return false;
        }

        String key = normalizeKey(email);
        Instant lockEnd = lockedUntil.get(key);

        if (lockEnd == null) {
            return false;
        }

        if (!Instant.now().isBefore(lockEnd)) {
            lockedUntil.remove(key);
            failedAttempts.remove(key);

            return false;
        }

        return true;
    }

    public void loginSucceeded(String email) {
        if (email == null) {
            return;
        }

        String key = normalizeKey(email);

        failedAttempts.remove(key);
        lockedUntil.remove(key);
    }

    public int loginFailed(String email) {
        if (email == null) {
            return 0;
        }

        String key = normalizeKey(email);
        int attempts = failedAttempts.merge(key, 1, Integer::sum);

        if (attempts >= maxAttempts) {
            lockedUntil.put(key, Instant.now().plusSeconds(lockDurationSeconds));
        }

        return attempts;
    }

    public int getRemainingAttempts(String email) {
        if (email == null) {
            return maxAttempts;
        }

        String key = normalizeKey(email);
        int attempts = failedAttempts.getOrDefault(key, 0);

        return Math.max(0, maxAttempts - attempts);
    }

    public long getRemainingLockSeconds(String email) {
        if (email == null) {
            return 0;
        }

        String key = normalizeKey(email);
        Instant lockEnd = lockedUntil.get(key);

        if (lockEnd == null) {
            return 0;
        }

        long remaining = lockEnd.getEpochSecond() - Instant.now().getEpochSecond();

        return Math.max(0, remaining);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    private String normalizeKey(String email) {
        return email.trim().toLowerCase();
    }
}