package com.example.organiza.dto;

import java.time.LocalDateTime;
import java.util.List;

public record User (
    String name,
    String email,
    String id,
    String keycloakId,
    String username,
    String firstName,
    String lastName,
    List<String> roles,
    LocalDateTime createdAt,
    LocalDateTime lastLogin
){}
