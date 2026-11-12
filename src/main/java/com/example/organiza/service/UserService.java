package com.example.organiza.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.organiza.dto.User;
import com.example.organiza.models.UserModel;
import com.example.organiza.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(String id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUser(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUser)
                .toList();
    }

    private User toUser(UserModel user) {
        return new User(
                user.getName(),
                user.getEmail(),
                user.getId(),
                user.getKeycloakId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getLastLogin());
    }
}
