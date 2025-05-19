package com.example.bankrest.service;

import com.example.bankrest.dto.UserDetailsDto;
import com.example.bankrest.dto.UserDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.NotFoundException;
import com.example.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. getUserDetailsById

    @Test
    void getUserDetailsByIdSuccess() {
        User u = new User();
        u.setId(7L);
        u.setUsername("Alice");
        u.setRole(Role.USER);
        u.setCreatedAt(LocalDateTime.of(2023,5,1,12,0));
        when(userRepo.findById(7L)).thenReturn(Optional.of(u));

        UserDetailsDto dto = service.getUserDetailsById(7L);

        assertEquals(7L, dto.getId());
        assertEquals("Alice", dto.getUsername());
        assertEquals("USER", dto.getRole());
        assertEquals(u.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void getUserDetailsByIdNotFound() {
        when(userRepo.findById(5L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.getUserDetailsById(5L));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    // 2. createUser

    @Test
    void createUserSuccess() {
        when(userRepo.findByUsername("Alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pwd")).thenReturn("encoded");
        User saved = new User();
        saved.setId(3L);
        saved.setUsername("Alice");
        saved.setRole(Role.USER);
        when(userRepo.save(any())).thenReturn(saved);

        UserDto dto = service.createUser("Alice", "pwd", "USER");

        assertEquals(3L, dto.getId());
        assertEquals("Alice", dto.getUsername());
        assertEquals("USER", dto.getRole());
        // verify we encoded and saved
        verify(passwordEncoder).encode("pwd");
        verify(userRepo).save(argThat(u -> u.getPassword().equals("encoded")));
    }

    @Test
    void createUserAlreadyExists() {
        when(userRepo.findByUsername("x")).thenReturn(Optional.of(new User()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createUser("x","p","USER"));
        assertTrue(ex.getMessage().contains("Имя пользователя уже существует"));
    }

    // 3. findByUsername

    @Test
    void findByUsernameSuccess() {
        User u = new User();
        u.setId(9L);
        u.setUsername("Alice");
        u.setRole(Role.ADMIN);
        when(userRepo.findByUsername("Alice")).thenReturn(Optional.of(u));

        UserDto dto = service.findByUsername("Alice");
        assertEquals(9L, dto.getId());
        assertEquals("Alice", dto.getUsername());
        assertEquals("ADMIN", dto.getRole());
    }

    // 4. updateUserRole

    @Test
    void updateUserRoleSuccess() {
        User u = new User();
        u.setId(8L);
        u.setRole(Role.USER);
        when(userRepo.findById(8L)).thenReturn(Optional.of(u));

        service.updateUserRole(8L, "ADMIN");
        assertEquals(Role.ADMIN, u.getRole());
    }


    // 5. deleteUser

    @Test
    void deleteUserSuccess() {
        when(userRepo.existsById(4L)).thenReturn(true);
        service.deleteUser(4L);
        verify(userRepo).deleteById(4L);
    }

    @Test
    void deleteUserNotFound() {
        when(userRepo.existsById(10L)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.deleteUser(10L));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    // 6. listUsers

    @Test
    void listUsersMapsPage() {
        Pageable pg = PageRequest.of(0,5);
        User u = new User(); u.setId(2L); u.setUsername("x"); u.setRole(Role.USER);
        Page<User> pageIn = new PageImpl<>(List.of(u));
        when(userRepo.findAll(pg)).thenReturn(pageIn);

        Page<UserDto> out = service.listUsers(pg);
        assertEquals(1, out.getTotalElements());
        assertEquals("x", out.getContent().get(0).getUsername());
    }

    // 7. findUsersByUsernameContaining

    @Test
    void findUsersByUsernameContainingMapsPage() {
        Pageable pg = PageRequest.of(0,3);
        User u = new User(); u.setId(5L); u.setUsername("ann"); u.setRole(Role.ADMIN);
        Page<User> pageIn = new PageImpl<>(List.of(u));
        when(userRepo.findAllByUsernameContains("an", pg)).thenReturn(pageIn);

        Page<UserDto> out = service.findUsersByUsernameContaining("an", pg);
        assertEquals(1, out.getTotalElements());
        assertEquals("ADMIN", out.getContent().get(0).getRole());
    }
}
