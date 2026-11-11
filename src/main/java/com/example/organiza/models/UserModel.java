package com.example.organiza.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "users")
@Data 
public class UserModel {
    @Id
    private String id;
    private String name;
    private String email;
}