package com.example.serviceportal.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    @Test
    void loginFailed_shouldIncreaseAttemptCount() {
        LoginAttemptService service = new LoginAttemptService(5, 60);

        int attempts1 = service.loginFailed("user@test.de");
        int attempts2 = service.loginFailed("user@test.de");

        assertEquals(1, attempts1);
        assertEquals(2, attempts2);
        assertEquals(3, service.getRemainingAttempts("user@test.de"));
    }

    @Test
    void loginFailed_shouldLockAfterMaxAttempts() {
        LoginAttemptService service = new LoginAttemptService(3, 60);

        service.loginFailed("user@test.de");
        service.loginFailed("user@test.de");
        service.loginFailed("user@test.de");

        assertTrue(service.isLocked("user@test.de"));
        assertTrue(service.getRemainingLockSeconds("user@test.de") > 0);
    }

    @Test
    void loginSucceeded_shouldResetAttemptsAndLock() {
        LoginAttemptService service = new LoginAttemptService(3, 60);

        service.loginFailed("user@test.de");
        service.loginFailed("user@test.de");
        service.loginSucceeded("user@test.de");

        assertFalse(service.isLocked("user@test.de"));
        assertEquals(3, service.getRemainingAttempts("user@test.de"));
        assertEquals(0, service.getRemainingLockSeconds("user@test.de"));
    }

    @Test
    void isLocked_shouldReturnFalseForNullEmail() {
        LoginAttemptService service = new LoginAttemptService(3, 60);

        assertFalse(service.isLocked(null));
        assertEquals(0, service.loginFailed(null));
        assertEquals(3, service.getRemainingAttempts(null));
        assertEquals(0, service.getRemainingLockSeconds(null));
    }

    @Test
    void shouldNormalizeEmailKeysCaseInsensitive() {
        LoginAttemptService service = new LoginAttemptService(3, 60);

        service.loginFailed("USER@Test.de");
        service.loginFailed("user@test.de");

        assertEquals(1, service.getRemainingAttempts("user@test.de"));
    }

    @Test
    void lockShouldExpireAutomatically() throws InterruptedException {
        LoginAttemptService service = new LoginAttemptService(1, 1);

        service.loginFailed("user@test.de");
        assertTrue(service.isLocked("user@test.de"));

        Thread.sleep(1200);

        assertFalse(service.isLocked("user@test.de"));
        assertEquals(1, service.getRemainingAttempts("user@test.de"));
    }
}