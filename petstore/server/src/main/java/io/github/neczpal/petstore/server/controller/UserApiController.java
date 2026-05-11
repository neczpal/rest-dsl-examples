package io.github.neczpal.petstore.server.controller;

import io.github.neczpal.petstore.server.User;
import io.github.neczpal.petstore.server.UserApi;
import io.github.neczpal.petstore.server.data.UserEntity;
import io.github.neczpal.petstore.server.data.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class UserApiController implements UserApi {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setEmail(user.email());
        entity.setPassword(user.password());
        entity.setPhone(user.phone());
        entity.setUserStatus(user.userStatus());
        return entity;
    }

    private User toDto(UserEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getPhone(),
                entity.getUserStatus()
        );
    }

    @Override
    public ResponseEntity<User> createUser(User body) {
        log.info("Creating a new user with username: {}", body.username());
        UserEntity saved = userRepository.save(toEntity(body));
        log.info("Successfully created user with ID: {}", saved.getId());
        return ResponseEntity.ok(toDto(saved));
    }

    @Override
    public ResponseEntity<User> createUsersWithListInput(List<User> body) {
        log.info("Creating {} users from list input", body.size());
        List<UserEntity> saved = userRepository.saveAll(body.stream().map(this::toEntity).toList());
        log.info("Successfully created users");
        return ResponseEntity.ok(toDto(saved.getLast())); // Return the last one as per signature
    }

    @Override
    public ResponseEntity<String> loginUser(String username, String password) {
        log.info("Attempting login for user: {}", username);
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            log.info("Login successful for user: {}", username);
            return ResponseEntity.ok("SessionToken12345");
        }
        log.warn("Login failed for user: {}", username);
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Void> logoutUser() {
        log.info("Logging out current user session");
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<User> getUserByName(String username) {
        log.info("Searching for user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(entity -> {
                    log.info("Found user: {}", username);
                    return ResponseEntity.ok(toDto(entity));
                })
                .orElseGet(() -> {
                    log.warn("User {} not found.", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<Void> updateUser(String username, User body) {
        log.info("Updating user with username: {}", username);
        return userRepository.findByUsername(username).map(entity -> {
            entity.setFirstName(body.firstName());
            entity.setLastName(body.lastName());
            entity.setEmail(body.email());
            entity.setPassword(body.password());
            entity.setPhone(body.phone());
            entity.setUserStatus(body.userStatus());
            userRepository.save(entity);
            log.info("Successfully updated user: {}", username);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> {
            log.warn("User {} not found for update.", username);
            return ResponseEntity.notFound().build();
        });
    }

    @Override
    public ResponseEntity<Void> deleteUser(String username) {
        log.info("Attempting to delete user with username: {}", username);
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            log.info("Successfully deleted user: {}", username);
            return ResponseEntity.ok().build();
        }
        log.warn("User {} not found for deletion.", username);
        return ResponseEntity.notFound().build();
    }
}
