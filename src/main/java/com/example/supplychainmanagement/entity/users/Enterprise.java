package com.example.supplychainmanagement.entity.users;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("enterprise")
public class Enterprise extends User {
}
