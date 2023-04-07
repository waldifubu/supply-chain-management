package com.example.supplychainmanagement.model.enums;

import java.util.Arrays;

public enum UserRole {
    ROLE_CUSTOMER("customer"),
    ROLE_ENTERPRISE("enterprise"),
    ROLE_SUPPLIER("supplier"),
    ROLE_DISTRIBUTOR("distributor"),
    ROLE_ADMIN("admin");

    public final String label;

    private UserRole(String label) {
        this.label = label;
    }

    public static boolean contains(String s) {
        return Arrays.stream(values()).anyMatch(choice -> choice.name().equals(s));
    }

    public static UserRole valueOfLabel(String label) {
        for (UserRole e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
