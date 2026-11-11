package com.example.organiza.repository;
import com.example.organiza.models.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserModel, String> { 
    
}
