package com.example.organiza.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.example.organiza.models.UserModel;
import com.example.organiza.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserSyncService {

    private static final long LOGIN_REFRESH_MINUTES = 5;

    private final UserRepository userRepository;
    private final String resourceId;

    public UserSyncService(
            UserRepository userRepository,
            @Value("${jwt.auth.converter.resource-id}") String resourceId) {
        this.userRepository = userRepository;
        this.resourceId = resourceId;
    }

    public void sync(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            return;
        }

        UserModel user = userRepository.findByKeycloakId(keycloakId).orElseGet(UserModel::new);
        boolean isNew = user.getId() == null;
        LocalDateTime now = LocalDateTime.now();

        if (isNew) {
            user.setKeycloakId(keycloakId);
            user.setCreatedAt(now);
        }

        applyProfile(user, jwt, now);

        if (!isNew && !profileChanged(user, jwt) && !loginRefreshDue(user, now)) {
            return;
        }

        if (isNew || loginRefreshDue(user, now)) {
            user.setLastLogin(now);
        }

        save(user, keycloakId, isNew);
    }

    private void applyProfile(UserModel user, Jwt jwt, LocalDateTime now) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setName(displayName(jwt, username, firstName, lastName));
        user.setRoles(extractRoles(jwt));
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
    }

    private boolean profileChanged(UserModel persisted, Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        return !Objects.equals(persisted.getUsername(), username)
                || !Objects.equals(persisted.getEmail(), jwt.getClaimAsString("email"))
                || !Objects.equals(persisted.getFirstName(), firstName)
                || !Objects.equals(persisted.getLastName(), lastName)
                || !Objects.equals(persisted.getName(), displayName(jwt, username, firstName, lastName))
                || !Objects.equals(persisted.getRoles(), extractRoles(jwt));
    }

    private boolean loginRefreshDue(UserModel user, LocalDateTime now) {
        return user.getLastLogin() == null
                || user.getLastLogin().isBefore(now.minusMinutes(LOGIN_REFRESH_MINUTES));
    }

    private void save(UserModel user, String keycloakId, boolean isNew) {
        try {
            userRepository.save(user);
            if (isNew) {
                log.info("Saved new user from Keycloak subject {}", keycloakId);
            }
        } catch (DuplicateKeyException ex) {
            UserModel existing = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> ex);
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setName(user.getName());
            existing.setRoles(user.getRoles());
            existing.setLastLogin(user.getLastLogin());
            userRepository.save(existing);
        }
    }

    private String displayName(Jwt jwt, String username, String firstName, String lastName) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        String combined = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        if (!combined.isBlank()) {
            return combined;
        }
        return username;
    }

    private List<String> extractRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        addRoles(roles, jwt.getClaim("realm_access"), true);
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.get(resourceId) instanceof Map<?, ?> resource) {
            addRoles(roles, resource, false);
        }
        return List.copyOf(roles);
    }

    private void addRoles(Set<String> roles, Object accessClaim, boolean filterDefaults) {
        if (!(accessClaim instanceof Map<?, ?> access)) {
            return;
        }
        Object rawRoles = access.get("roles");
        if (!(rawRoles instanceof Collection<?> collection)) {
            return;
        }
        for (Object role : collection) {
            if (!(role instanceof String name) || name.isBlank()) {
                continue;
            }
            if (filterDefaults && isDefaultRealmRole(name)) {
                continue;
            }
            roles.add(name);
        }
    }

    private boolean isDefaultRealmRole(String role) {
        return role.startsWith("default-roles-")
                || role.equals("offline_access")
                || role.equals("uma_authorization");
    }
}
