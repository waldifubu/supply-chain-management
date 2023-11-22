package com.example.supplychainmanagement.service;

import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.entity.users.User;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findByEmail(String email);

    List<UserDto> findAllUsers();

    List<User> findAllRawUsers();

    UserDto convertEntityToDto(User user);

    boolean hasRole(String[] rolesAllowed);
}
