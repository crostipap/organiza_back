package com.example.organiza.models;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "users")
@Data 
public class UserModel {
    @Id
    private String id;
    private String keycloakId;
    private String username;
    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}