package com.example.organiza.service;

import org.springframework.stereotype.Service;

import com.example.organiza.dto.UserCreatedResponse;
import com.example.organiza.models.UserModel;
import com.example.organiza.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {
    private final UserRepository userRepository;
  
    public UserCreatedResponse onSaveUser (UserModel newUser) {
       UserModel response =  userRepository.save(newUser);

        return new UserCreatedResponse(response.getName(), response.getEmail(), response.getId());
     }
}
