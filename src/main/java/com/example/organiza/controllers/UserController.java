package com.example.organiza.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.organiza.dto.UserCreatedResponse;
import com.example.organiza.models.UserModel;
import com.example.organiza.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired 
    private  UserService userService;

    @GetMapping("/id/{id}")
    public String getUser (@PathVariable String id ) {
        return id;
    }

    @PostMapping("/create")
    public UserCreatedResponse onCreateUser(@RequestBody UserModel entity) {
        return userService.onSaveUser(entity);
    }
}