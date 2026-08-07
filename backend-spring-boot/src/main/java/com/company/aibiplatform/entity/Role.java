package com.company.aibiplatform.entity;

/**
 * Application roles. Kept simple (3 flat roles) to match the README's
 * Admin/Manager/Staff model. Spring Security reads this directly as the
 * granted authority (prefixed with "ROLE_" by Spring Security convention).
 */
public enum Role {
    ADMIN,
    MANAGER,
    STAFF
}