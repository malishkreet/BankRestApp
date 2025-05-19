package com.example.bankrest.service;

import com.example.bankrest.dto.UserDetailsDto;
import com.example.bankrest.dto.UserDto;
import com.example.bankrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> listUsers(Pageable pageable);

    Page<UserDto> findUsersByUsernameContaining(String usernamePart, Pageable pageable);

    UserDto createUser(String username, String rawPassword, String role);

    UserDetailsDto getUserDetailsById(Long id);

    UserDto findByUsername(String username);

    void deleteUser(Long id);

    void updateUserRole(Long userId, String newRole);
}