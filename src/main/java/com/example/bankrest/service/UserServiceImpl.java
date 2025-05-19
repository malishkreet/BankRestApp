package com.example.bankrest.service;

import com.example.bankrest.dto.UserDetailsDto;
import com.example.bankrest.dto.UserDto;
import com.example.bankrest.entity.Role;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.NotFoundException;
import com.example.bankrest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public UserDetailsDto getUserDetailsById(Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
        return mapToDetailsDto(u);
    }

    private UserDetailsDto mapToDetailsDto(User u) {
        return new UserDetailsDto(
                u.getId(),
                u.getUsername(),
                u.getRole().name(),
                u.getCreatedAt()
        );
    }


    @Override
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        userRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, String newRole) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        if (u.getId().equals(1L)) {
            throw new AccessDeniedException("Нельзя удалить главного админа");
        }

        Role roleEnum = Role.valueOf(newRole.toUpperCase());
        u.setRole(roleEnum);
    }

    @Override
    public Page<UserDto> findUsersByUsernameContaining(String usernamePart, Pageable pageable) {
        return userRepo.findAllByUsernameContains(usernamePart, pageable)
                .map(this::toDto);
    }

    private UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setRole(user.getRole().name());
        userDto.setPassword(null);
        return userDto;
    }

    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(String username, String rawPassword, String role) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя уже существует");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.valueOf(role));
        user = userRepo.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getRole().name());
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + username));

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        return dto;
    }


}