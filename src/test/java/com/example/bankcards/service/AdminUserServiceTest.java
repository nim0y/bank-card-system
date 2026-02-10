package com.example.bankcards.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    @DisplayName("Успешное создание пользователя с хэшированием пароля")
    void createUser_Success() {
        String rawPassword = "secretPassword";
        String encodedPassword = "encoded_hash_123";

        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = adminUserService.createUser("new_user", rawPassword, Role.USER);

        assertNotNull(result);
        assertEquals("new_user", result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(Role.USER, result.getRole());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка при создании пользователя: Username уже занят")
    void createUser_AlreadyExists() {
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                adminUserService.createUser("existing_user", "password", Role.USER)
        );

        assertEquals("Пользователь уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    void getAllUsers_ShouldReturnList() {
        User user1 = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        User user2 = User.builder().id(2L).username("user").role(Role.USER).build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> result = adminUserService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals("ADMIN", result.get(0).getRole());
        verify(userRepository).findAll();
    }
}
