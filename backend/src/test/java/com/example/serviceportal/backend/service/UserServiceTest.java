package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.UserResponseDto;
import com.example.serviceportal.backend.dto.UserRoleUpdateDto;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import com.example.serviceportal.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void updateRole_shouldRejectRemovingOwnAdminRole() {
        User admin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);

        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("USER");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(1L, dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Du kannst dir die Administratorrolle nicht selbst entziehen.", ex.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRole_shouldPersistNewRole() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User targetUser = createUser(2L, "Max", "max@test.de", UserRole.USER);

        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("ADMIN");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.updateRole(2L, dto, authentication);

        assertEquals("ADMIN", result.getRole());
        assertEquals(UserRole.ADMIN, targetUser.getRole());
        verify(userRepository).save(targetUser);
    }

    @Test
    void deleteUser_shouldRejectUserWithExistingRequests() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User targetUser = createUser(2L, "User", "user@test.de", UserRole.USER);

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));
        when(serviceRequestRepository.existsByUser_Id(2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(2L, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Benutzer mit vorhandenen Serviceanfragen können nicht gelöscht werden.", ex.getReason());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteRegularUserWithoutRequests() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User targetUser = createUser(2L, "User", "user@test.de", UserRole.USER);

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));
        when(serviceRequestRepository.existsByUser_Id(2L)).thenReturn(false);

        userService.deleteUser(2L, authentication);

        verify(userRepository).delete(targetUser);
    }

    @Test
    void updateRole_shouldRejectInvalidRole() {
        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("SUPER_ADMIN");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(2L, dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Ungültige Rolle", ex.getReason());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRole_shouldRejectWhenAuthenticationIsMissing() {
        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("ADMIN");

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(createUser(2L, "User", "user@test.de", UserRole.USER)));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(2L, dto, null));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Nicht angemeldet", ex.getReason());
    }

    @Test
    void deleteUser_shouldRejectDeletingLastAdmin() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User targetAdmin = createUser(2L, "Second Admin", "second@test.de", UserRole.ADMIN);

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(2L, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Der letzte verbleibende Administrator kann nicht gelöscht werden.", ex.getReason());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldRejectDeletingOwnAccount() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentAdmin));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(1L, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Du kannst dein eigenes Benutzerkonto nicht löschen.", ex.getReason());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateRole_shouldRejectDemotingLastAdmin() {
        User currentAdmin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User targetAdmin = createUser(2L, "Second Admin", "second@test.de", UserRole.ADMIN);

        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("USER");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(currentAdmin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateRole(2L, dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Der letzte verbleibende Administrator kann nicht herabgestuft werden.", ex.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnMappedDtos() {
        User admin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User user = createUser(2L, "User", "user@test.de", UserRole.USER);

        when(userRepository.findAll()).thenReturn(List.of(admin, user));

        var result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getRole());
        assertEquals("USER", result.get(1).getRole());
    }

    private User createUser(Long id, String name, String email, UserRole role) {
        User user = new User();
        setUserId(user, id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash("hashed");
        return user;
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}