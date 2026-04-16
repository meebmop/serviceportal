package com.example.serviceportal.backend.model;

import com.example.serviceportal.backend.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    void setName_shouldTrimWhitespace() {
        User user = new User();

        user.setName("  Marie Test  ");

        assertEquals("Marie Test", user.getName());
    }

    @Test
    void setName_shouldAllowNull() {
        User user = new User();

        user.setName(null);

        assertNull(user.getName());
    }

    @Test
    void setEmail_shouldTrimAndLowercase() {
        User user = new User();

        user.setEmail("  Marie.TEST@Example.DE  ");

        assertEquals("marie.test@example.de", user.getEmail());
    }

    @Test
    void setEmail_shouldAllowNull() {
        User user = new User();

        user.setEmail(null);

        assertNull(user.getEmail());
    }

    @Test
    void constructor_shouldSetInitialValues() {
        User user = new User("Marie", "marie@test.de", UserRole.ADMIN);

        assertEquals("Marie", user.getName());
        assertEquals("marie@test.de", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void setters_shouldStoreRoleAndPasswordHash() {
        User user = new User();

        user.setRole(UserRole.USER);
        user.setPasswordHash("hashedPassword");

        assertEquals(UserRole.USER, user.getRole());
        assertEquals("hashedPassword", user.getPasswordHash());
    }
}