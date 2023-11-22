package com.example.supplychainmanagement.service;

import com.example.supplychainmanagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository userRepository) {
        this.roleRepository = userRepository;
    }

    //method invoked during the startup
    public void run(ApplicationArguments args) {
        System.out.println("HALLO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
        /*
        roleRepository.save(new Role("ROLE_CUSTOMER"));
        roleRepository.save(new Role("ROLE_ENTERPRISE"));
        roleRepository.save(new Role("ROLE_SUPPLIER"));
        roleRepository.save(new Role("ROLE_DISTRIBUTOR"));
        roleRepository.save(new Role("ROLE_ADMIN"));*/
    }
}