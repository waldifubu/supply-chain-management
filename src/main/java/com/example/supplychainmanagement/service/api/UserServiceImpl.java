package com.example.supplychainmanagement.service.api;

import com.example.supplychainmanagement.dto.UserDto;
import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.*;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.RoleRepository;
import com.example.supplychainmanagement.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Role role = roleRepository.findByName(userDto.getRole().name()).orElseThrow();
        user.setRoles(List.of(role));

        saveWithUserType(user);
    }

    private void saveWithUserType(User user) {
        UserRole userRole = Enum.valueOf(UserRole.class, user.getRoles().get(0).getName());

        switch (userRole) {
            case ROLE_CUSTOMER -> {
                Customer customer = new Customer();
                BeanUtils.copyProperties(user, customer);
                userRepository.save(customer);
            }
            case ROLE_ENTERPRISE -> {
                Enterprise enterprise = new Enterprise();
                BeanUtils.copyProperties(user, enterprise);
                userRepository.save(enterprise);
            }
            case ROLE_SUPPLIER -> {
                Supplier supplier = new Supplier();
                BeanUtils.copyProperties(user, supplier);
                userRepository.save(supplier);
            }
            case ROLE_DISTRIBUTOR -> {
                Distributor distributor = new Distributor();
                BeanUtils.copyProperties(user, distributor);
                userRepository.save(distributor);
            }
            case ROLE_ADMIN -> {
                Admin admin = new Admin();
                BeanUtils.copyProperties(user, admin);
                userRepository.save(admin);
            }
            default -> throw new IllegalStateException("Unexpected value: " + userRole);
        }
    }

    @Override
    public User findByEmail(String email) {
        Optional<User> object = userRepository.findByEmail(email);
        return object.orElse(null);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllRawUsers() {
        return userRepository.findAll();
    }


    public UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        String[] name = user.getName().split(" ");
        userDto.setFirstName(name[0]);
        userDto.setLastName(name[1]);
        userDto.setEmail(user.getEmail());
        userDto.setId(user.getId());
        userDto.setRole(UserRole.valueOf(user.getRoles().getFirst().getName()));
        return userDto;
    }

    @Override
    public boolean hasRole(String[] rolesAllowed) {
        return false;
    }

    private Role checkRoleExist() {
        Role role = new Role("ROLE_ADMIN");
        role.setName("ROLE_ADMIN");
        return roleRepository.save(role);
    }
}
