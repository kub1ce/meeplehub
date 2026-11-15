package ru.meeplehub.domain.entity;

import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String username;
    private String email;
    private String displayName;
    private LocalDateTime registeredAt;
    private UserRole role;
    private UserStatus status;

    public User() {
    }

    public User(Long id,
                String username,
                String email,
                String displayName,
                LocalDateTime registeredAt,
                UserRole role,
                UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.registeredAt = registeredAt;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", registeredAt=" + registeredAt +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
