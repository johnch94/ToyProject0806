package com.example.demo.user;

/**
 * 사용자 신분 구분
 */
public enum UserRole {
    USER("사용자"),
    ADMIN("관리자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
