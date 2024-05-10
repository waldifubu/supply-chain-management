package com.example.supplychainmanagement.service.business;

import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    private final RoleRepository roleRepository;

    public AdminService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public boolean isAdmin(User user) {
        Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_ADMIN.name());

        // First check, if we are admin
        return optionalRole.isPresent() && user.getRoles().contains(optionalRole.get());
    }
}
