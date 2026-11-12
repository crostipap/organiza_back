package com.example.organiza.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import com.example.organiza.models.UserModel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    void ensureUserIndexes() {
        mongoTemplate.indexOps(UserModel.class).ensureIndex(
                new Index()
                        .on("keycloakId", Sort.Direction.ASC)
                        .unique()
                        .sparse()
                        .named("uk_users_keycloakId"));
    }
}
